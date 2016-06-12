package com.vito.work.weather.admin.controllers

import com.vito.work.weather.domain.services.ForecastWeatherService
import com.vito.work.weather.domain.services.HourWeatherService
import com.vito.work.weather.domain.services.LocationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletRequest

/**
 * Created by lingzhiyuan.
 * Date : 16/4/10.
 * Time : 上午11:47.
 * Description:
 *
 */

@Controller
open class BasicController @Autowired constructor(val forecastWeatherService: ForecastWeatherService, val hourWeatherService: HourWeatherService, val locationService: LocationService)
{

    @RequestMapping("/")
    open fun toIndex(): String
    {
        return "index"
    }

    @RequestMapping("/test")
    open fun test()
    {

    }

    @RequestMapping("/login")
    open fun login(): String
    {
        return "login"
    }

    @RequestMapping("/logout")
    open fun logout(request: HttpServletRequest): String
    {
        request.logout()
        return "login"
    }

}
