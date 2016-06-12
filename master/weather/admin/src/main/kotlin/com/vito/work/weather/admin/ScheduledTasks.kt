package com.vito.work.weather.admin

import com.vito.work.weather.domain.config.Constant
import com.vito.work.weather.domain.config.SpiderStatus
import com.vito.work.weather.domain.entities.City
import com.vito.work.weather.domain.entities.Province
import com.vito.work.weather.domain.services.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import us.codecraft.webmagic.scheduler.QueueScheduler
import java.time.LocalDate

/**
 * Created by lingzhiyuan.
 * Date : 16/4/15.
 * Time : 上午9:27.
 * Description:
 *
 * 定时执行的任务:
 *
 * 1. 30天预报更新   每天早上七点
 * 2. 24小时天气更新  每三小时更新一次
 * 3. 实时AQI更新       每三小时更新一次
 * 4. 历史天气更新    每周日中午十二点更新
 * 5. 即时天气更新    每小时执行一次
 *
 */

@Component
@EnableScheduling
open class ScheduledTasks @Autowired constructor(val locationService: LocationService, val forecastWeatherService: ForecastWeatherService,val aqiService: AQIService, val historyWeatherService: HistoryWeatherService, val hourWeatherService: HourWeatherService, val instantWeatherService: InstantWeatherService){


    companion object{
        val logger = LoggerFactory.getLogger(ScheduledTasks::class.java)
    }

    @Scheduled(cron = "0 0 7 * * * ") // 每天早上七点更新
    open fun scheduledForecastWeatherUpdate()
    {
        // 如果正在更新,则跳过
        if(SpiderStatus.FORECAST_UPDATE_STATUS == true)
        {
            logger.info("Skip Scheduled Task : Forecast Weather Is Updating")
            return
        }

        logger.info("Start : Executing Scheduled Task Update Forecast Weather")

        // 重置 Scheduler （清空Scheduler内已爬取的 url）
        ForecastWeatherService.spider.scheduler = QueueScheduler()
        var districts = locationService.findDistricts()

        var cities = locationService.findCities() ?: listOf()

        for(city in cities)
        {
            districts?.filter { it.city == city.id }?.forEach { district ->

                try
                {
                    forecastWeatherService.updateFromWeb(city, district)
                }
                catch(ex: Exception)
                {
                    ex.printStackTrace()
                    // 忽略更新时的异常
                }
            }
        }
        logger.info("Success : Task Update Forecast Weather")
    }

    @Scheduled(cron = "0 0 0-23/3 * * * ") // 每天0-23点,每三小时检查一次
    open fun scheduledTodayWeatherUpdate()
    {
        // 如果正在更新,则跳过
        if(SpiderStatus.TODAY_WEATHER_STATUS == true)
        {
            logger.info("Skip Scheduled Task : Today Weather Is Updating")
            return
        }

        logger.info("Start : Executing Scheduled Task Update Today Weather")

        // 重置 Scheduler （清空Scheduler内已爬取的 url）
        HourWeatherService.spider.scheduler = QueueScheduler()
        var districts = locationService.findDistricts()
        var cities = locationService.findCities() ?: listOf()

        districts?.forEach { district ->
            try
            {
                hourWeatherService.updateFromWeb(cities.first{ it.id == district.city}, district)
            }
            catch(ex: Exception)
            {
                ex.printStackTrace()
                // 忽略更新时的异常
            }
         }

        logger.info("Success : Task Update Today Weather")
    }

    @Scheduled(cron = "0 0 0-23/3 * * * ") // 每天0-23点,每三小时检查一次
    open fun scheduledAQIUpdate()
    {
        // 如果正在更新,则跳过
        if(SpiderStatus.AQI_UPDATE_STATUS == true)
        {
            logger.info("Skip Scheduled Task : AQI Is Updating")
            return
        }

        logger.info("Start : Executing Scheduled AQI Update")

        // 重置 Scheduler （清空Scheduler内已爬取的 url）
        AQIService.spider.scheduler = QueueScheduler()
        val districts = locationService.findAQIDistrict() ?: listOf()
        districts.forEach { it ->
            try
            {
                aqiService.updateAQI(it)
            }
            catch(ex: Exception)
            {
                ex.printStackTrace()
                // 忽略异常
            }
        }

        logger.info("Success : AQI Updated")
    }


    @Scheduled(cron = "0 0 12 ? * SUN") // 每周的周日中午十二点更新
    open fun scheduledHistoryWeatherUpdate()
    {
        // 如果正在更新,则跳过
        if( SpiderStatus.HISTORY_UPDATE_STATUS == true)
        {
            logger.info("Skip Scheduled Task : History Weather Is Updating")
            return
        }

        logger.info("Start : Executing Scheduled Task Update History Weather")

        // 重置 Scheduler (清空已爬取的 urls)
        HistoryWeatherService.spider.scheduler = QueueScheduler()

        var provinces = mutableListOf<Province>()
        var cities = mutableListOf<City>()

        provinces.addAll(locationService.findProvinces())
        cities.addAll(locationService.findCities(provinces))
        for (city in cities)
        {
            var tempDate = Constant.SPIDER_HISTORY_START_DATE
            while (tempDate <= LocalDate.now().minusMonths(1))
            {
                try
                {
                    tempDate = tempDate.plusMonths(1)
                    historyWeatherService.updateFromWeb(city, tempDate)
                }
                catch(ex: Exception)
                {
                    // 忽略更新时的异常
                    continue
                }
            }
        }
        logger.info("Success : Task Update History Weather")
    }

    // 即时天气
    @Scheduled(cron = "0 0 0-23/1 * * *") // 每小时执行一次
    open fun scheduledInstantWeatherUpdate()
    {
        // 如果正在更新,则跳过
        if( SpiderStatus.INSTANT_WEATHER_UPDATE_STATUS == true)
        {
            logger.info("Skip Scheduled Task : Instant Weather Is Updating")
            return
        }

        logger.info("Start : Executing Scheduled Task Update Instant Weather")

        val distrcts = locationService.findDistricts() ?: listOf()

        SpiderStatus.INSTANT_WEATHER_UPDATE_STATUS = true
        distrcts.forEach { district ->
            try
            {
                instantWeatherService.updateFromWeb(district)
            }
            catch(ex: Exception)
            {
                ex.printStackTrace()
                // 忽略错误
            }
        }
        SpiderStatus.INSTANT_WEATHER_UPDATE_STATUS = false
        logger.info("Success : Task Update Instant Weather")

    }

}