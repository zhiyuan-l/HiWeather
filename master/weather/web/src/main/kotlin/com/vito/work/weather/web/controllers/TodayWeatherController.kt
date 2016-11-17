package com.vito.work.weather.web.controllers

import com.vito.work.weather.domain.services.HourWeatherService
import com.vito.work.weather.domain.services.InstantWeatherService
import com.vito.work.weather.domain.util.http.ObjectResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/19.
 * Time : 下午2:57.
 * Description:
 *
 */

@Controller
@RequestMapping("/weather/today")
open class TodayWeatherController
{

    @Resource
    lateinit var instantWeatherService: InstantWeatherService
    @Resource
    lateinit var  hourWeatherService: HourWeatherService

    /**
     * 获取最新的24小时天气预报
     * */
    @RequestMapping("/24h")
    @ResponseBody
    open fun find24HWeather(@RequestParam(required = true)districtId: Long): ObjectResponse
    {
        var weathers = hourWeatherService.find24HWeather(districtId)
        var response = ObjectResponse(weathers)
        return response
    }

    /**
     * 获取最新的即时天气
     * */
    @RequestMapping("/now")
    @ResponseBody
    open fun findLatestInstantWeather(@RequestParam(required = true)districtId: Long): ObjectResponse
    {
        var weather = instantWeatherService.findLatestInstantWeather(districtId)
        var response = ObjectResponse(weather)
        return response
    }

}