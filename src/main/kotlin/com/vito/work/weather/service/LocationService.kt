package com.vito.work.weather.service

import com.vito.work.weather.config.Constant
import com.vito.work.weather.domain.beans.api.LocationData
import com.vito.work.weather.domain.beans.api.LocationData.Companion.LOCATION_INFO_TYPE_ONE
import com.vito.work.weather.domain.beans.api.LocationData.Companion.LOCATION_INFO_TYPE_TWO
import com.vito.work.weather.domain.beans.api.LocationData.Companion.LOCATION_INFO_TYPE_ZERO
import com.vito.work.weather.domain.beans.api.LocationInfo
import com.vito.work.weather.domain.beans.api.locationInfoParser
import com.vito.work.weather.dto.City
import com.vito.work.weather.dto.District
import com.vito.work.weather.dto.Province
import com.vito.work.weather.repo.CityDao
import com.vito.work.weather.repo.DistrictDao
import com.vito.work.weather.repo.ProvinceDao
import com.vito.work.weather.service.spider.AQICityPageProcessor
import com.vito.work.weather.service.spider.AbstractSpiderTask
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
class LocationService : AbstractSpiderTask() {

    @Resource
    lateinit var districtDao: DistrictDao
    @Resource
    lateinit var cityDao: CityDao
    @Resource
    lateinit var provinceDao: ProvinceDao

    @PreDestroy
    fun destroy() {
        spider.close()
    }

    companion object {
        val logger = LoggerFactory.getLogger(LocationService::class.java) !!

        val spider = Spider.create(AQICityPageProcessor()).thread(Constant.SPIDER_THREAD_COUNT) !!
    }

    /**
     * 获取所有省份
     * */
    fun findProvinces(): List<Province> = provinceDao.findAll(Province::class.java)

    /**
     * 获取省份
     * @param provinceId 省份的 id
     * */
    fun getProvince(provinceId: Long)
            = provinceDao.findById(Province::class.java, provinceId)

    /**
     * 获取所有城市
     * @param provinceId 省份的 id, 为0时返回所有城市
     * */
    fun findCities(provinceId: Long = 0): List<City>?
            = if (provinceId == 0L) cityDao.findAll(City::class.java) else cityDao.findCities(provinceId)

    /**
     * 获取一个城市
     * @param cityId 城市的 id
     * */
    fun getCity(cityId: Long) = cityDao.findById(City::class.java, cityId)

    /**
     * 根据省份列表查询出城市列表
     * @param provinces 需要查询的省份列表
     * */
    fun findCities(provinces: List<Province>) = provinces
            .map { it.id }
            .map { cityDao.findCities(it) }
            .reduce { acc, list -> acc + list }

    /**
     * 查询出所有的区
     * @param cityId 城市的 id
     * */
    fun findDistricts(cityId: Long = 0)
            = if (cityId == 0L) districtDao.findAll(District::class.java) else districtDao.findDistricts(cityId)

    fun updateProvince() {
        val provinces = updateProvincesFromWeb()
        provinces.forEach { provinceDao save it }
    }

    fun updateCity() {
        val provinces = provinceDao.findAll(Province::class.java)
        val cities = updateCitiesFromWebByProvinces(provinces)
        cities.forEach { cityDao save it }
    }

    fun updateDistrict() {
        val cities = cityDao.findAll(City::class.java)
        // 获取 tianqi.com上的所有区县
        updateDistrictsFromWebByCities(cities).run {
            // 给有 aqi 数据的区县添加pinyin_aqi
            updateAQIDistricts(this).let {
                // 从文件中筛选出中国天气网上有的区县
                updateDistrictsFromFile(this).forEach { districtDao save it }
            }
        }
    }

    fun updateDistrictViaAPI() {

        val districts = districtDao.findAll(District::class.java)
        districts.apply {
            districtDao batchDelete districtDao.findObsoleteDistricts(districts.map { it.id })
            com.vito.work.weather.service.updateDistrictViaAPI(this)
            this.forEach { districtDao save it }
        }
    }

    /**
     * 执行更新任务
     * */
    fun execute(type: Int) {
        try {
            task {
                when (type) {
                    LOCATION_INFO_TYPE_ZERO ->
                        updateProvince()
                    LOCATION_INFO_TYPE_ONE  ->
                        updateCity()
                    LOCATION_INFO_TYPE_TWO  ->
                        updateDistrict()
                    3  ->
                       updateDistrictViaAPI()
                }
            }
        } finally {
            spider.scheduler = QueueScheduler()
        }
    }
} // end LocationService

/**
 * 通过网站 API获取省的信息, 并保存到数据库中
 * */
private val updateProvincesFromWeb: () -> List<Province> = {
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
private fun updateCitiesFromWebByProvinces(provinces: List<Province>): List<City> {

    val cities = provinces
            .map { mapOf("type" to LocationData.LOCATION_INFO_TYPE_ONE, "pid" to it.id) }
            .map(::fetchAndConvertDataFromWeb)
            .map { locationInfoParser(LocationData.LOCATION_INFO_TYPE_ONE, it) }
            .filterNotNull()
            .map(::getCitiesFromLocationInfo)
            .reduce { acc, list -> acc + list }

    LocationService.logger.info("Cities Updated")
    return cities
}

/**
 * 通过网站 API, 根据市爬去所有区县信息
 * */
@Contract("null->null")
private fun updateDistrictsFromWebByCities(cities: List<City>): List<District> {

    val districts = cities
            .map { mapOf("type" to LocationData.LOCATION_INFO_TYPE_TWO, "pid" to it.province, "cid" to it.id) }
            .map(::fetchAndConvertDataFromWeb)
            .map { locationInfoParser(LocationData.LOCATION_INFO_TYPE_TWO, it) }
            .filterNotNull()
            .map(::getDistrictsFromLocationInfo)
            .reduce { acc, list -> acc + list }

    return districts
}

/**
 * 从获取到的区域信息中提取省信息并保存
 * */
private fun getProvincesFromLocationInfo(locationInfo: LocationInfo): List<Province> {
    val value = locationInfo.value.toSortedMap()
    val pys = locationInfo.py.toSortedMap()
    val ishots = locationInfo.ishot.toSortedMap()
    val provinces = value.map {
        Province().apply {
            id = it.key
            title = it.value?.split(" ")?.last() !!
            pinyin = pys[it.key] !!
            ishot = ishots[it.key] !!
        }
    }
    return provinces
}

/**
 * 从获取到的区域信息中提取城市信息并保存
 * */
private fun getCitiesFromLocationInfo(locationInfo: LocationInfo): List<City> {
    val value = locationInfo.value.toSortedMap()
    val pys = locationInfo.py.toSortedMap()
    val ishots = locationInfo.ishot.toSortedMap()
    val cities = value.map {
        City().apply {
            id = it.key
            title = it.value?.split(" ")?.last() !!
            pinyin = pys[it.key] !!
            ishot = ishots[it.key] !!
            province = id / 100
        }
    }
    return cities
}

/**
 * 从获取到的区域信息中提取省信息并保存
 * */
private fun getDistrictsFromLocationInfo(locationInfo: LocationInfo): List<District> {
    val value = locationInfo.value.toSortedMap()
    val pys = locationInfo.py.toSortedMap()
    val ishots = locationInfo.ishot.toSortedMap()
    val districts = value.map { it ->
        District().apply {
            id = it.key
            city = (it.key - 101000000 - (it.key - 101000000) % 100) / 100
            title = it.value?.split(" ")?.last() !!
            pinyin = pys[it.key] !!
            ishot = ishots[it.key] !!
        }
    }
    return districts
}

/**
 * 获取数据并转换成正确的 json 格式
 * */
private fun fetchAndConvertDataFromWeb(params: Map<String, Any>): String {
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
private fun updateAQIDistricts(districts: List<District>): List<District> {
    val resultItems: ResultItems = LocationService.spider.get("http://www.tianqi.com/air/")

    val receivedUrls: List<String> = resultItems.get("urls")
    val titles: List<String> = resultItems.get("titles")
    val pinyins = receivedUrls.map { it.substringAfter("/air/").removeSuffix(".html") }

    districts.forEach { district ->
        titles.forEachIndexed { index, title ->
            if (title.contains(district.title.split(" ").last())) {
                district.pinyin_aqi = pinyins[index]
            }
        }
    }

    return districts
}

/**
 * 通过中国天气网的API更新信息
 * */
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
private fun updateDistrictsFromFile(districts: List<District>): List<District> {

    data class TempDistrict(val id: Long, val title: String, val city: String, val province: String)

    val strList = mutableListOf<String>()
    try {
        val br: BufferedReader
        val reader: InputStreamReader = InputStreamReader(LocationService::class.java.getResourceAsStream(Constant.DISTRICT_API_FILE_LOCATION), "UTF-8")
        br = BufferedReader(reader)
        do {
            val line = br.readLine()
            if (! line.isNullOrBlank() && line.trim().isNotEmpty()) {
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

    val tempDistricts = strList
            .filter { it.trim().isNotEmpty() }
            .map { it.trim().split(",") }
            .map { TempDistrict(it[0].toLong(), it[1], it[2], it[3]) }

    districts.forEach { district ->
        val tempDistrict = tempDistricts.firstOrNull { it.id == district.id }
        if (tempDistrict == null) {
            // id 为 -1的项目为无效项目
            district.id = -1L
        } else {
            district.title = tempDistrict.title
        }
    }

    return districts.filter { it.id != -1L }
}

