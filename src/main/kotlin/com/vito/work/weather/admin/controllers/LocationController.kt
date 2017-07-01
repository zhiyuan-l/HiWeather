package com.vito.work.weather.admin.controllers

import com.vito.work.weather.domain.beans.api.LocationData
import com.vito.work.weather.dto.City
import com.vito.work.weather.dto.Province
import com.vito.work.weather.service.LocationService
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
@RequestMapping("/admin/location")
open class LocationController {

    @Resource
    lateinit var locationService: LocationService

    @RequestMapping("/")
    open fun toManage(): String {
        return "admin/location/index"
    }

    /**
     * 获取所有省信息
     * */
    @RequestMapping("/provinces")
    @ResponseBody
    fun getProvinces(): List<Province>? {
        return locationService.findProvinces()
    }

    /**
     * 获取所有市信息
     * */
    @RequestMapping("/cities")
    @ResponseBody
    fun getCitiesByProvinceId(@RequestParam(required = false, defaultValue = "0") provinceId: Long): List<City?>? {
        return locationService.findCities(provinceId)
    }

    /**
     * 获取所有区县信息
     * */
    @RequestMapping("/districts")
    @ResponseBody
    fun getDistrictByCityId(@RequestParam(required = false, defaultValue = "0") cityId: Long): List<Any?>?
            = locationService.findDistricts(cityId)

    /**
     * 从网上更新所有区域信息
     * */
    @RequestMapping("/spider/update")
    @ResponseBody
    fun updateLocations(): Any {
        locationService.execute(LocationData.LOCATION_INFO_TYPE_ZERO)
        locationService.execute(LocationData.LOCATION_INFO_TYPE_ONE)
        locationService.execute(LocationData.LOCATION_INFO_TYPE_TWO)
        locationService.execute(3)
        return true
    }

}