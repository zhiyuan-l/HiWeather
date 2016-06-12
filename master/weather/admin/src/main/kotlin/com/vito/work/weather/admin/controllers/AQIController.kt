package com.vito.work.weather.admin.controllers

import com.vito.work.weather.domain.services.AQIService
import com.vito.work.weather.domain.services.LocationService
import com.vito.work.weather.domain.util.http.ObjectResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import us.codecraft.webmagic.scheduler.QueueScheduler

/**
 * Created by lingzhiyuan.
 * Date : 16/4/17.
 * Time : 下午6:01.
 * Description:
 *
 */


@Controller
@RequestMapping("/aqi")
open class AQIController @Autowired constructor(val aqiService: AQIService, val locationService: LocationService)
{

    @RequestMapping("/spider/update")
    @ResponseBody
    open fun updateAQIFromWeb(): ObjectResponse
    {
        AQIService.spider.scheduler = QueueScheduler()
        val districts = locationService.findAQIDistrict() ?: listOf()
        districts.forEach {
            try
            {
                aqiService.updateAQI(it)
            }
            catch(ex: Exception)
            {
                // 忽略异常
            }
        }

        return ObjectResponse("true")
    }

}