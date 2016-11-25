package com.vito.work.weather.admin.controllers

import com.vito.work.weather.domain.config.Constant
import com.vito.work.weather.domain.config.SpiderStatus
import com.vito.work.weather.domain.entities.City
import com.vito.work.weather.domain.entities.Province
import com.vito.work.weather.domain.services.ForecastWeatherService
import com.vito.work.weather.domain.services.LocationService
import com.vito.work.weather.domain.util.http.BusinessError
import com.vito.work.weather.domain.util.http.BusinessException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import us.codecraft.webmagic.scheduler.QueueScheduler
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/11.
 * Time : 下午1:15.
 * Description:
 *
 */

@Controller
@RequestMapping("/admin/weather/forecast")
open class ForecastWeatherController {

    @Resource
    lateinit var forecastWeatherService: ForecastWeatherService
    @Resource
    lateinit var locationService: LocationService

    @RequestMapping("/")
    open fun forecastIndex(): String {
        return "weather/forecast/index"
    }

    @RequestMapping("/spider/update")
    @ResponseBody
    open fun updateForecastFromWeb(@RequestParam(required = true) type: Int, @RequestParam(required = false, defaultValue = "0") cityId: Long) {
        // 如果正在更新,则跳过
        if (SpiderStatus.FORECAST_UPDATE_STATUS == true) {
            throw BusinessException(BusinessError.ERROR_FORECAST_WEATHER_UPDATING)
        }
        // 重置 Scheduler （清空Scheduler内已爬取的 url）
        ForecastWeatherService.spider.scheduler = QueueScheduler()
        val provinces: List<Province> = locationService.findProvinces()
        val cities: List<City>
        val districts = locationService.findDistricts()

        when (type) {
            Constant.FORECAST_WEATHER_UPDATE_TYPE_ALL  -> {
                cities = locationService.findCities(provinces)
            }
            Constant.FORECAST_WEATHER_UPDATE_TYPE_CITY -> {
                if (cityId == 0L) {
                    throw BusinessException(BusinessError.ERROR_CITY_NOT_FOUND)
                }
                val city = locationService.getCity(cityId)
                if (city != null) {
                    cities = listOf(city)
                }
                else {
                    throw BusinessException(BusinessError.ERROR_CITY_NOT_FOUND)
                }
            }
            else                                       -> {
                throw BusinessException(BusinessError.ERROR_TYPE_NOT_SUPPORTED)
            }
        }

        for (city in cities) {
            try {
                districts?.filter { it.city == city.id }?.forEach { forecastWeatherService.execute(city, it) }
            }
            catch(ex: Exception) {
                // 忽略更新的异常
                continue
            }
        }
    }

}