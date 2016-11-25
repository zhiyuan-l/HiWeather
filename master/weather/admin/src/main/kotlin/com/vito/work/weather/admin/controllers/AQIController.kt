package com.vito.work.weather.admin.controllers

import com.vito.work.weather.domain.services.AQIService
import com.vito.work.weather.domain.util.http.ObjectResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/17.
 * Time : 下午6:01.
 * Description:
 *
 */


@Controller
@RequestMapping("/admin/aqi")
open class AQIController {

    @Resource
    lateinit var aqiService: AQIService

    @RequestMapping("/spider/update")
    @ResponseBody
    open fun updateAQIFromWeb(): ObjectResponse {
        aqiService.execute()
        return ObjectResponse("true")
    }

}