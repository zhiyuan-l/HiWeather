package com.vito.work.weather.domain.services

import com.vito.work.weather.domain.config.Constant
import com.vito.work.weather.domain.daos.HistoryWeatherDao
import com.vito.work.weather.domain.daos.LocationDao
import com.vito.work.weather.domain.entities.City
import com.vito.work.weather.domain.entities.HistoryWeather
import com.vito.work.weather.domain.services.spider.MonthViewPageProcessor
import com.vito.work.weather.domain.util.http.BusinessError
import com.vito.work.weather.domain.util.http.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import us.codecraft.webmagic.ResultItems
import us.codecraft.webmagic.Spider
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import javax.annotation.PreDestroy

/**
 * Created by lingzhiyuan.
 * Date : 16/4/5.
 * Time : 下午7:55.
 * Description:
 *
 */

@Service("historyWeatherService")
@Transactional
open class HistoryWeatherService @Autowired constructor(val historyWeatherDao: HistoryWeatherDao, val locationDao: LocationDao)
{

    @PreDestroy
    open fun destroy()
    {
        spider.close()
        logger.info("History Spider Stopped")
    }

    companion object
    {
        // 只使用一个 spider, 一个线程池
        var spider: Spider = Spider.create(MonthViewPageProcessor())
                .thread(Constant.SPIDER_THREAD_COUNT)

        val logger = LoggerFactory.getLogger(HistoryWeatherService::class.java)
    }

    /**
     * 根据日期找到一个历史项, 找不到则抛出资源未找到的异常
     * */
    open fun findHistoryWeather(city: City, date: LocalDate): HistoryWeather?
    {
        var weather = historyWeatherDao.findByCityDate(city.id, Date.valueOf(date))
        return weather
    }

    /**
     * 更新历史天气的入口
     *
     * 执行更新和和保存操作
     * */
    open fun updateFromWeb(city: City, date: LocalDate)
    {
        var targetUrl = monthViewUrlBuilder(Constant.HISTORY_WEATHER_BASE_URL, city.pinyin, date)
        try
        {
            val hws = fetchDataViaSpider(targetUrl, city)
            saveHistoryWeather(hws, city)
        }
        catch(ex: Exception)
        {
            throw ex
        }
    }

    /**
     * 执行保存历史天气的操作
     * */
    open fun saveHistoryWeather(weathers: List<HistoryWeather>, city: City)
    {
        var dates = mutableListOf<Date>()
        weathers.forEach { dates.add(it.date) }
        var savedWeathers: MutableList<HistoryWeather> = mutableListOf()
        var temp = historyWeatherDao.findByCityDates(city.id, dates)
        if (temp != null)
        {
            savedWeathers.addAll(temp)
        }
        weathers.forEach { iw ->
            val t = savedWeathers.firstOrNull() { it -> it.city == iw.city && it.date == iw.date }
            if (t == null )
            {
                savedWeathers.add(iw)
            }
            else
            {
                t.max = iw.max
                t.min = iw.min
                t.wind_direction = iw.wind_direction
                t.wind_force = iw.wind_force
                t.weather = iw.weather
                t.update_time = iw.update_time
            }
        }

        savedWeathers.forEach { historyWeatherDao.saveOrUpdate(it) }
    }

    open fun findHistoryWeathersOfToday(cityId: Long): List<HistoryWeather>
    {
        var now = LocalDate.now()

        var dates = mutableListOf<Date>()
        for(year in Constant.SPIDER_HISTORY_START_DATE.year..now.year)
        {
            dates.add(Date.valueOf(LocalDate.of(year, now.monthValue, now.dayOfMonth)))
        }

        var result = historyWeatherDao.findByCityDates(cityId, dates) ?: throw BusinessException(BusinessError.ERROR_RESOURCE_NOT_FOUND)

        return result
    }

    open fun findHistoryTops(cityId: Long): List<HistoryWeather>
    {

        var result = mutableListOf<HistoryWeather>()

        var maxTempWeather = historyWeatherDao.findHighestTemperature(cityId) ?: HistoryWeather()
        var minTempWeather = historyWeatherDao.findLowestTemperature(cityId) ?: HistoryWeather()

        result.add(maxTempWeather)
        result.add(minTempWeather)

        return result
    }

    /**
     * 根据城市的 id查找某一天的历史天气
     * */
    open fun findByDate(cityId: Long,date: LocalDate): HistoryWeather?
    {
        var weather = historyWeatherDao.findByCityDate(cityId, Date.valueOf(date))
        return weather
    }

    /**
     * 根据城市的 id 查出某一个月所有的历史天气
     * */
    open fun findByMonth(cityId: Long, date: LocalDate): List<HistoryWeather>?
    {
        var dates = mutableListOf<Date>()

        // 这个月的起始日期
        var startDate = date.minusDays(date.dayOfMonth.toLong()-1)
        for(offset in 0..date.lengthOfMonth()-1)
        {
            dates.add(Date.valueOf(startDate.plusDays(offset.toLong())))
        }

        var list: List<HistoryWeather>?

        list = historyWeatherDao.findByCityDates(cityId, dates)

        return list
    }

}


/**
 * URL Builder
 * */
private fun monthViewUrlBuilder(baseUrl: String, cityPinyin: String, date: LocalDate): String
{
    val urlSeparator = "/"
    val urlSuffix = ".html"

    var urlBuffer: StringBuffer = StringBuffer()

    with(urlBuffer){
        if (! baseUrl.startsWith("http://") && ! baseUrl.startsWith("https://"))
        {
            append("http://")
        }
        append(baseUrl)
        append(urlSeparator)
        append(cityPinyin)
        append(urlSeparator)
        append(date.year)
        var month = date.monthValue
        append(if (month >= 10) month else "0$month")
        append(urlSuffix)
    }

    return urlBuffer.toString()
}



/**
 * 解析获取到的结果集
 * 一个 url 代表一个城市一个月的历史天气
 *
 * */
private fun fetchDataViaSpider(targetUrl: String, city: City): List<HistoryWeather>
{
    var resultItems: ResultItems?
    var hws = mutableListOf<HistoryWeather>()

    try
    {
        resultItems = HistoryWeatherService.spider.get(targetUrl)
    }
    catch(ex: Exception)
    {
        throw ex
    }

    if (resultItems == null || resultItems.request == null)
    {
        throw Exception("Request Can't Be Null")
    }

    with(resultItems){
        var dateStrs: List<String> = get("date")
        var maxes: List<String> = get("max")
        var mines: List<String> = get("min")
        var weathers: List<String> = get("weather")
        var directions: List<String> = get("wind_direction")
        var forces: List<String> = get("wind_force")

        var dates = mutableListOf<Date>()

        // 将 date 的字符串转换成日期数组
        dateStrs.forEach { dates.add(Date.valueOf(it.toString())) }

        for ((index, date) in dates.withIndex())
        {
            var weather: HistoryWeather =  HistoryWeather()

            with(weather){
                this.city = city.id
                this.date = dates[index]
                max = Integer.parseInt(maxes[index])
                min = Integer.parseInt(mines[index])
                // 调换温度的顺序, 数字大的为 max, 小的为 min
                if(max < min)
                {
                    var temp = min
                    min = max
                    max = temp
                }
                this.weather = weathers[index]
                wind_direction = directions[index]
                wind_force = forces[index]
                update_time = Timestamp.valueOf(LocalDateTime.now())
            }

            hws.add(weather)
        }
    }


    return hws
}