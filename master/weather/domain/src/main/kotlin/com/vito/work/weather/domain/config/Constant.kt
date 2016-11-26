package com.vito.work.weather.domain.config

import java.time.LocalDate

/**
 * Created by lingzhiyuan.
 * Date : 16/4/3.
 * Time : 下午3:12.
 * Description:
 *
 */
class Constant {

    companion object {

        val CNWEATHER_BASE_URL = "http://open.weather.com.cn/data/"
        val CNWEATHER_APPID = "d8729751835749e4"
        val CNWEATHER_PRIVATE_KEY = "4513dd_SmartWeatherAPI_2b1cd15"

        val LOCATION_SOURCE_URL: String = "http://www.tianqi.com/weather.php?a=getZoneInfo"
        val HISTORY_WEATHER_BASE_URL: String = "http://lishi.tianqi.com"
        val FORCAST_WEATHER_BASE_URL: String = "http://www.tianqi.com"
        val AQI_BASE_URL: String = "http://www.tianqi.com/air/"
        val TODAY_BASE_URL: String = "http://city.tianqi.com/district/today/"
        val SPIDER_THREAD_COUNT = 15
        val SPIDER_HISTORY_START_DATE: LocalDate = LocalDate.of(2011, 1, 1)

        val URL_TYPE_WEATHER_CURRENT: Int = 10
        val URL_TYPE_WEATHER_FORECAST: Int = 20
        val URL_TYPE_WEATHER_HISTORY: Int = 30
        val URL_TYPE_DEFAULT = 100

        val URL_TYPE_WEATHER_AQI: Int = 40
        val URL_TYPE_LOCATION_PROVINCE: Int = 60
        val URL_TYPE_LOCATION_CITY: Int = 70
        val URL_TYPE_LOCATION_DISTRICT: Int = 80
        val URL_STATUS_FINISHED = 10
        val URL_STATUS_UNFINISHED = 20

        val HISTORY_QUERY_TYPE_DAY: Int = 10
        val HISTORY_QUERY_TYPE_MONTH: Int = 20

        val FORECAST_WEATHER_UPDATE_TYPE_ALL: Int = 10
        val FORECAST_WEATHER_UPDATE_TYPE_CITY: Int = 20

        val HISTORY_WEATHER_UPDATE_TYPE_ALL: Int = 10
        val HISTORY_WEATHER_UPDATE_TYPE_PROVINCE: Int = 20
        val HISTORY_WEATHER_UPDATE_TYPE_CITY: Int = 30
        val DISTRICT_API_FILE_LOCATION: String = "/public/cnweather_api_districts.txt"
        val CNWEATHER_24H_API_BASE_URL: String = "http://flash.weather.com.cn/sk2/districtId.xml"


    }

}


