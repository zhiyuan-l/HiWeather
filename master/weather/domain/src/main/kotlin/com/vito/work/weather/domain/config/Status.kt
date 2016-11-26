package com.vito.work.weather.domain.config

/**
 * Created by lingzhiyuan.
 * Date : 16/4/19.
 * Time : 下午4:49.
 * Description:
 *
 * 爬虫更新任务的状态（锁）
 *
 */
class SpiderStatus {

    companion object {

        var FORECAST_UPDATE_STATUS: Boolean = false
        var HISTORY_UPDATE_STATUS: Boolean = false
        var TODAY_WEATHER_STATUS: Boolean = false
    }

}