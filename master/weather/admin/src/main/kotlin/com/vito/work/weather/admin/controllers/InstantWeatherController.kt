package com.vito.work.weather.admin.controllers

import com.vito.work.weather.domain.config.SpiderStatus
import com.vito.work.weather.domain.services.InstantWeatherService
import com.vito.work.weather.domain.services.LocationService
import com.vito.work.weather.domain.util.http.BusinessError
import com.vito.work.weather.domain.util.http.BusinessException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

/**
 * Created by lingzhiyuan.
 * Date : 16/4/19.
 * Time : 下午2:57.
 * Description:
 *
 */

@Controller
@RequestMapping("/weather/instant")
open class InstantWeatherController @Autowired constructor(val instantWeatherService: InstantWeatherService, val locationService: LocationService)
{

    // 即时天气, 每半小时更新一次
    @RequestMapping("/spider/update")
    @ResponseBody
    open fun updateFromWeb()
    {
        if(SpiderStatus.INSTANT_WEATHER_UPDATE_STATUS == true)
        {
            throw BusinessException(BusinessError.ERROR_INSTANT_WEATHER_IS_UPDATING)
        }
        val distrcts = locationService.findDistricts() ?: listOf()

        distrcts.forEach { district ->
            try
            {
                instantWeatherService.updateFromWeb(district)
            }
            catch(ex: Exception)
            {
                ex.printStackTrace()
                // 忽略错误
            }
        }
    }

}