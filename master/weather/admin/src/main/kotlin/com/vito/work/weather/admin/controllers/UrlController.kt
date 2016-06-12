package com.vito.work.weather.admin.controllers

import com.vito.work.weather.domain.config.Constant
import com.vito.work.weather.domain.services.UrlService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Created by lingzhiyuan.
 * Date : 16/4/12.
 * Time : 下午9:56.
 * Description:
 *
 */

@Controller
@RequestMapping("/url")
class UrlController @Autowired constructor(val urlService: UrlService)
{

    @RequestMapping("/delete/history")
    open fun removeHistoryUrls()
    {
        urlService.deleteAll(type = Constant.URL_TYPE_WEATHER_HISTORY)
    }

}