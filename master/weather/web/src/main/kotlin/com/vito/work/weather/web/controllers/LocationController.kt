package com.vito.work.weather.web.controllers

import com.vito.work.weather.domain.services.LocationService
import com.vito.work.weather.domain.util.http.ObjectResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

/**
 * Created by lingzhiyuan.
 * Date : 16/4/11.
 * Time : 下午1:05.
 * Description:
 *
 */

@Controller
@RequestMapping("/location")
open class LocationController @Autowired constructor(val locationService: LocationService)
{

    /**
     * 前往 index 页面
     * */
    @RequestMapping("/")
    fun toManage(): String
    {
        return "location/index"
    }

    /**
     * 获取所有省份
     * */
    @RequestMapping("/provinces")
    @ResponseBody
    fun getProvinces(): ObjectResponse
    {
        var provinces = locationService.findProvinces()
        var response = ObjectResponse(provinces)
        return response
    }

    /**
     * 获取所有地级市
     * */
    @RequestMapping("/cities")
    @ResponseBody
    fun getCitiesByProvinceId(@RequestParam(required = false, defaultValue = "0")provinceId: Long): ObjectResponse
    {
        var cities = locationService.findCities(provinceId)
        var response = ObjectResponse(cities)
        return response
    }

    /**
     * 获取所有县级市, 区, 县
     * */
    @RequestMapping("/districts")
    @ResponseBody
    fun getDistrictByCityId(@RequestParam(required = false, defaultValue = "0") cityId: Long): ObjectResponse
    {
        var districts = locationService.findDistricts(cityId)
        var response = ObjectResponse(districts)
        return response
    }


}