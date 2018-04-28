package com.vito.work.weather.web.controllers

import com.vito.work.weather.service.LocationService
import com.vito.work.weather.util.http.ObjectResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/11.
 * Time : 下午1:05.
 * Description:
 *
 */

@Controller
@RequestMapping("/location")
class WebLocationController {

    @Resource
    lateinit var locationService: LocationService

    /**
     * 前往 index 页面
     * */
    @RequestMapping("/")
    fun toManage(): String {
        return "web/location/index"
    }

    /**
     * 获取所有省份
     * */
    @RequestMapping("/provinces")
    @ResponseBody
    fun getProvinces(): ObjectResponse {
        val provinces = locationService.findProvinces()
        val response = ObjectResponse(provinces)
        return response
    }

    /**
     * 获取所有地级市
     * */
    @RequestMapping("/cities")
    @ResponseBody
    fun getCitiesByProvinceId(@RequestParam(required = false, defaultValue = "0") provinceId: Long): ObjectResponse {
        val cities = locationService.findCities(provinceId)
        val response = ObjectResponse(cities)
        return response
    }

    /**
     * 获取所有县级市, 区, 县
     * */
    @RequestMapping("/districts")
    @ResponseBody
    fun getDistrictByCityId(@RequestParam(required = false, defaultValue = "0") cityId: Long): ObjectResponse {
        val districts = locationService.findDistricts(cityId)
        val response = ObjectResponse(districts)
        return response
    }


}