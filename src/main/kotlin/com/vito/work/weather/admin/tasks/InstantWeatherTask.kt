package com.vito.work.weather.admin.tasks

import com.vito.work.weather.service.InstantWeatherService
import com.vito.work.weather.service.LocationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Created by lingzhiyuan.
 * Date : 2016/11/22.
 * Time : 12:43.
 * Description:
 *
 */

@Component
@EnableScheduling
open class InstantWeatherTask @Autowired constructor(val locationService: LocationService, val instantWeatherService: InstantWeatherService) {

    companion object {
        val logger = LoggerFactory.getLogger(InstantWeatherTask::class.java) !!
    }

    // 即时天气
    @Scheduled(cron = "0 0 0-23/1 * * ?")
    open fun scheduledInstantWeatherUpdate() {

        instantWeatherService.execute()
        logger.info("Success : Task Update Instant Weather")

    }
}