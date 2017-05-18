package com.vito.work.weather.config

import java.time.LocalDate

/**
 * Created by lingzhiyuan.
 * Date : 16/4/3.
 * Time : 下午3:12.
 * Description:
 *
 */
object Constant {

    const val CNWEATHER_BASE_URL = "http://open.weather.com.cn/data/"
    const val CNWEATHER_APPID = "d8729751835749e4"
    const val CNWEATHER_PRIVATE_KEY = "4513dd_SmartWeatherAPI_2b1cd15"
    const val LOCATION_SOURCE_URL: String = "http://www.tianqi.com/weather.php?a=getZoneInfo"
    const val CNWEATHER_24H_API_BASE_URL: String = "http://flash.weather.com.cn/sk2/districtId.xml"
    const val HISTORY_WEATHER_BASE_URL: String = "http://lishi.tianqi.com"
    const val FORCAST_WEATHER_BASE_URL: String = "http://www.tianqi.com"
    const val AQI_BASE_URL: String = "http://www.tianqi.com/air/"
    const val TODAY_BASE_URL: String = "http://city.tianqi.com/district/today/"
    const val SPIDER_THREAD_COUNT = 15
    val SPIDER_HISTORY_START_DATE: LocalDate = LocalDate.of(2011, 1, 1)
    const val URL_TYPE_WEATHER_CURRENT: Int = 10
    const val URL_TYPE_WEATHER_FORECAST: Int = 20
    const val URL_TYPE_WEATHER_HISTORY: Int = 30
    const val URL_TYPE_DEFAULT = 100
    const val URL_TYPE_WEATHER_AQI: Int = 40
    const val URL_TYPE_LOCATION_PROVINCE: Int = 60
    const val URL_TYPE_LOCATION_CITY: Int = 70
    const val URL_TYPE_LOCATION_DISTRICT: Int = 80
    const val URL_STATUS_FINISHED = 10
    const val URL_STATUS_UNFINISHED = 20
    const val HISTORY_QUERY_TYPE_DAY: Int = 10
    const val HISTORY_QUERY_TYPE_MONTH: Int = 20
    const val FORECAST_WEATHER_UPDATE_TYPE_ALL: Int = 10
    const val FORECAST_WEATHER_UPDATE_TYPE_CITY: Int = 20
    const val HISTORY_WEATHER_UPDATE_TYPE_ALL: Int = 10
    const val HISTORY_WEATHER_UPDATE_TYPE_PROVINCE: Int = 20
    const val HISTORY_WEATHER_UPDATE_TYPE_CITY: Int = 30
    const val DISTRICT_API_FILE_LOCATION: String = "/public/cnweather_api_districts.txt"

}


