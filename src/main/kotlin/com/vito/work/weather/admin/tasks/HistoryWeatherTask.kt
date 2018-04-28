package com.vito.work.weather.admin.tasks

import com.vito.work.weather.service.HistoryWeatherService
import com.vito.work.weather.service.LocationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Created by lingzhiyuan.
 * Date : 2016/11/22.
 * Time : 12:44.
 * Description:
 *
 */

@Component
@EnableScheduling
class HistoryWeatherTask @Autowired constructor(val locationService: LocationService, val historyWeatherService: HistoryWeatherService) {


    companion object {
        val logger = LoggerFactory.getLogger(HistoryWeatherTask::class.java)
    }

    @Scheduled(cron = "0 0 12 ? * SUN") // 每周的周日中午十二点更新
    fun scheduledHistoryWeatherUpdate() {

        logger.info("Start : Executing Scheduled Task Update History Weather")

        historyWeatherService.execute()

        logger.info("Success : Task Update History Weather")
    }
}