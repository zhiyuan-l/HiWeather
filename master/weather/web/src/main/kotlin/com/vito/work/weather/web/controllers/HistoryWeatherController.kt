package com.vito.work.weather.web.controllers

import com.vito.work.weather.domain.config.Constant
import com.vito.work.weather.domain.entities.HistoryWeather
import com.vito.work.weather.domain.services.HistoryWeatherService
import com.vito.work.weather.domain.util.http.ObjectResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDate
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/16.
 * Time : 上午10:50.
 * Description:
 *
 */

@Controller
@RequestMapping("/weather/history")
open class HistoryWeatherController {
    @Resource
    lateinit var historyWeatherService: HistoryWeatherService

    @RequestMapping("/")
    open fun index(): String {
        return "weather/history/index"
    }

    /**
     * 获取过去每年和今天日期相同的当天天气
     * */
    @RequestMapping("/today")
    @ResponseBody
    open fun today(@RequestParam cityId: Long): ObjectResponse {
        val list = historyWeatherService.findHistoryWeathersOfToday(cityId)
        val response = ObjectResponse(list)
        return response
    }

    /**
     * 获取特定城市历史纪录之最
     * */
    @RequestMapping("/tops")
    @ResponseBody
    open fun tops(@RequestParam cityId: Long): ObjectResponse {
        val list = historyWeatherService.findHistoryTops(cityId)
        val response = ObjectResponse(list)
        return response
    }

    /**
     * 查询特定城市特定日期的历史天气
     * 查询类型有两种: 按月查询, 按天查询
     * */
    @RequestMapping("/query")
    @ResponseBody
    open fun query(@RequestParam cityId: Long, @RequestParam date: String, @RequestParam(required = true) type: Int): ObjectResponse {
        var data: Any = listOf<Any>()

        when (type) {
            Constant.HISTORY_QUERY_TYPE_DAY   -> {
                data = listOf(historyWeatherService.findByDate(cityId, LocalDate.parse(date)))
            }
            Constant.HISTORY_QUERY_TYPE_MONTH -> {
                data = historyWeatherService.findByMonth(cityId, LocalDate.parse(date)) ?: listOf<HistoryWeather>()
            }
        }
        val response = ObjectResponse(data)
        return response
    }
}