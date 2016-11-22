package com.vito.work.weather.admin.tasks

import com.vito.work.weather.domain.config.SpiderStatus
import com.vito.work.weather.domain.services.ForecastWeatherService
import com.vito.work.weather.domain.services.LocationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import us.codecraft.webmagic.scheduler.QueueScheduler

/**
 * Created by lingzhiyuan.
 * Date : 2016/11/22.
 * Time : 12:44.
 * Description:
 *
 */

@Component
@EnableScheduling
open class ForecastWeatherTask @Autowired constructor(val locationService: LocationService, val forecastWeatherService: ForecastWeatherService) {


    companion object {
        val logger = LoggerFactory.getLogger(ForecastWeatherTask::class.java)
    }

    @Scheduled(cron = "0 0 7 * * ?") // 每天早上七点更新
    open fun scheduledForecastWeatherUpdate() {
        // 如果正在更新,则跳过
        if (SpiderStatus.FORECAST_UPDATE_STATUS == true) {
            logger.info("Skip Scheduled Task : Forecast Weather Is Updating")
            return
        }

        logger.info("Start : Executing Scheduled Task Update Forecast Weather")

        // 重置 Scheduler （清空Scheduler内已爬取的 url）
        ForecastWeatherService.spider.scheduler = QueueScheduler()
        val districts = locationService.findDistricts()

        val cities = locationService.findCities() ?: listOf()

        for (city in cities) {
            districts?.filter { it.city == city.id }?.forEach { district ->

                try {
                    forecastWeatherService.updateFromWeb(city, district)
                }
                catch(ex: Exception) {
                    ex.printStackTrace()
                    // 忽略更新时的异常
                }
            }
        }
        logger.info("Success : Task Update Forecast Weather")
    }
}