package com.vito.work.weather.domain.services

import com.vito.work.weather.domain.config.Constant
import com.vito.work.weather.domain.config.getAQITypeCodeByName
import com.vito.work.weather.domain.daos.AQIDao
import com.vito.work.weather.domain.daos.LocationDao
import com.vito.work.weather.domain.daos.StationAQIDao
import com.vito.work.weather.domain.daos.StationDao
import com.vito.work.weather.domain.entities.AQI
import com.vito.work.weather.domain.entities.District
import com.vito.work.weather.domain.entities.Station
import com.vito.work.weather.domain.entities.StationAQI
import com.vito.work.weather.domain.services.spider.AQIViewPageProcessor
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
open class AQIService: SpiderTask()
{

    @Resource
    lateinit var aqiDao: AQIDao
    @Resource
    lateinit var stationAQIDao: StationAQIDao
    @Resource
    lateinit var stationDao: StationDao
    @Resource
    lateinit var locationDao: LocationDao

    @PreDestroy
    open fun destroy()
    {
        spider.close()
    }

    companion object
    {
        var spider: Spider = Spider.create(AQIViewPageProcessor())
                .thread(Constant.SPIDER_THREAD_COUNT)

        val logger = LoggerFactory.getLogger(AQIService::class.java)
    }

    open fun findLatestAQI(districtId: Long): AQI?
    {
        val result = aqiDao.findLatestByDistrict(districtId)
        return result
    }

    open fun execute(){
        try {
            task(){
                val districts = locationDao.findAQIDistrict()
                districts?.forEach {
                    val targetUrl = AQIViewUrlBuilder(Constant.AQI_BASE_URL, it.pinyin_aqi)
                    val webData = fetchDataViaSpider(targetUrl, it)
                    if(webData != null){
                        saveWebdata(webData)
                    }
                }
            }
        }finally {
            spider.scheduler = QueueScheduler()
        }
    }

    /**
     * 保存天气预报的 list
     * @param weathers      待保存的所有天气
     * @param district      待保存的天气所属的区县
     *
     * 保存前需要先查出数据库中是否有对应的天气, 根据区县和日期判断是否存在旧项
     *  若有, 则更新旧项
     *  没有, 则保存为新项
     * */
    open fun saveWebdata(webData: WebData)
    {

        val aqi = webData.aqi
        val stationAQIs = webData.stationAQIs
        val stations = webData.stations

        val stationNames = mutableListOf<String>()
        stations.forEach { stationNames.add(it.name_zh) }

        val savedStations = stationDao.findByNames(stationNames) ?: mutableListOf()

        // 不存在则保存
        stations.forEach { iw ->
            val station = savedStations.firstOrNull { iw.name_zh == it.name_zh }
            if (station == null)
            {
                savedStations.add(iw)
            }
            else
            {
                station.district = iw.district
                station.name_en = iw.name_en
                station.name_zh = iw.name_zh
                savedStations.add(station)
            }
        }

        aqiDao saveOrUpdate aqi

        savedStations.forEach { stationDao saveOrUpdate it }

        stationAQIs.forEach { iw -> iw.station = savedStations.firstOrNull { iw.station_name == it.name_zh }?.id ?: 0L }

        stationAQIs.forEach { stationAQIDao saveOrUpdate it }

    }

    open fun findStationAQI(districtId: Long): List<StationAQI>?
    {
        val stations = stationDao.findByDistrict(districtId) ?: mutableListOf()
        val ids = mutableListOf<Long>()
        stations.forEach { ids.add(it.id) }
        val result = stationAQIDao.findLatestByStations(ids)
        result?.forEach { id -> stations.forEach { if (it.id == id.station) id.station_name = it.name_zh } }
        return result
    }
}

/**
 * URL Builder
 * */
private fun AQIViewUrlBuilder(baseUrl: String, districtPinyin: String): String
{
    val urlSuffix = ".html"

    val urlBuffer: StringBuffer = StringBuffer()
    if (! baseUrl.startsWith("http://") && ! baseUrl.startsWith("https://"))
    {
        urlBuffer.append("http://")
    }

    urlBuffer.append(baseUrl)
    urlBuffer.append(districtPinyin)
    urlBuffer.append(urlSuffix)

    return urlBuffer.toString()
}

private fun fetchDataViaSpider(targetUrl: String, district: District): WebData?
{

    var resultItems: ResultItems = ResultItems()
    val stationAqis = mutableListOf<StationAQI>()
    val aqi = AQI()
    val stations = mutableListOf<Station>()

    try
    {
        resultItems = AQIService.spider.get(targetUrl)
        if (resultItems.request == null)
        {
            throw Exception("Request Can't Be Null")
        }
    }
    catch(ex: Exception)
    {
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

        for ((index, stationName) in stationNames.withIndex())
        {
            val stationAQI = StationAQI()
            val station = Station()
            with(station) {
                name_zh = stationName.toString()
                name_en = station_urls[index].toString()
                this.district = district.id
            }
            stations.add(station)

            with(stationAQI) {
                station_name = stationName.toString()
                this.value = parseIntegerData(station_values[index])
                PM25 = parseIntegerData(station_pm25[index])
                O3 = parseIntegerData(station_o3[index])
                major = getAQITypeCodeByName(station_primary[index].toString()).code
                datetime = Timestamp.valueOf(LocalDateTime.now())
            }
            stationAqis.add(stationAQI)
        }

    }

    val result = WebData(stationAqis, aqi, stations)

    return result
}

private fun parseIntegerData(data: String): Int
{
    try
    {
        return parseInt(data.split("：").last().replace("μg/m3", "").trim())
    }
    catch(ex: Exception)
    {
        return - 1
    }
}

data class WebData(
        val stationAQIs: List<StationAQI>,
        val aqi: AQI,
        val stations: List<Station>
                  )