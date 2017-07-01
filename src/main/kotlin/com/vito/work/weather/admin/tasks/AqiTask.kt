package com.vito.work.weather.admin.tasks

import com.vito.work.weather.service.AQIService
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
open class AqiTask @Autowired constructor(val locationService: LocationService, val aqiService: AQIService) {

    companion object {
        val logger = LoggerFactory.getLogger(AqiTask::class.java) !!
    }

    @Scheduled(cron = "0 0 0-23/3 * * ?") // 每天0-23点,每三小时检查一次
    open fun scheduledAQIUpdate() {
        aqiService.execute()
        logger.info("Success : AQI Updated")
    }
}