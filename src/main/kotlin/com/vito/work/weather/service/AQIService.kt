package com.vito.work.weather.service

import com.vito.work.weather.config.Constant
import com.vito.work.weather.config.getAQITypeCodeByName
import com.vito.work.weather.dto.AQI
import com.vito.work.weather.dto.District
import com.vito.work.weather.dto.Station
import com.vito.work.weather.dto.StationAQI
import com.vito.work.weather.repo.AQIDao
import com.vito.work.weather.repo.DistrictDao
import com.vito.work.weather.repo.StationAQIDao
import com.vito.work.weather.repo.StationDao
import com.vito.work.weather.service.spider.AQIViewPageProcessor
import com.vito.work.weather.service.spider.AbstractSpiderTask
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import us.codecraft.webmagic.ResultItems
import us.codecraft.webmagic.Spider
import us.codecraft.webmagic.scheduler.QueueScheduler
import us.codecraft.webmagic.selector.PlainText
import java.lang.Integer.parseInt
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import javax.annotation.PreDestroy
import javax.annotation.Resource
import javax.transaction.Transactional

/**
 * Created by lingzhiyuan.
 * Date : 16/4/17.
 * Time : 下午4:15.
 * Description:
 *
 */

@Service
@Transactional
class AQIService : AbstractSpiderTask() {

    @Resource
    lateinit var aqiDao: AQIDao
    @Resource
    lateinit var stationAQIDao: StationAQIDao
    @Resource
    lateinit var stationDao: StationDao
    @Resource
    lateinit var districtDao: DistrictDao

    @PreDestroy
    fun destroy() {
        spider.close()
    }

    companion object {
        var spider: Spider = Spider.create(AQIViewPageProcessor())
                .thread(Constant.SPIDER_THREAD_COUNT)

        val logger = LoggerFactory.getLogger(AQIService::class.java)
    }

    fun findLatestAQI(districtId: Long): AQI? {
        return aqiDao.findLatestByDistrict(districtId)
    }

    fun execute(){
        try {
            task {
                val districts = districtDao.findAQIDistrict()
                districts.forEach {
                    val targetUrl = AQIViewUrlBuilder(Constant.AQI_BASE_URL, it.pinyin_aqi)
                    val webData = fetchDataViaSpider(targetUrl, it)
                    if (webData != null) {
                        saveWebdata(webData)
                    }
                }
            }
        } finally {
            spider.scheduler = QueueScheduler()
        }
    }

    /**
     * 保存天气预报的 list
     * 保存前需要先查出数据库中是否有对应的天气, 根据区县和日期判断是否存在旧项
     *  若有, 则更新旧项
     *  没有, 则保存为新项
     * */
    fun saveWebdata(webData: WebData) {
        val aqi = webData.aqi
        val stationAQIs = webData.stationAQIs
        val stations = webData.stations
        val stationNames = stations.map { it.name_zh }
        val savedStations = stationDao.findByNames(stationNames).toMutableList()

        // 不存在则保存
        stations.forEach { iw ->
            with(iw){
                savedStations.firstOrNull { name_zh == it.name_zh }.apply {
                    if (this == null) {
                        savedStations.add(iw)
                    } else {
                        district = district
                        name_en = name_en
                        name_zh = name_zh
                        savedStations.add(this)
                    }
                }
            }
        }
        aqiDao save aqi
        savedStations.forEach { stationDao save it }
        stationAQIs.forEach { iw -> iw.station = savedStations.firstOrNull { iw.station_name == it.name_zh }?.id ?: 0L }
        stationAQIs.forEach { stationAQIDao save it }
    }

    fun findStationAQI(districtId: Long) {
        val stations = stationDao.findByDistrict(districtId)
        val ids = stations.map { it.id }
        val result = stationAQIDao.findLatestByStations(ids)
        result.map {  }
        result.forEach { id -> stations.forEach { if (it.id == id.station) id.station_name = it.name_zh } }
    }
}

/**
 * URL Builder
 * */
private val AQIViewUrlBuilder: (String, String) -> String = {
    baseUrl, districtPinyin ->
    val urlSuffix = ".html"

    val urlBuffer: StringBuffer = StringBuffer()
    if (! baseUrl.startsWith("http://") && ! baseUrl.startsWith("https://")) {
        urlBuffer.append("http://")
    }

    urlBuffer.append(baseUrl)
    urlBuffer.append(districtPinyin)
    urlBuffer.append(urlSuffix)

    urlBuffer.toString()
}

private fun fetchDataViaSpider(targetUrl: String, district: District): WebData? {

    val resultItems: ResultItems
    val stationAQIs = mutableListOf<StationAQI>()
    val aqi = AQI()
    val stations = mutableListOf<Station>()

    try {
        resultItems = AQIService.spider.get(targetUrl)
        if (resultItems.request == null) {
            throw Exception("Request Can't Be Null")
        }
    } catch(ex: Exception) {
        ex.printStackTrace()
        return null
    }

    with(resultItems) {

        val value: PlainText = get("aqi_value")

        aqi.district = district.id
        aqi.value = parseIntegerData(value.toString())
        aqi.date = Date.valueOf(LocalDate.now())
        aqi.update_time = Timestamp.valueOf(LocalDateTime.now())

        val stationNames: List<String> = get("stations")
        val station_urls: List<String> = get("station_urls")
        val station_values: List<String> = get("station_values")
        val station_pm25: List<String> = get("station_pm25")
        val station_o3: List<String> = get("station_o3")
        val station_primary: List<String> = get("station_primary")

        for ((index, stationName) in stationNames.withIndex()) {
            val stationAQI = StationAQI()
            val station = Station()
            with(station) {
                name_zh = stationName
                name_en = station_urls[index]
                this.district = district.id
            }
            stations.add(station)

            with(stationAQI) {
                station_name = stationName
                this.value = parseIntegerData(station_values[index])
                PM25 = parseIntegerData(station_pm25[index])
                O3 = parseIntegerData(station_o3[index])
                major = getAQITypeCodeByName(station_primary[index]).code
                datetime = Timestamp.valueOf(LocalDateTime.now())
            }
            stationAQIs.add(stationAQI)
        }

    }

    val result = WebData(stationAQIs, aqi, stations)

    return result
}

private val parseIntegerData:(String)-> Int = {
    data ->
    try {
        parseInt(data.split("：").last().replace("μg/m3", "").trim())
    } catch(ex: Exception) {
        - 1
    }
}

data class WebData(
        val stationAQIs: List<StationAQI>,
        val aqi: AQI,
        val stations: List<Station>
                  )