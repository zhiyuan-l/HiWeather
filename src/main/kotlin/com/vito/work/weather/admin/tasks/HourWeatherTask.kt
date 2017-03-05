package com.vito.work.weather.admin.tasks

import com.vito.work.weather.config.SpiderStatus
import com.vito.work.weather.service.HourWeatherService
import com.vito.work.weather.service.LocationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import us.codecraft.webmagic.scheduler.QueueScheduler

/**
 * Created by lingzhiyuan.
 * Date : 2016/11/22.
 * Time : 12:49.
 * Description:
 *
 */

@Component
@EnableScheduling
open class HourWeatherTask @Autowired constructor(val locationService: LocationService, val hourWeatherService: HourWeatherService) {

    companion object {
        val logger = LoggerFactory.getLogger(InstantWeatherTask::class.java)
    }

    @Scheduled(cron = "0 0 0-23/3 * * ?") // 每天0-23点,每三小时检查一次
    open fun scheduledTodayWeatherUpdate() {
        // 如果正在更新,则跳过
        if (SpiderStatus.TODAY_WEATHER_STATUS) {
            logger.info("Skip Scheduled Task : Today Weather Is Updating")
            return
        }

        logger.info("Start : Executing Scheduled Task Update Today Weather")

        // 重置 Scheduler （清空Scheduler内已爬取的 url）
        HourWeatherService.spider.scheduler = QueueScheduler()
        val districts = locationService.findDistricts()

        districts.forEach { district ->
            try {
                hourWeatherService.execute(district)
            } catch(ex: Exception) {
                ex.printStackTrace()
                // 忽略更新时的异常
            }
        }

        logger.info("Success : Task Update Today Weather")
    }
}