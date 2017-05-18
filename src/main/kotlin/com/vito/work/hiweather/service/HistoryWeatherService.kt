package com.vito.work.weather.service

import com.vito.work.weather.config.Constant
import com.vito.work.weather.dto.City
import com.vito.work.weather.dto.HistoryWeather
import com.vito.work.weather.repo.DistrictDao
import com.vito.work.weather.repo.HistoryWeatherDao
import com.vito.work.weather.service.spider.AbstractSpiderTask
import com.vito.work.weather.service.spider.MonthViewPageProcessor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import us.codecraft.webmagic.ResultItems
import us.codecraft.webmagic.Spider
import us.codecraft.webmagic.scheduler.QueueScheduler
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import javax.annotation.PreDestroy
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/5.
 * Time : 下午7:55.
 * Description:
 *
 */

@Service
@Transactional
class HistoryWeatherService : AbstractSpiderTask() {

    @Resource
    lateinit var historyWeatherDao: HistoryWeatherDao
    @Resource
    lateinit var districtDao: DistrictDao

    @PreDestroy
    fun destroy() {
        spider.close()
        logger.info("History Spider Stopped")
    }

    companion object {
        // 只使用一个 spider, 一个线程池
        var spider: Spider = Spider.create(MonthViewPageProcessor())
                .thread(Constant.SPIDER_THREAD_COUNT)

        private val logger = LoggerFactory.getLogger(HistoryWeatherService::class.java)
    }

    fun execute() {
        task {
            HistoryWeatherService.spider.scheduler = QueueScheduler()
            val cities = districtDao.findAll(City::class.java)
            cities.forEach { city ->
                var tempDate = Constant.SPIDER_HISTORY_START_DATE
                while (tempDate <= LocalDate.now().minusMonths(1)) {
                    try {
                        tempDate = tempDate.plusMonths(1)
                        updateFromWeb(city, tempDate)
                    } catch(ex: Exception) {
                        // 忽略更新时的异常
                        continue
                    }
                }
            }
        }
    }

    /**
     * 根据日期找到一个历史项, 找不到则抛出资源未找到的异常
     * */
    fun findHistoryWeather(city: City, date: LocalDate): HistoryWeather? {
        val weather = historyWeatherDao.findByCityDate(city.id, Date.valueOf(date))
        return weather
    }

    /**
     * 更新历史天气的入口
     *
     * 执行更新和和保存操作
     * */
    fun updateFromWeb(city: City, date: LocalDate) {
        try {
            task {
                val targetUrl = monthViewUrlBuilder(Constant.HISTORY_WEATHER_BASE_URL, city.pinyin, date)
                val hws = fetchDataViaSpider(targetUrl, city)
                saveHistoryWeather(hws, city)
            }
        } finally {
            spider.scheduler = QueueScheduler()
        }
    }

    /**
     * 执行保存历史天气的操作
     * */
    fun saveHistoryWeather(weathers: List<HistoryWeather>, city: City) {

        val savedWeathers = weathers
                .map { it.date }
                .let { historyWeatherDao.findByCityDates(city.id, it) }
                .toMutableList()

        weathers.forEach {
            iw ->
            savedWeathers.singleOrNull { it.city == iw.city && it.date == iw.date }?.apply {
                max = iw.max
                min = iw.min
                wind_direction = iw.wind_direction
                wind_force = iw.wind_force
                weather = iw.weather
                update_time = iw.update_time
            } ?: savedWeathers.add(iw)
        }

        savedWeathers.forEach { historyWeatherDao save it }
    }

    fun findHistoryWeathersOfToday(cityId: Long): List<HistoryWeather> {
        val now = LocalDate.now()
        val dates = (Constant.SPIDER_HISTORY_START_DATE.year .. now.year).map { Date.valueOf(LocalDate.of(it, now.monthValue, now.dayOfMonth)) }
        return historyWeatherDao.findByCityDates(cityId, dates)
    }

    fun findHistoryTops(cityId: Long): List<HistoryWeather> {

        val result = mutableListOf<HistoryWeather>()

        val maxTempWeather = historyWeatherDao.findHighestTemperature(cityId) ?: HistoryWeather()
        val minTempWeather = historyWeatherDao.findLowestTemperature(cityId) ?: HistoryWeather()

        result.add(maxTempWeather)
        result.add(minTempWeather)

        return result
    }

    /**
     * 根据城市的 id查找某一天的历史天气
     * */
    fun findByDate(cityId: Long, date: LocalDate): HistoryWeather? {
        return historyWeatherDao.findByCityDate(cityId, Date.valueOf(date))
    }

    /**
     * 根据城市的 id 查出某一个月所有的历史天气
     * */
    fun findByMonth(cityId: Long, date: LocalDate): List<HistoryWeather>? {
        val dates = mutableListOf<Date>()

        // 这个月的起始日期
        val startDate = date.minusDays(date.dayOfMonth.toLong() - 1)
        (0 .. date.lengthOfMonth() - 1).forEach { offset -> dates.add(Date.valueOf(startDate.plusDays(offset.toLong()))) }
        return historyWeatherDao.findByCityDates(cityId, dates)
    }
}


/**
 * URL Builder
 * */
private val monthViewUrlBuilder: (String, String, LocalDate) -> String = {
    baseUrl, cityPinyin, date ->
    val urlSeparator = "/"
    val urlSuffix = ".html"
    StringBuffer().apply {
        if (! baseUrl.startsWith("http://") && ! baseUrl.startsWith("https://")) {
            append("http://")
        }
        append(baseUrl)
        append(urlSeparator)
        append(cityPinyin)
        append(urlSeparator)
        append(date.year)
        val month = date.monthValue
        append(if (month >= 10) month else "0$month")
        append(urlSuffix)
    }.toString()
}

/**
 * 解析获取到的结果集
 * 一个 url 代表一个城市一个月的历史天气
 *
 * */
private val fetchDataViaSpider = fun(targetUrl: String, city: City): List<HistoryWeather> {
    val resultItems: ResultItems?

    try {
        resultItems = HistoryWeatherService.spider.get(targetUrl)
    } catch(ex: Exception) {
        throw ex
    }

    if (resultItems == null || resultItems.request == null) {
        throw Exception("Request Can't Be Null")
    }
    var hws: List<HistoryWeather> = listOf()

    resultItems.apply {
        val dateStrs: List<String> = get("date")
        val maxes: List<String> = get("max")
        val mines: List<String> = get("min")
        val weathers: List<String> = get("weather")
        val directions: List<String> = get("wind_direction")
        val forces: List<String> = get("wind_force")
        hws = dateStrs
                .map { Date.valueOf(it) }
                .mapIndexed {
                    index, date ->
                    HistoryWeather().apply {
                        this.city = city.id
                        this.date = date
                        this.max = Integer.parseInt(maxes[index])
                        this.min = Integer.parseInt(mines[index])
                        // 调换温度的顺序, 数字大的为 max, 小的为 min
                        if (this.max < this.min) {
                            val temp = this.min
                            this.min = this.max
                            this.max = temp
                        }
                        this.weather = weathers[index]
                        this.wind_direction = directions[index]
                        this.wind_force = forces[index]
                        this.update_time = Timestamp.valueOf(LocalDateTime.now())
                    }
                }
    }
    return hws

}