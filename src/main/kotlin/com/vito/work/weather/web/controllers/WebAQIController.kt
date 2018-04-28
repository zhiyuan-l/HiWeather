package com.vito.work.weather.web.controllers

import com.vito.work.weather.dto.District
import com.vito.work.weather.service.AQIService
import com.vito.work.weather.service.LocationService
import com.vito.work.weather.util.http.ObjectResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/18.
 * Time : 下午2:25.
 * Description:
 *
 */

@Controller
@RequestMapping("/weather/aqi")
class WebAQIController {

    @Resource
    lateinit var aqiService: AQIService
    @Resource
    lateinit var locationService: LocationService

    @RequestMapping("/")
    fun index(): String {
        return "web/weather/aqi/index"
    }

    /**
     * 获取城市所有监测站点的最新 AQI 数据
     * */
    @RequestMapping("/station")
    @ResponseBody
    fun stationAQI(@RequestParam cityId: Long): ObjectResponse {

        val district = locationService.findDistricts(cityId)?.firstOrNull { it.pinyin_aqi != "" } ?: District()
        val data = aqiService.findStationAQI(district.id)
        val response = ObjectResponse(data)
        return response
    }

    /**
     * 获取空气质量实况
     * */
    @RequestMapping("/instant")
    @ResponseBody
    fun instantAQI(@RequestParam cityId: Long): ObjectResponse {
        val district = locationService.findDistricts(cityId)?.firstOrNull { it.pinyin_aqi != "" } ?: District()
        val data = aqiService.findLatestAQI(district.id)
        val response = ObjectResponse(data)
        return response
    }

}