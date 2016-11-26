package com.vito.work.weather.web.controllers

import com.vito.work.weather.domain.services.ForecastWeatherService
import com.vito.work.weather.domain.util.http.ObjectResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDate
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/16.
 * Time : 上午10:51.
 * Description:
 *
 */

@Controller
@RequestMapping("/weather/forecast")
open class ForecastWeatherController {

    @Resource
    lateinit var forecastWeatherService: ForecastWeatherService

    @RequestMapping("/")
    open fun index(): String {
        return "weather/forecast/index"
    }

    /**
     * 获取未来三天的天气预报
     * */
    @RequestMapping("/three")
    @ResponseBody
    open fun findThreeDaysWeather(@RequestParam(required = true) districtId: Long): ObjectResponse {
        val weathers = forecastWeatherService.findThreeDaysWeather(districtId)
        return ObjectResponse(weathers)
    }

    /**
     * 获取30天的天气预报
     * */
    @RequestMapping("/30d")
    @ResponseBody
    open fun findThirtyDaysWeather(@RequestParam(required = true) districtId: Long): ObjectResponse {
        val weathers = forecastWeatherService.findThirtyDaysWeather(districtId)
        return ObjectResponse(weathers)
    }

    /**
     * 获取今天的天气预报
     * */
    @RequestMapping("/today")
    @ResponseBody
    open fun findTodayForecastWeather(@RequestParam(required = true) districtId: Long): ObjectResponse {
        val weather = forecastWeatherService.findByDate(districtId, LocalDate.now())
        return ObjectResponse(weather)
    }

}