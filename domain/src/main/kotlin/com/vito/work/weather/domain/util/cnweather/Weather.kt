package com.vito.work.weather.domain.util.cnweather

/**
 * Created by lingzhiyuan.
 * Date : 16/4/17.
 * Time : 上午11:04.
 * Description:
 *
 */

/**
 * 天气编码
 * */
enum class Weather(val code: Int, val names: Array<String>) {
    SUNNY(0, arrayOf("晴", "Sunny")),
    CLOUDY(1, arrayOf("多云", "Cloudy")),
    OVERCAST(2, arrayOf("阴", "Overcast")),
    SHOWER(3, arrayOf("阵雨", "Shower")),
    THUNDERSHOWER(4, arrayOf("雷阵雨", "Thundershower", "雷雨")),
    THUNDERSHOWER_WITH_HAIL(5, arrayOf("雷阵雨伴有冰雹", "Thundershower with hail")),
    SLEET(6, arrayOf("雨夹雪", "Sleet")),
    LIGHT_RAIN(7, arrayOf("小雨", "Light rain")),
    MODERATE_RAIN(8, arrayOf("中雨", "Moderate rain")),
    HEAVY_RAIN(9, arrayOf("大雨", "Heavy rain")),
    STORM(10, arrayOf("暴雨", "Storm")),
    HEAVY_STORM(11, arrayOf("大暴雨", "Heavy storm")),
    SEVERE_STORM(12, arrayOf("特大暴雨", "Severe storm")),
    SNOW_FLURRY(13, arrayOf("阵雪", "Snow flurry")),
    LIGHT_SNOW(14, arrayOf("小雪", "Light snow")),
    MODERATE_SNOW(15, arrayOf("中雪", "Moderate snow")),
    HEAVEY_SNOW(16, arrayOf("大雪", "Heavy snow")),
    SNOWSTORM(17, arrayOf("暴雪", "Snowstorm")),
    FOGGY(18, arrayOf("雾", "Foggy")),
    ICE_RAIN(19, arrayOf("冻雨", "Ice rain")),
    DUSTSTORM(20, arrayOf("沙尘暴", "Duststorm")),
    LIGHT_TO_MODETATE_RAIN(21, arrayOf("小到中雨", "Light to moderate rain")),
    MODERATE_TO_HEAVY_RAIN(22, arrayOf("中到大雨", "Moderate to heavy rain")),
    HEAVY_RAIN_TO_STORM(23, arrayOf("大到暴雨", "Heavy rain to storm")),
    STORM_TO_HEAVY_STORM(24, arrayOf("暴雨到大暴雨", "Storm to heavy storm")),
    HEAVY_TO_SEVERE_STORM(25, arrayOf("大暴雨到特大暴雨", "Heavy to severe storm")),
    LIGHT_TO_MODERATE_SNOW(26, arrayOf("小到中雪", "Light to moderate snow")),
    MODETATE_TO_HEAVY_SNOW(27, arrayOf("中到大雪", "Moderate to heavy snow")),
    HEAVY_SNOW_TO_SNOWSTORM(28, arrayOf("大到暴雪", "Heavy snow to snowstorm")),
    DUST(29, arrayOf("浮尘", "Dust")),
    SAND(30, arrayOf("扬沙", "Sand")),
    SANDSTORM(31, arrayOf("强沙尘暴", "Sandstorm")),
    HAZE(53, arrayOf("霾", "Haze")),
    UNKNOWN(99, arrayOf("无", "Unknown"));
}

fun findByWeatherCode(code: String): Weather {

    if (code.trim() == "") {
        return Weather.UNKNOWN
    }

    try {
        var codeValue = code.toInt()
        for (it in Weather.values()) {
            if (it.code == codeValue) {
                return it
            }
        }
    } catch(ex: Exception) {
        ex.printStackTrace()
    }

    return Weather.UNKNOWN
}

fun findByWeatherName(name: String): Weather {
    val name = name.trim()
    for (it in Weather.values()) {
        if (it.names.any { it == name }) {
            return it
        }
    }

    return Weather.UNKNOWN
}


/**
 * 风力编码
 * */
enum class WindForce(val code: Int, val name_ch: String, val level: String, val min: Int, val max: Int) {
    LEVEL_ZERO(0, "微风", "<10m/h", 0, 3),
    LEVEL_ONE(1, "3-4级", "10~17m/h", 3, 4),
    LEVEL_TWO(2, "4-5级", "17~25m/h", 4, 5),
    LEVEL_THREE(3, "5-6级", "25~34m/h", 5, 6),
    LEVEL_FOUR(4, "6-7级", "34~43m/h", 6, 7),
    LEVEL_FIVE(5, "7-8级", "43~54m/h", 7, 8),
    LEVEL_SIX(6, "8-9级", "54~65m/h", 8, 9),
    LEVEL_SEVEN(7, "9-10级", "65~77m/h", 9, 10),
    LEVEL_EIGHT(8, "10-11级", "77~89m/h", 10, 11),
    LEVEL_NINE(9, "11-12级", "89~102m/h", 11, 12);
}

/**
 * 根据风力的等级数判断所处的等级区间
 * */
fun chooseLevel(num: Int): WindForce {
    for (it in WindForce.values()) {
        if (num >= it.min && num <= it.max) {
            return it
        }
    }
    return WindForce.LEVEL_ZERO
}

/**
 * 根据风力的等级码返回对应的等级
 * */
fun findByWindForceCode(code: String): WindForce {

    if (code.trim() == "") {
        return WindForce.LEVEL_ZERO
    }

    try {
        var codeValue = code.toInt()
        for (it in WindForce.values()) {
            if (it.code == codeValue) {
                return it
            }
        }
    } catch(ex: Exception) {
        ex.printStackTrace()
    }

    return WindForce.LEVEL_ZERO
}

/**
 * 根据名称找到对应的等级
 * */
fun findByWindForceName(name: String): WindForce {
    for (it in WindForce.values()) {
        if (it.name_ch == name) {
            return it
        }
    }
    return WindForce.LEVEL_ZERO
}


/**
 * 风向编码
 * */
enum class WindDirection(val code: Int, val name_ch: String, val name_en: String) {
    NOWIND(0, "无持续风向", "No wind"),
    NORTHEAST(1, "东北风", "Northeast"),
    EAST(2, "东风", "East"),
    SOUTHEAST(3, "东南风", "Southeast"),
    SOUTH(4, "南风", "South"),
    SOUTHWEST(5, "西南风", "Southweast"),
    WEST(6, "西风", "West"),
    NORTHWEST(7, "西北风", "Northwest"),
    NORTH(8, "北风", "North"),
    WHIRLWIND(9, "旋转风", "Whirl wind");
}

/**
 * 根据风向的编码找到对应项
 * */
fun findByWindDirectionCode(code: String): WindDirection {
    if (code.trim() == "") {
        return WindDirection.NOWIND
    }

    try {
        var codeValue = code.toInt()
        for (it in WindDirection.values()) {
            if (it.code == codeValue) {
                return it
            }
        }
    } catch(ex: Exception) {
        ex.printStackTrace()
    }

    return WindDirection.NOWIND
}

fun findByWindDirectionName(name: String): WindDirection {
    for (it in WindDirection.values()) {
        if (it.name_ch == name || it.name_en == name) {
            return it
        }
    }

    return WindDirection.NOWIND
}

