package com.vito.work.weather.domain.services

import com.vito.work.weather.domain.config.Constant
import com.vito.work.weather.domain.daos.HistoryWeatherDao
import com.vito.work.weather.domain.daos.LocationDao
import com.vito.work.weather.domain.entities.City
import com.vito.work.weather.domain.entities.HistoryWeather
import com.vito.work.weather.domain.entities.Province
import com.vito.work.weather.domain.services.spider.MonthViewPageProcessor
import com.vito.work.weather.domain.util.http.BusinessError
import com.vito.work.weather.domain.util.http.BusinessException
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
open class HistoryWeatherService : AbstractSpiderTask() {

    @Resource
    lateinit var historyWeatherDao: HistoryWeatherDao
    @Resource
    lateinit var locationDao: LocationDao

    @PreDestroy
    open fun destroy() {
        spider.close()
        logger.info("History Spider Stopped")
    }

    companion object {
        // 只使用一个 spider, 一个线程池
        var spider: Spider = Spider.create(MonthViewPageProcessor())
                .thread(Constant.SPIDER_THREAD_COUNT)

        private val logger = LoggerFactory.getLogger(HistoryWeatherService::class.java)
    }

    open fun execute() {
        task {
            val provinces = mutableListOf<Province>()
            val cities = mutableListOf<City>()
            HistoryWeatherService.spider.scheduler = QueueScheduler()
            provinces.addAll(locationDao.findAll(Province::class.java))
            cities.addAll(locationDao.findAll(City::class.java))
            for (city in cities) {
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
    open fun findHistoryWeather(city: City, date: LocalDate): HistoryWeather? {
        val weather = historyWeatherDao.findByCityDate(city.id, Date.valueOf(date))
        return weather
    }

    /**
     * 更新历史天气的入口
     *
     * 执行更新和和保存操作
     * */
    open fun updateFromWeb(city: City, date: LocalDate) {
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
    open fun saveHistoryWeather(weathers: List<HistoryWeather>, city: City) {
        val dates = mutableListOf<Date>()
        weathers.forEach { dates.add(it.date) }
        val savedWeathers: MutableList<HistoryWeather> = mutableListOf()
        val temp = historyWeatherDao.findByCityDates(city.id, dates)
        if (temp != null) {
            savedWeathers.addAll(temp)
        }
        weathers.forEach { iw ->
            val t = savedWeathers.firstOrNull { it -> it.city == iw.city && it.date == iw.date }
            if (t == null) {
                savedWeathers.add(iw)
            } else {
                t.max = iw.max
                t.min = iw.min
                t.wind_direction = iw.wind_direction
                t.wind_force = iw.wind_force
                t.weather = iw.weather
                t.update_time = iw.update_time
            }
        }

        savedWeathers.forEach { historyWeatherDao save it }
    }

    open fun findHistoryWeathersOfToday(cityId: Long): List<HistoryWeather> {
        val now = LocalDate.now()

        val dates = mutableListOf<Date>()
        for (year in Constant.SPIDER_HISTORY_START_DATE.year .. now.year) {
            dates.add(Date.valueOf(LocalDate.of(year, now.monthValue, now.dayOfMonth)))
        }

        val result = historyWeatherDao.findByCityDates(cityId, dates) ?: throw BusinessException(BusinessError.ERROR_RESOURCE_NOT_FOUND)

        return result
    }

    open fun findHistoryTops(cityId: Long): List<HistoryWeather> {

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
    open val findByDate: (Long, LocalDate) -> HistoryWeather? = {
        cityId, date ->
        historyWeatherDao.findByCityDate(cityId, Date.valueOf(date))
    }

    /**
     * 根据城市的 id 查出某一个月所有的历史天气
     * */
    open val findByMonth: (Long, LocalDate) -> List<HistoryWeather>? = {
        cityId, date ->
        val dates = mutableListOf<Date>()

        // 这个月的起始日期
        val startDate = date.minusDays(date.dayOfMonth.toLong() - 1)
        (0 .. date.lengthOfMonth() - 1).forEach { offset -> dates.add(Date.valueOf(startDate.plusDays(offset.toLong()))) }
        historyWeatherDao.findByCityDates(cityId, dates)
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
    val hws = mutableListOf<HistoryWeather>()

    try {
        resultItems = HistoryWeatherService.spider.get(targetUrl)
    } catch(ex: Exception) {
        throw ex
    }

    if (resultItems == null || resultItems.request == null) {
        throw Exception("Request Can't Be Null")
    }

    with(resultItems) {
        val dateStrs: List<String> = get("date")
        val maxes: List<String> = get("max")
        val mines: List<String> = get("min")
        val weathers: List<String> = get("weather")
        val directions: List<String> = get("wind_direction")
        val forces: List<String> = get("wind_force")

        val dates = mutableListOf<Date>()

        // 将 date 的字符串转换成日期数组
        dateStrs.forEach { dates.add(Date.valueOf(it.toString())) }

        for ((index, date) in dates.withIndex()) {
            val weather: HistoryWeather = HistoryWeather()

            with(weather) {
                this.city = city.id
                this.date = dates[index]
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

            hws.add(weather)
        }
    }


    return hws
}