package com.vito.work.weather.service

import com.vito.work.weather.domain.beans.api.LocationData
import com.vito.work.weather.domain.beans.api.LocationInfo
import com.vito.work.weather.domain.beans.api.locationInfoParser
import com.vito.work.weather.config.Constant
import com.vito.work.weather.repo.LocationDao
import com.vito.work.weather.dto.City
import com.vito.work.weather.dto.District
import com.vito.work.weather.dto.Province
import com.vito.work.weather.service.spider.AQICityPageProcessor
import com.vito.work.weather.util.cnweather.getResultBean
import com.vito.work.weather.util.http.BusinessError
import com.vito.work.weather.util.http.BusinessException
import com.vito.work.weather.util.http.HttpUtil
import com.vito.work.weather.util.http.sendGetRequestViaHttpClient
import org.jetbrains.annotations.Contract
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import us.codecraft.webmagic.ResultItems
import us.codecraft.webmagic.Spider
import us.codecraft.webmagic.scheduler.QueueScheduler
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*
import javax.annotation.PreDestroy
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/5.
 * Time : 下午7:06.
 * Description:
 *
 */

@Service(value = "locationService")
@Transactional
open class LocationService : AbstractSpiderTask() {

    @Resource
    lateinit var locationDao: LocationDao

    @PreDestroy
    open fun destroy() {
        spider.close()
    }

    companion object {
        val logger = LoggerFactory.getLogger(LocationService::class.java) !!

        val spider = Spider.create(AQICityPageProcessor()).thread(Constant.SPIDER_THREAD_COUNT) !!
    }

    /**
     * 获取所有省份
     * */
    open fun findProvinces(): List<Province> = locationDao.findAll(Province::class.java)

    /**
     * 获取省份
     * @param provinceId 省份的 id
     * */
    open fun getProvince(provinceId: Long)
            = locationDao.findById<Province>(Province::class.java,provinceId)

    /**
     * 获取所有城市
     * @param provinceId 省份的 id, 为0时返回所有城市
     * */
    open fun findCities(provinceId: Long = 0): List<City>?
            = if (provinceId == 0L) locationDao.findAll(City::class.java) else locationDao.findCities(provinceId)?.filterIsInstance<City>()

    /**
     * 获取一个城市
     * @param cityId 城市的 id
     * */
    open fun getCity(cityId: Long) = locationDao.findById(City::class.java, cityId)

    /**
     * 根据省份列表查询出城市列表
     * @param provinces 需要查询的省份列表
     * */
    open fun findCities(provinces: List<Province>)
            = mutableListOf<City>().apply {
        provinces.forEach { addAll(locationDao.findCities(it.id)?.filterIsInstance<City>() ?: listOf()) }
    }

    /**
     * 查询出所有的区
     * @param cityId 城市的 id
     * */
    open fun findDistricts(cityId: Long = 0)
            = if (cityId == 0L) locationDao.findAll(District::class.java) else locationDao.findDistricts(cityId)

    /**
     * 执行更新任务
     * */
    open fun execute() {
        try {
            task {
                updateProvincesFromWeb().apply {
                    forEach { locationDao save it }
                    updateCitiesFromWebByProvinces(this).apply {
                        forEach { locationDao save it }
                        // 获取 tianqi.com上的所有区县
                        updateDistrictsFromWebByCitites(this).apply {
                            // 给有 aqi 数据的区县添加pinyin_aqi
                            updateAQIDistricts(this)
                            // 从文件中筛选出中国天气网上有的区县
                            updateDistrictsFromFile(this)
                            val newIds = mutableListOf<Long>()
                            val saveDistricts = filter { it.id != 0L }
                            saveDistricts.apply {
                                forEach { newIds.add(it.id) }
                                locationDao batchDelete locationDao.findObsoleteDistricts(newIds)
                                updateDistrictViaAPI(this)
                                this.forEach { locationDao save it }
                            }
                        }
                    }
                }
                logger.info("Location Updated")
            }
        } finally {
            spider.scheduler = QueueScheduler()
        }
    }
} // end LocationService

/**
 * 通过网站 API获取省的信息, 并保存到数据库中
 * */
private val updateProvincesFromWeb: ()-> List<Province> = {
    var provinces = listOf<Province>()
    val params = HashMap<String, Any>()
    params.put("type", LocationData.LOCATION_INFO_TYPE_ZERO)
    // 获取原始的JSON数据, 结构为 List
    val data: String = fetchAndConvertDataFromWeb(params)
    // 通过 mapper 转换成 LocationInfo 对象
    val locationInfo = locationInfoParser(LocationData.LOCATION_INFO_TYPE_ZERO, data)
    // 将对象中的数据保存到数据库中
    if (locationInfo != null) {
        provinces = getProvincesFromLocationInfo(locationInfo)
    }

    LocationService.logger.info("Provinces Updated")
    provinces
}

/**
 * 通过网站API, 根据省爬取所有市的信息
 * */
@Contract("null->null")
private fun updateCitiesFromWebByProvinces(provinces: List<Province>): MutableList<City> {

    val cities = mutableListOf<City>()
    for ((id) in provinces) {
        val params = HashMap<String, Any>()
        params.put("type", LocationData.LOCATION_INFO_TYPE_ONE)
        params.put("pid", id.toString())
        val data = fetchAndConvertDataFromWeb(params)

        // 获取区域信息
        val locationInfo = locationInfoParser(LocationData.LOCATION_INFO_TYPE_ONE, data)
        if (locationInfo != null) {
            cities.addAll(getCitiesFromLocationInfo(locationInfo, id))
        }
    }
    LocationService.logger.info("Cities Updated")

    return cities
}

/**
 * 通过网站 API, 根据市爬去所有区县信息
 * */
@Contract("null->null")
private fun updateDistrictsFromWebByCitites(citites: List<City>): List<District> {
    val districts = mutableListOf<District>()
    for (city in citites) {
        val params = HashMap<String, Any>()
        params.put("type", LocationData.LOCATION_INFO_TYPE_TWO)
        params.put("pid", city.province)
        params.put("cid", city.id)
        val data = fetchAndConvertDataFromWeb(params)

        // 获取区域信息
        val locationInfo = locationInfoParser(LocationData.LOCATION_INFO_TYPE_TWO, data)
        if (locationInfo != null) {
            districts.addAll(getDistrictsFromLocationInfo(locationInfo, city.id))
        }
    }

    return districts
}

/**
 * 从获取到的区域信息中提取省信息并保存
 * */
private fun getProvincesFromLocationInfo(locationInfo: LocationInfo): List<Province> {
    val provinces = mutableListOf<Province>()
    val value = locationInfo.value.toSortedMap()
    val pys = locationInfo.py.toSortedMap()
    val ishots = locationInfo.ishot.toSortedMap()
    for ((k, v) in value) {
        with(Province()) {
            id = k
            // 去除字母前缀
            title = v?.split(" ")?.last() !!
            pinyin = pys[k] !!
            ishot = ishots[k] !!
            provinces.add(this)
        }

    }
    return provinces
}

/**
 * 从获取到的区域信息中提取城市信息并保存
 * */
private fun getCitiesFromLocationInfo(locationInfo: LocationInfo, provinceId: Long): List<City> {
    val value = locationInfo.value.toSortedMap()
    val pys = locationInfo.py.toSortedMap()
    val ishots = locationInfo.ishot.toSortedMap()

    val cities = mutableListOf<City>()
    for ((k, v) in value) {
        with(City()) {
            id = k
            title = v?.split(" ")?.last() !!
            pinyin = pys[k] !!
            ishot = ishots[k] !!
            province = provinceId
            cities.add(this)
        }
    }

    return cities
}

/**
 * 从获取到的区域信息中提取省信息并保存
 * */
private fun getDistrictsFromLocationInfo(locationInfo: LocationInfo, cityId: Long): List<District> {

    val districts = mutableListOf<District>()

    val value = locationInfo.value.toSortedMap()
    val pys = locationInfo.py.toSortedMap()
    val ishots = locationInfo.ishot.toSortedMap()
    for ((k, v) in value) {
        with(District()) {
            id = k
            city = cityId
            title = v?.split(" ")?.last() !!
            pinyin = pys[k] !!
            ishot = ishots[k] !!
            districts.add(this)
        }

    }
    return districts
}

/**
 * 获取数据并转换成正确的 json 格式
 * */
private fun fetchAndConvertDataFromWeb(params: HashMap<String, Any>): String {
    var data = HttpUtil.sendGetRequestViaHttpClient(Constant.LOCATION_SOURCE_URL, params, hashMapOf(), Charset.forName("utf-8"))
    data = data?.substring(data.indexOf('(') + 1, data.length - 1)?.removeSuffix(")")

    return data ?: throw BusinessException(BusinessError.ERROR_RESOURCE_NOT_FOUND)
}

/**
 * 到网上获取区县的 aqi pinyin
 *
 * 天气网有两套拼音,一套大多数天气通用的区县拼音, 第二套市空气质量板块专有的 pinyin, 需要区分使用
 *
 * */
private fun updateAQIDistricts(districts: List<District>) {
    val resultItems: ResultItems = LocationService.spider.get("http://www.tianqi.com/air/")

    val receivedUrls: List<String> = resultItems.get("urls")
    val titles: List<String> = resultItems.get("titles")
    val pinyins = receivedUrls.map { it.substringAfter("/air/").removeSuffix(".html") }

    for (district in districts) {
        titles.forEachIndexed { index, it ->
            if (it.contains(district.title.split(" ").last())) {
                district.pinyin_aqi = pinyins[index]
            }
        }
    }
}

private fun updateDistrictViaAPI(districts: List<District>) {
    districts.forEach {
        try {
            val resultBean = getResultBean(it)
            val c = resultBean?.c
            if (c != null) {
                it.longitude = c.c13
                it.latitude = c.c14
                it.zipcode = c.c12
                it.altitude = c.c15.toDouble()
            }
        } catch(ex: Exception) {
            ex.printStackTrace()
        }
    }
}

/**
 * 从文件中更新区县信息
 *
 * 文件来源为中国天气网, 所有基本信息以中国天气网为准
 * */
private fun updateDistrictsFromFile(districts: List<District>) {

    data class TempDistrict(val id: Long, val title: String, val city: String, val province: String)

    val strList = mutableListOf<String>()
    try {
        val br: BufferedReader
        val reader: InputStreamReader
        reader = InputStreamReader(LocationService::class.java.getResourceAsStream(Constant.DISTRICT_API_FILE_LOCATION), "UTF-8")
        br = BufferedReader(reader)
        do {
            val line = br.readLine()
            if (line.trim().isNotEmpty()) {
                strList.add(line)
            }
        }
        while (line != null)
        reader.close()
        br.close()
    } catch(ex: Exception) {
        ex.printStackTrace()
        throw ex
    }

    val tempDistricts = mutableListOf<TempDistrict>()
    for (str in strList) {
        if (str.trim().isNotEmpty()) {
            val infoList = str.trim().split(",")
            tempDistricts.add(TempDistrict(infoList[0].toLong(), infoList[1], infoList[2], infoList[3]))
        }
    }

    districts.forEachIndexed { index, district ->
        val tempDistrict = tempDistricts.firstOrNull { it.id == district.id }
        if (tempDistrict == null) {
            district.id = 0L
        } else {
            district.title = tempDistrict.title
        }
    }
}

