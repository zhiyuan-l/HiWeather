package com.vito.work.weather.admin.controllers

import com.vito.work.weather.config.Constant
import com.vito.work.weather.dto.City
import com.vito.work.weather.dto.Province
import com.vito.work.weather.service.HistoryWeatherService
import com.vito.work.weather.service.LocationService
import com.vito.work.weather.util.http.BusinessError
import com.vito.work.weather.util.http.BusinessException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDate
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/19.
 * Time : 下午3:11.
 * Description:
 *
 */

@Controller
@RequestMapping("/admin/weather/history")
class HistoryWeatherController {

    @Resource
    lateinit var locationService: LocationService
    @Resource
    lateinit var historyWeatherService: HistoryWeatherService

    @RequestMapping("/")
    fun forecastIndex(): String {
        return "admin/weather/history/index"
    }

    /**
     * @param year 是否是更新特定年份的历史天气
     * @param month 是否更新特定月份的历史天气
     * @param provinceId 是否是更新特定省份的历史天气
     * @param cityId 是否是更新特定城市的历史天气
     * */
    @RequestMapping("/spider/update")
    @ResponseBody
    fun updateHistoryFromWeb(@RequestParam(required = true) type: Int,
                                  @RequestParam(required = false, defaultValue = "0") provinceId: Long,
                                  @RequestParam(required = false, defaultValue = "0") cityId: Long) {

        val provinces = mutableListOf<Province>()
        val cities = mutableListOf<City>()

        when (type) {
            Constant.HISTORY_WEATHER_UPDATE_TYPE_ALL      -> {
                provinces.addAll(locationService.findProvinces())
                cities.addAll(locationService.findCities(provinces))
            }
            Constant.HISTORY_WEATHER_UPDATE_TYPE_PROVINCE -> {
                val province = locationService.getProvince(provinceId)
                if (province != null) provinces.add(province) else throw BusinessException(BusinessError.ERROR_PROVINCE_NOT_FOUND)
                cities.addAll(locationService.findCities(provinces))
            }
            Constant.HISTORY_WEATHER_UPDATE_TYPE_CITY     -> {
                val city = locationService.getCity(cityId)
                if (city != null) cities.add(city) else throw BusinessException(BusinessError.ERROR_CITY_NOT_FOUND)
            }
        }

        for (city in cities) {
            var tempDate = Constant.SPIDER_HISTORY_START_DATE
            while (tempDate <= LocalDate.now().minusMonths(1)) {
                try {
                    tempDate = tempDate.plusMonths(1)
                    historyWeatherService.updateFromWeb(city, tempDate)
                } catch(ex: Exception) {
                    // 忽略更新时的异常
                    continue
                }
            }
        }
    }

}