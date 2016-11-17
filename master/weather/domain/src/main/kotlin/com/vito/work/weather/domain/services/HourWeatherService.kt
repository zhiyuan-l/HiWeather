package com.vito.work.weather.domain.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.vito.work.weather.domain.config.Constant
import com.vito.work.weather.domain.daos.HourWeatherDao
import com.vito.work.weather.domain.entities.City
import com.vito.work.weather.domain.entities.District
import com.vito.work.weather.domain.entities.HourWeather
import com.vito.work.weather.domain.services.spider.CnWeather24ViewPageProcessor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import us.codecraft.webmagic.ResultItems
import us.codecraft.webmagic.Spider
import us.codecraft.webmagic.selector.PlainText
import java.lang.Integer.parseInt
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.annotation.PreDestroy
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/16.
 * Time : 上午11:21.
 * Description:
 *
 * 管理以小时为单位的天气
 *
 */

@Service("hourWeatherService")
@Transactional
open class HourWeatherService: UseLock(), SpiderTask
{

    @Resource
    lateinit var hourWeatherDao: HourWeatherDao

    @PreDestroy
    open fun destroy()
    {
        spider.close()
    }

    companion object
    {
//        var spider: Spider = Spider.create(TodayViewPageProcessor()).thread(Constant.SPIDER_THREAD_COUNT)
        var spider: Spider = Spider.create(CnWeather24ViewPageProcessor()).thread(Constant.SPIDER_THREAD_COUNT)

        val logger = LoggerFactory.getLogger(HourWeatherService::class.java)
    }

    /**
     * 根据时间和区县找出一个节点的天气
     *  一个节点的时间精确到小时, 小时以下为0
     * */
    open fun findHourWeather(district: District, dateTime: LocalDateTime): HourWeather?
    {

        val hourWeather: HourWeather? = hourWeatherDao.findByDistrictDateTime(district.id, dateTime)

        return hourWeather !!
    }

    /**
     * 查出最新的二十四小时天气预报
     * */
    open fun find24HWeather(districtId: Long): List<HourWeather>?
    {
        val list = hourWeatherDao.find24HByDistrict(districtId)
        return list
    }

    /**
     * 爬虫更新的入口
     *
     * 首先组装 url
     * 再根据 url 从网上获取数据并组装成 HourWeather 对象数组
     * 最后保存所有的结果
     *
     * @param   city        区县所属的城市
     * @param   district    需要更新的区县
     *
     * @return  hws     获取到并保存成功的结果集
     * */
    open fun updateFromWeb(city: City, district: District)
    {
        val targetUrl = urlBuilder( Constant.CNWEATHER_24H_API_BASE_URL,district.id)//todayViewUrlBuilder(Constant.TODAY_BASE_URL, city.pinyin, district)
        try
        {
            lock()
            val hws = fetchDataFromCnWeather(targetUrl, district)
            saveHourWeather(hws)
        }
        catch(ex: Exception)
        {
            throw ex
        }
        finally
        {
            unlock()
        }
    }

    override fun executeTask() {

    }


    /**
     * 保存所有的 HourWeather
     *
     * 1. 首先通过时间和区县 id 获取已保存的天气数据
     * 2. 遍历需要保存的数据, 如果在已保存的天气数据数组中找到相同时间和区县 id 的数据, 则将新数据复制到旧的已保存的项中, 然后添加到待保存数组中
     * 3. 如果没有找到相同的项, 则直接放到待保存数组中, 将保存为新项
     *
     * 以上操作是为了保证一个区县在一个时间点（精确到小时）只有一条数据
     *
     * */
    open fun saveHourWeather(hourWeathers: List<HourWeather>)
    {
        val savedWeathers: MutableList<HourWeather> = mutableListOf()
        val datetimes = mutableListOf<Timestamp>()
        hourWeathers.forEach { datetimes.add(it.datetime) }

        savedWeathers.addAll(hourWeatherDao.findByDistrictDatetimes(hourWeathers[0].district, datetimes) ?: listOf())

        val weathersToSave = mutableListOf<HourWeather>()

        for (hw in hourWeathers)
        {
            val temp = savedWeathers.firstOrNull {
                it.district == hw.district && it.datetime == hw.datetime
            }

            if (temp == null)
            {
                // 若不存在相同时间,相同区域的天气, 则保存为新项
                weathersToSave.add(hw)
            }
            else
            {
                with(temp){
                    // 否则, 更新属性
                    aqi = hw.aqi
                    humidity = hw.humidity
                    temperature = hw.temperature
                    precipitation = hw.precipitation
                    update_time = hw.update_time
                }

                weathersToSave.add(temp)
            }
        }

        for (it in weathersToSave)
        {
            hourWeatherDao.saveOrUpdate(it)
        }
    }

}

/**
 * URL Builder For CN Weather
 * */
private fun urlBuilder(baseUrl: String, districtId: Long): String
{
    return baseUrl.replace("districtId", districtId.toString())
}

/**
 * 从中国天气网的接口获取24小时的天气数据
 * */
private fun fetchDataFromCnWeather(targetUrl: String, district: District): List<HourWeather>
{

    val hws = mutableListOf<HourWeather>()
    val resultItems: ResultItems = HourWeatherService.spider.get(targetUrl)

    with(resultItems){
        val ptime: PlainText = get<PlainText>("ptime")
        val hours: List<String> = get("hours")
        val temps: List<String> = get("temps")
        val wds: List<String> = get("wds")
        val wfs: List<String> = get("wfs")
        val preds: List<String> = get("preds")
        val humis: List<String> = get("humis")

        val startTime = LocalDateTime.parse(ptime.firstSourceText, DateTimeFormatter.ofPattern("yy-MM-dd HH:mm"))
        hours.forEachIndexed { index, hour ->

            val weather = HourWeather()
            with(weather){
                this.district = district.id
                precipitation = (if(preds[index] != "") preds[index] else "-1").toDouble()
                humidity = (if(humis[index] != "") humis[index] else "-1").toInt()
                wind_direction = (if(wds[index] != "") wds[index] else "-1").toInt()
                wind_force = (if(wfs[index] != "") wfs[index] else "-1").toInt()
                datetime = Timestamp.valueOf(startTime.plusHours(index.toLong()))
                temperature = (if(temps[index] != "") temps[index] else "-273").toInt()
            }

            hws.add(weather)
        }
    }

    return hws
}


// 以下两个方法用于从 tianqi.com上爬取数据,已废弃

/**
 * URL Builder
 *
 * 将城市的拼音和区县拼音组装成可达的页面 URL
 *
 * @example http://beijing.tianqi.com/beijing/today/
 *
 * @param   baseUrl 统一的 url
 * @param   cityPinyin 所属城市的拼音
 * @param   district    所属的区县
 *
 * */
private fun todayViewUrlBuilder(baseUrl: String, cityPinyin: String, district: District): String
{

    val urlBuffer: StringBuffer = StringBuffer()

    with(urlBuffer){
        if (! baseUrl.startsWith("http://") && ! baseUrl.startsWith("https://"))
        {
            append("http://")
        }
        append(baseUrl.replace("city", cityPinyin).replace("district", district.pinyin))
    }

    return urlBuffer.toString()
}


/**
 * 通过爬虫从天气网上爬取一个网页的数据并转换成 HourWeather的 List, 然后返回获得数据
 *
 * 获得的数据为五个字符串, 每个字符串都是二十四小时单项数据的集合, 形式为JSON数组, [1,2,3...]
 * 1. tdate     天气信息对应的时间, 当天的小时, 包含在 span 中
 * 2. tvalue    温度
 * 3. pvalue    降水量
 * 4. hvalue    湿度
 * 5. aqi       空气质量指数
 *
 * 获取到的JSON 数组同果 Jackson 的 ObjectMapper 映射成 Array
 * 然后组装成结果 list
 *
 * @param   targetUrl   需要爬取的目标页面
 * @param   district    页面对应的区县
 *
 * @return  hws     获得的结果list
 * */
private fun fetchDataViaSpider(targetUrl: String, district: District): List<HourWeather>
{

    val resultItems: ResultItems?
    val hws = mutableListOf<HourWeather>()

    try
    {
        resultItems = HourWeatherService.spider.get(targetUrl)
    }
    catch(ex: Exception)
    {
        throw ex
    }

    if (resultItems == null || resultItems.request == null)
        throw Exception("Request Can't Be Null")

    val tdate: String = resultItems["tdate"]
    val tvalue: String = resultItems["tvalue"]
    val pvalue: String = resultItems["pvalue"]
    val hvalue: String = resultItems["hvalue"]
    val aqi: String = resultItems["aqi"]

    val mapper: ObjectMapper = ObjectMapper()
    val dateSpans = mapper.readValue(tdate.toString(), Array<String>::class.java)
    val tvalues = mapper.readValue(tvalue, Array<Int>::class.java)
    val pvalues = mapper.readValue(pvalue, Array<Double>::class.java)
    val hvalues = mapper.readValue(hvalue, Array<Int>::class.java)
    val aqis = mapper.readValue(aqi, Array<Int>::class.java)
    val now = LocalDateTime.now()
    val startTime = LocalDateTime.of(now.year, now.monthValue, now.dayOfMonth, parseInt(dateSpans[0].substringAfter(">").substringBefore("</span>")), 0, 0, 0)
    for ((index, span) in dateSpans.withIndex())
    {
        val hw: HourWeather = HourWeather()
        hw.district = district.id
        hw.datetime = Timestamp.valueOf(startTime.plusHours(index.toLong()))
        hw.precipitation = pvalues[index]
        hw.humidity = hvalues[index]
        hw.temperature = tvalues[index]
        hw.aqi = aqis[index]
        hws.add(hw)
    }

    return hws
}
