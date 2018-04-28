package com.vito.work.weather.web.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Created by lingzhiyuan.
 * Date : 16/4/10.
 * Time : 上午11:47.
 * Description:
 *
 */

@Controller
class WebBasicController {
    @RequestMapping("/")
    fun toIndex(): String {
        return "web/index"
    }

}
