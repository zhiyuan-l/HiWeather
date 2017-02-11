package com.vito.work.weather.config

/**
 * Created by lingzhiyuan.
 * Date : 16/4/17.
 * Time : 下午5:02.
 * Description:
 *
 */

enum class AQIPrimaryType(val code: Int, val name_1: String, val name_2: String = "NONE") {

    PM25(1, "PM2.5", "P2.5"),
    PM10(2, "PM10", "P10"),
    O3(3, "臭氧"),
    SO2(4, "二氧化硫"),
    NO2(5, "二氧化氮"),
    CO(6, "一氧化碳"),
    UNKNOWN(10, "无", "暂无数据");

}

fun getAQITypeCodeByName(name: String): AQIPrimaryType {

    return AQIPrimaryType.values().firstOrNull { it -> it.name_1 == name.trim() || it.name_2 == name.trim() }
           ?: AQIPrimaryType.UNKNOWN
}