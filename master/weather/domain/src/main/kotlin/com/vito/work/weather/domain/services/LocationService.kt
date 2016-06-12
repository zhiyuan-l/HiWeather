package com.vito.work.weather.domain.services

import com.vito.work.weather.domain.beans.api.LocationData
import com.vito.work.weather.domain.beans.api.LocationInfo
import com.vito.work.weather.domain.beans.api.locationInfoParser
import com.vito.work.weather.domain.config.Constant
import com.vito.work.weather.domain.config.SpiderStatus
import com.vito.work.weather.domain.daos.LocationDao
import com.vito.work.weather.domain.entities.City
import com.vito.work.weather.domain.entities.District
import com.vito.work.weather.domain.entities.Province
import com.vito.work.weather.domain.services.spider.AQICityPageProcessor
import com.vito.work.weather.domain.util.cnweather.getResultBean
import com.vito.work.weather.domain.util.http.BusinessError
import com.vito.work.weather.domain.util.http.BusinessException
import com.vito.work.weather.domain.util.http.sendGetRequestViaHttpClient
import org.jetbrains.annotations.Contract
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import us.codecraft.webmagic.ResultItems
import us.codecraft.webmagic.Spider
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*
import javax.annotation.PreDestroy

/**
 * Created by lingzhiyuan.
 * Date : 16/4/5.
 * Time : 下午7:06.
 * Description:
 *
 */

@Service(value = "locationService")
@Transactional
open class LocationService @Autowired constructor(val locationDao: LocationDao)
{

    @PreDestroy
    open fun destroy()
    {
        spider.close()
    }

    companion object
    {
        val logger = LoggerFactory.getLogger(LocationService::class.java)

        val spider = Spider.create(AQICityPageProcessor()).thread(Constant.SPIDER_THREAD_COUNT)
    }

    /**
     * 获取所有省份
     * */
    open fun findProvinces(): List<Province>
    {
        return locationDao.findAll(Province::class.java) as List<Province>
    }

    /**
     * 获取省份
     * @param provinceId 省份的 id
     * */
    open fun getProvince(provinceId: Long)
            = locationDao.findById(Province::class.java, provinceId) as Province?

    /**
     * 获取所有城市
     * @param provinceId 省份的 id, 为0时返回所有城市
     * */
    open fun findCities(provinceId: Long = 0): List<City>?
    {

        if (provinceId == 0L)
        {
            return locationDao.findAll(City::class.java) as List<City>?
        }
        return locationDao.findCities(provinceId) as List<City>?
    }

    /**
     * 获取一个城市
     * @param cityId 城市的 id
     * */
    open fun getCity(cityId: Long)
            = locationDao.findById(City::class.java, cityId) as City?

    /**
     * 根据省份列表查询出城市列表
     * @param provinces 需要查询的省份列表
     * */
    open fun findCities(provinces: List<Province>): List<City>
    {
        var cities = mutableListOf<City>()
        for (province in provinces)
        {
            cities.addAll(locationDao.findCities(province.id) as List<City>)
        }
        return cities
    }

    /**
     * 查询出所有的区
     * @param cityId 城市的 id
     * */
    open fun findDistricts(cityId: Long = 0): List<District>?
    {
        if (cityId == 0L)
        {
            return locationDao.findAll(District::class.java) as List<District>
        }
        return locationDao.findDistricts(cityId) as List<District>
    }

    open fun findAQIDistrict(): List<District>?
    {
        return locationDao.findAQIDistrict()
    }

    open fun saveAllDistricts(districts: List<District>)
    {
        for (it in districts)
        {
            locationDao.saveOrUpdate(it)
        }
    }

    /**
     * 城市信息更新总入口
     *
     * Step 1: 更新省份
     * Step 2: 更新城市
     * Step 3: 更新区县
     * Step 4: 更新区县的 pinyin_aqi
     * Step 5: 从文件中更新区县的 id_api
     * Step 6: 删除已经废弃的区县
     * Step 7: 调用中国天气网的 api 更新区县的坐标等信息
     * */
    open fun updateAllLocationsFromWeb()
    {
        try
        {
            SpiderStatus.LOCATION_UPDATE_STATUS = true
            var provinces = updateProvincesFromWeb()
            provinces.forEach { locationDao.saveOrUpdate(it) }
            var cities = updateCititesFromWebByProvinces(provinces)
            cities.forEach { locationDao.saveOrUpdate(it) }
            // 获取 tianqi.com上的所有区县
            var districts = updateDistrictsFromWebByCitites(cities)
            // 给有 aqi 数据的区县添加pinyin_aqi
            updateAQIDistricts(districts)
            // 从文件中筛选出中国天气网上有的区县
            updateDistrictsFromFile(districts)
            var newIds = mutableListOf<Long>()
            var saveDistricts = districts.filter { it.id != 0L }
            saveDistricts.forEach { newIds.add(it.id) }
            var obsoleteData = locationDao.findObsoleteDistricts(newIds) ?: listOf()
            locationDao.batchDelete(obsoleteData)
            updateDistrictViaAPI(saveDistricts)
            saveDistricts.forEach { locationDao.saveOrUpdate(it) }
            logger.info("Location Updated")
        }
        catch(ex: Exception)
        {
            ex.printStackTrace()
            throw BusinessException(BusinessError.ERROR_LOCATION_UPDATE_FAILED)
        }
        finally
        {
            SpiderStatus.LOCATION_UPDATE_STATUS = false
        }
    }

} // end LocationService

/**
 * 通过网站 API获取省的信息, 并保存到数据库中
 * */
private fun updateProvincesFromWeb(): List<Province>
{
    var provinces = listOf<Province>()

    var params = HashMap<String, Any>()
    params.put("type", LocationData.LOCATION_INFO_TYPE_ZERO)
    // 获取原始的JSON数据, 结构为 List
    var data: String = fetchAndConvertDataFromWeb(params)
    // 通过 mapper 转换成 LocationInfo 对象
    var locationInfo = locationInfoParser(LocationData.LOCATION_INFO_TYPE_ZERO, data)
    // 将对象中的数据保存到数据库中
    if (locationInfo != null)
    {
        provinces = getProvincesFromLocationInfo(locationInfo)
    }

    LocationService.logger.info("Provinces Updated")
    return provinces
}

/**
 * 通过网站API, 根据省爬取所有市的信息
 * */
@Contract("null->null")
private fun updateCititesFromWebByProvinces(provinces: List<Province>): MutableList<City>
{

    var cities = mutableListOf<City>()
    for (province in provinces)
    {
        var params = HashMap<String, Any>()
        params.put("type", LocationData.LOCATION_INFO_TYPE_ONE)
        params.put("pid", province.id.toString())
        var data = fetchAndConvertDataFromWeb(params)

        // 获取区域信息
        var locationInfo = locationInfoParser(LocationData.LOCATION_INFO_TYPE_ONE, data)
        if (locationInfo != null)
        {
            cities.addAll(getCitiesFromLocationInfo(locationInfo, province.id))
        }
    }
    LocationService.logger.info("Cities Updated")

    return cities
}

/**
 * 通过网站 API, 根据市爬去所有区县信息
 * */
@Contract("null->null")
private fun updateDistrictsFromWebByCitites(citites: List<City>): List<District>
{
    var districts = mutableListOf<District>()
    for (city in citites)
    {
        var params = HashMap<String, Any>()
        params.put("type", LocationData.LOCATION_INFO_TYPE_TWO)
        params.put("pid", city.province)
        params.put("cid", city.id)
        var data = fetchAndConvertDataFromWeb(params)

        // 获取区域信息
        var locationInfo = locationInfoParser(LocationData.LOCATION_INFO_TYPE_TWO, data)
        if (locationInfo != null)
        {
            districts.addAll(getDistrictsFromLocationInfo(locationInfo, city.id))
        }
    }

    return districts
}

/**
 * 从获取到的区域信息中提取省信息并保存
 * */
private fun getProvincesFromLocationInfo(locationInfo: LocationInfo): List<Province>
{
    var provinces = mutableListOf<Province>()
    var value = locationInfo.value.toSortedMap()
    var pys = locationInfo.py.toSortedMap()
    var ishots = locationInfo.ishot.toSortedMap()
    for ((k, v) in value)
    {
        var province = Province()
        with(province) {
            id = k
            // 去除字母前缀
            title = v?.split(" ")?.last() !!
            pinyin = pys[k] !!
            ishot = ishots[k] !!
        }

        provinces.add(province)
    }
    return provinces
}

/**
 * 从获取到的区域信息中提取城市信息并保存
 * */
private fun getCitiesFromLocationInfo(locationInfo: LocationInfo, provinceId: Long): List<City>
{
    var value = locationInfo.value.toSortedMap()
    var pys = locationInfo.py.toSortedMap()
    var ishots = locationInfo.ishot.toSortedMap()

    var cities = mutableListOf<City>()
    for ((k, v) in value)
    {
        var city: City = City()
        with(city) {
            id = k
            title = v?.split(" ")?.last() !!
            pinyin = pys[k] !!
            ishot = ishots[k] !!
            province = provinceId
        }

        cities.add(city)
    }

    return cities
}

/**
 * 从获取到的区域信息中提取省信息并保存
 * */
private fun getDistrictsFromLocationInfo(locationInfo: LocationInfo, cityId: Long): List<District>
{

    var districts = mutableListOf<District>()

    var value = locationInfo.value.toSortedMap()
    var pys = locationInfo.py.toSortedMap()
    var ishots = locationInfo.ishot.toSortedMap()
    for ((k, v) in value)
    {
        var district = District()
        with(district) {
            id = k
            city = cityId
            title = v?.split(" ")?.last() !!
            pinyin = pys[k] !!
            ishot = ishots[k] !!
        }

        districts.add(district)
    }
    return districts
}

/**
 * 获取数据并转换成正确的 json 格式
 * */
private fun fetchAndConvertDataFromWeb(params: HashMap<String, Any>): String
{
    var data = sendGetRequestViaHttpClient(Constant.LOCATION_SOURCE_URL, params, hashMapOf(), Charset.forName("utf-8"))
    data = data?.substring(data.indexOf('(') + 1, data.length - 1)?.removeSuffix(")")

    return data ?: throw BusinessException(BusinessError.ERROR_RESOURCE_NOT_FOUND)
}

/**
 * 到网上获取区县的 aqi pinyin
 *
 * 天气网有两套拼音,一套大多数天气通用的区县拼音, 第二套市空气质量板块专有的 pinyin, 需要区分使用
 *
 * */
private fun updateAQIDistricts(districts: List<District>)
{
    var resultItems: ResultItems = LocationService.spider.get("http://www.tianqi.com/air/")

    var receivedUrls: List<String> = resultItems.get("urls")
    var titles: List<String> = resultItems.get("titles")
    var pinyins = mutableListOf<String>()

    for (url in receivedUrls)
    {
        var pinyin_aqi = url.substringAfter("/air/").removeSuffix(".html")
        pinyins.add(pinyin_aqi)
    }

    for (district in districts)
    {
        titles.forEachIndexed { index, it ->
            if (it.contains(district.title.split(" ").last()))
            {
                district.pinyin_aqi = pinyins[index]
            }
        }
    }

}

private fun updateDistrictViaAPI(districts: List<District>)
{
    districts.forEach {
        try
        {
            val resultBean = getResultBean(it)
            val c = resultBean?.c
            if (c != null)
            {
                it.longitude = c.c13
                it.latitude = c.c14
                it.zipcode = c.c12
                it.altitude = c.c15.toDouble()
            }
        }
        catch(ex: Exception)
        {
            ex.printStackTrace()
        }
    }
}

/**
 * 从文件中更新区县信息
 *
 * 文件来源为中国天气网, 所有基本信息以中国天气网为准
 * */
private fun updateDistrictsFromFile(districts: List<District>)
{

    var strList = mutableListOf<String>()
    var reader: InputStreamReader
    var br: BufferedReader
    try
    {
        reader = InputStreamReader(LocationService::class.java.getResourceAsStream(Constant.DISTRICT_API_FILE_LOCATION), "UTF-8")
        br = BufferedReader(reader)
        var line = br.readLine()
        while (line != null)
        {
            if (line.trim() != "")
            {
                strList.add(line)
            }
            line = br.readLine()
        }
        reader.close()
        br.close()
    }
    catch(ex: Exception)
    {
        ex.printStackTrace()
        throw ex
    }

    var tempDistricts = mutableListOf<TempDistrict>()
    for (str in strList)
    {
        if (str.trim() != "")
        {
            var tempList = str.trim().split(",")

            tempDistricts.add(TempDistrict(tempList[0].toLong(), tempList[1], tempList[2], tempList[3]))
        }
    }

    districts.forEachIndexed { index, district ->
        var tempDistrict = tempDistricts.firstOrNull { it.id == district.id }
        if (tempDistrict != null)
        {
            district.title = tempDistrict.title
        }
        else
        {
            district.id = 0L
        }
    }

}

data class TempDistrict(val id: Long, val title: String, val city: String, val province: String)