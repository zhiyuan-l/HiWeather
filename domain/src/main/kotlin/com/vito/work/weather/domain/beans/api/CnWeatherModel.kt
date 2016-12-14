package com.vito.work.weather.domain.beans.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by lingzhiyuan.
 * Date : 16/4/19.
 * Time : 下午11:22.
 * Description:
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class CnWeatherNowAPIModel(
        @JsonProperty("nameen")
        var nameen: String = "",
        @JsonProperty("cityname")
        var cityname: String = "",
        @JsonProperty("city")
        var city: String = "",
        @JsonProperty("temp")
        var temp: String = "",
        @JsonProperty("tempf")
        var tempf: String = "",
        @JsonProperty("WD")
        var WD: String = "",
        @JsonProperty("wde")
        var wde: String = "",
        @JsonProperty("WS")
        var WS: String = "",
        @JsonProperty("wse")
        var wse: String = "",
        @JsonProperty("time")
        var time: String = "0",
        @JsonProperty("weather")
        var weather: String? = "",
        @JsonProperty("weathere")
        var weathere: String? = "0",
        @JsonProperty("weathercode")
        var weathercode: String = "0",
        // 气压
        @JsonProperty("qy")
        var qy: String? = "0",
        // 能见度
        @JsonProperty("njd")
        var njd: String = "",
        @JsonProperty("sd")
        var sd: String = "0%",
        @JsonProperty("rain", required = false)
        var rain: String? = null,
        @JsonProperty("aqi")
        var aqi: String? = null,
        @JsonProperty("limitnumber")
        var limitnumber: String? = null,
        @JsonProperty("aqi_pm25")
        var aqi_pm25: String? = null,
        @JsonProperty("date")
        var date: String? = "0"
                               )

data class CnWeatherModel(
        var c: ModelC = ModelC(),
        var f: ModelF = ModelF()
                         )

data class ModelC(
        var c1: String = "",
        var c2: String = "",
        var c3: String = "",
        var c4: String = "",
        var c5: String = "",
        var c6: String = "",
        var c7: String = "",
        var c8: String = "",
        var c9: String = "",
        var c10: String = "",
        var c11: String = "",
        var c12: String = "",
        var c13: Double = 0.0,
        var c14: Double = 0.0,
        var c15: String = "",
        var c16: String = "",
        var c17: String = ""
                 )

data class ModelF(
        var f1: List<ModeF1> = listOf(),
        var f0: String = ""
                 )

data class ModeF1(
        var fa: String = "",
        var fb: String = "",
        var fc: String = "",
        var fd: String = "",
        var fe: String = "",
        var ff: String = "",
        var fg: String = "",
        var fh: String = "",
        var fi: String = ""
                 )