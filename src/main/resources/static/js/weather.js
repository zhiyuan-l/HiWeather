/**
 * Created by lingzhiyuan on 16/4/21.
 */

// 全局变量-所有的天气编码数组
window.acodes = []
window.dcodes = []
window.wcodes = []
window.fcodes = []


function WeatherCode(code, name_ch, name_en){
    this.code = code
    this.name_ch = name_ch
    this.name_en = name_en
}

function DirectionCode(code, name_ch, name_en){
    this.code = code
    this.name_ch = name_ch
    this.name_en = name_en
}

function ForceCode(code, name_ch, level){
    this.code = code
    this.name_ch = name_ch
    this.level = level
}

function ForecastWeather(district, w_day, w_night, t_day, t_night, wd_day, wd_night, wf_day, wf_night, sunrise, sunset) {
    this.district = district
    this.w_day = w_day
    this. w_night = w_night
    this.t_day = t_day
    this.t_night = t_night
    this.wd_day = wd_day
    this.wd_night = wd_night
    this.wf_day = wf_day
    this.wf_night = wf_night
    this.sunrise = sunrise
    this.sunset = sunset
}

function CurrentWeather(district, weather, temperature, wind_direction, wind_force, aqi, humidity) {

    this.district = district
    this.weather = weather
    this.temperature = temperature
    this.wind_direction = wind_direction
    this.wind_force = wind_force
    this.aqi = aqi
    this.humidity = humidity

}

function AQI(value, pm25, pm10, so2, no2, co, o3, major) {

    this.value = value
    this.pm25 = pm25
    this.pm10 = pm10
    this.so2 = so2
    this.co = co
    this.no2 = no2
    this.o3 = o3
    this.major = major
}

/**
 * 根据 aqi 的数值判断其所在的等级
 *
 * 未找到则返回0
 * */
var findAQICodeByValue = function (value) {
    if(value >= 0 && value <= 50){
        return 1
    }else if(value >=51 && value <= 100){
        return 2
    }else if(value >= 101 && value <= 150){
        return 3
    }else if(value >= 151 && value <= 200){
        return 4
    }else if(value >= 201 && value <= 300){
        return 5
    }else if(value > 300){
        return 6
    }else{
        return 0
    }
}

function AQICode(code, name){
    this.code = code
    this.name = name
}

/**
 * 根据 code 找到对应的 AQI 编码
 * */
var findAQICodeByCode = function (code) {
    var acodes = window.acodes
    var result = ""
    $.each(acodes, function (index, val) {
        if(val.code == code){
            result = val
        }
    })
    return result
}

/**
 * 获取特定区县的实时天气
 * */
var getCurrentWeather = function (districtId) {

    var result = null
    $.ajax({
        url: "/weather/today/now",
        data: {districtId: districtId},
        dataType: "json",
        async: false,
        success: function (data) {
            var weather = data.data
            var district = findDistrictById(weather.district)
            result = new CurrentWeather(district, weather.weather, weather.temperature, weather.wind_direction, weather.wind_force, weather.aqi, weather.humidity)
        }
    })

    return result
}

/**
 * 获取*/
var getTodayWeather = function (districtId) {
    var result = null
    $.ajax({
        url: "/weather/forecast/today",
        data: {districtId: districtId},
        dataType: "json",
        async: false,
        success: function (data) {
            var weather = data.data
            var district = findDistrictById(weather.district)
            result = new ForecastWeather(district, weather.weather_day,weather.weather_night, weather.max, weather.min, weather.wind_direction_day,weather.wind_direction_night, weather.wind_force_day,weather.wind_force_day, weather.sunrise, weather.sunset)
        }
    })

    return result
}

var findWeatherCodeByCode = function (code) {
    var wcodes = window.wcodes
    var result = ""
    $.each(wcodes, function (index, val) {
        if(val.code == code){
            result = val
        }
    })

    return result
}

var parseWeatherCodeStr = function (codeStr) {

    var codes = codeStr.toString().split(",")
    var result = findWeatherCodeByCode(codes[0]).name_ch
    if(codes.length > 1){
        if(codes[0] != codes[1]) {
            result += "转"
            result += findWeatherCodeByCode(codes[1]).name_ch
        }
    }

    return result
}

var findWindDirectionCodeByCode = function(code){
    var dcodes = window.dcodes
    var result = ""
    $.each(dcodes, function (index, val) {
        if(val.code == code){
            result = val
        }
    })

    return result
}

var findWindForceCodeByCode = function (code) {
    var fcodes = window.fcodes
    var result = ""
    $.each(fcodes, function (index, val) {
        if(val.code == code){
            result = val
        }
    })
    return result
}

var parseForceCodeStr = function (codeStr) {

    var codes = codeStr.toString().split(",")
    var result = findWindForceCodeByCode(codes[0]).name_ch
    if(codes.length > 1){
        result += "转"
        result += findWindForceCodeByCode(codes[1]).name_ch
    }

    return result
}


var getCodes = function (url) {
    $.ajaxSettings.async = false;
    $.getJSON(url, function (data) {
        var temp0 = data.acodes
        $.each(temp0, function (index, val) {
            window.acodes.push(new AQICode(val.code, val.name))
        })
        var temp1 = data.wcodes
        $.each(temp1, function (index, val) {
            window.wcodes.push(new WeatherCode(val.code, val.name_ch, val.name_en))
        })
        var temp2 = data.fcodes
        $.each(temp2, function (index, val) {
            window.fcodes.push(new ForceCode(val.code, val.name_ch, val.level))
        })
        var temp3 = data.dcodes
        $.each(temp3, function (index, val) {
            window.dcodes.push(new DirectionCode(val.code, val.name_ch, val.name_en))
        })
    })
    $.ajaxSettings.async = true;
}

var aqiCodeToClass = {
    0: "btn-default",
    1: "btn-success",
    2: "btn-info",
    3: "btn-primary",
    4: "btn-warning",
    5: "btn-danger",
    6: "btn-danger"
}

var codeToClass = {
    0:"wi-day-sunny",
    1:"wi-day-sunny-overcast",
    2:"wi-day-cloudy",
    3:"wi-showers",
    4:"wi-storm-showers",
    5:"wi-storm-showers",
    6:"wi-sleet",
    7:"wi-raindrop",
    8:"wi-raindrops",
    9:"wi-rain",
    10:"wi-rain",
    11:"wi-rain",
    12:"wi-rain",
    13:"wi-snowflake-cold",
    14:"wi-snow",
    15:"wi-snow-wind",
    16:"wi-snow",
    17:"wi-snow",
    18:"wi-fog",
    19:"wi-sprinkle",
    20:"wi-sandstorm",
    21:"wi-rain",
    22:"wi-rain",
    23:"wi-rain",
    24:"wi-rain",
    25:"wi-rain",
    26:"wi-snow",
    27:"wi-snow",
    28:"wi-snow",
    29:"wi-dust",
    30:"wi-sandstorm",
    31:"wi-sandstorm",
    53:"wi-smog",
    99:"wi-na"
}