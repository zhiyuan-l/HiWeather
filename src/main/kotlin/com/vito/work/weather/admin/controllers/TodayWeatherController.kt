package com.vito.work.weather.admin.controllers

import com.vito.work.weather.config.SpiderStatus
import com.vito.work.weather.service.HourWeatherService
import com.vito.work.weather.service.LocationService
import com.vito.work.weather.util.http.BusinessError
import com.vito.work.weather.util.http.BusinessException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import us.codecraft.webmagic.scheduler.QueueScheduler
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/19.
 * Time : 下午2:57.
 * Description:
 *
 */

@Controller
@RequestMapping("/admin/weather/today")
class TodayWeatherController {
    @Resource
    lateinit var locationService: LocationService
    @Resource
    lateinit var hourWeatherService: HourWeatherService

    @RequestMapping("/spider/update")
    @ResponseBody
    fun updateFromWeb() {

        if (SpiderStatus.TODAY_WEATHER_STATUS == true) {
            throw BusinessException(BusinessError.ERROR_TODAY_WEATHER_UPDATING)
        }
        // 重置 Scheduler （清空Scheduler内已爬取的 url）
        HourWeatherService.spider.scheduler = QueueScheduler()

        val districts = locationService.findDistricts()

        districts?.forEach { district ->
            try {
                hourWeatherService.execute(district)
            } catch(ex: Exception) {
                ex.printStackTrace()
                // 忽略错误
            }
        }
    }

}