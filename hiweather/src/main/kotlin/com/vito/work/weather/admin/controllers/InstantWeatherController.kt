package com.vito.work.weather.admin.controllers

import com.vito.work.weather.domain.services.InstantWeatherService
import com.vito.work.weather.domain.services.LocationService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
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
@RequestMapping("/admin/weather/instant")
open class InstantWeatherController {

    @Resource
    lateinit var locationService: LocationService
    @Resource
    lateinit var instantWeatherService: InstantWeatherService

    // 即时天气, 每半小时更新一次
    @RequestMapping("/spider/update")
    @ResponseBody
    open fun updateFromWeb() {
        instantWeatherService.execute()
    }

}