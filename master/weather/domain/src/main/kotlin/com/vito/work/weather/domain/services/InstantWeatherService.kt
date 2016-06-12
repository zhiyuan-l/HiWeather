package com.vito.work.weather.domain.services

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.vito.work.weather.domain.beans.api.CnWeatherNowAPIModel
import com.vito.work.weather.domain.config.SpiderStatus
import com.vito.work.weather.domain.daos.InstantWeatherDao
import com.vito.work.weather.domain.daos.LocationDao
import com.vito.work.weather.domain.entities.District
import com.vito.work.weather.domain.entities.InstantWeather
import com.vito.work.weather.domain.util.cnweather.Weather
import com.vito.work.weather.domain.util.cnweather.chooseLevel
import com.vito.work.weather.domain.util.cnweather.findByWindDirectionName
import com.vito.work.weather.domain.util.cnweather.invokeAPI
import com.vito.work.weather.domain.util.http.BusinessError
import com.vito.work.weather.domain.util.http.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Created by lingzhiyuan.
 * Date : 16/4/16.
 * Time : 上午11:21.
 * Description:
 *
 *管理即时的天气
 *
 */

@Service("instantWeatherService")
@Transactional
open class InstantWeatherService @Autowired constructor(val instantWeatherDao: InstantWeatherDao, val locationDao: LocationDao)
{

    companion object
    {
        val logger = LoggerFactory.getLogger(HourWeatherService::class.java)
    }

    /**
     * 根据区县的 id 获取最近的天气
     *
     * 没找到则抛出资源未找到的异常
     *
     * */
    open fun findLatestInstantWeather(districtId: Long): InstantWeather
    {

        var weather: InstantWeather = instantWeatherDao.findLatestByDistrictId(districtId) ?: throw BusinessException(BusinessError.ERROR_RESOURCE_NOT_FOUND)

        return weather
    }

    open fun updateFromWeb(district: District)
    {
        try
        {
            SpiderStatus.INSTANT_WEATHER_UPDATE_STATUS = true
            var weather: InstantWeather = fetchAndSaveInstantWeather(district.id)
            instantWeatherDao.saveOrUpdate(weather)
        }
        catch(ex: Exception)
        {
            throw ex
        }
        finally
        {
            SpiderStatus.INSTANT_WEATHER_UPDATE_STATUS = false
        }
    }
}

private fun fetchAndSaveInstantWeather(districtId: Long): InstantWeather
{
    var jsonDataList = invokeAPI(districtId.toString()).removePrefix("var dataSK =").replace("SD", "sd2")

    var mapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    var weatherModel = mapper.readValue(jsonDataList, CnWeatherNowAPIModel::class.java)
    var weather = InstantWeather()

    with(weatherModel) wm@{
        var date = LocalDate.now()
        var time = LocalTime.parse("${this@wm.time.trim()}", DateTimeFormatter.ofPattern("HH:mm"))
        var datetime = LocalDateTime.of(date, time)

        this@wm.date = this@wm.date?.split("(")?.first()?.trim() ?: "${LocalDate.now().monthValue}月${LocalDate.now().dayOfMonth}日"
        this@wm.sd = if(this@wm.sd.trim() != "暂无实况" && this@wm.sd.trim() != "") this@wm.sd.trim().removeSuffix("%") else "-1"
        this@wm.WS = this@wm.WS.replace("级", "").trim()

        with(weather) w@{
            aqi = if(this@wm.aqi != null && this@wm.aqi?.trim() != "" && this@wm.aqi?.trim() != "?") this@wm.aqi?.trim()?.toInt() else -1
            pm25 = if(this@wm.aqi_pm25 != null && this@wm.aqi_pm25?.trim() != "") this@wm.aqi_pm25?.trim()?.toInt() else -1
            this.weather = if(this@wm.weathercode.trim() != "暂无实况") this@wm.weathercode.trim().substring(1).toInt() else Weather.UNKNOWN.code
            this.datetime = Timestamp.valueOf(datetime)
            precipitation = if(this@wm.rain != null && this@wm.rain?.trim() != "暂无实况") this@wm.rain?.trim()?.toDouble() else -1.0
            humidity = if(this@wm.sd != "") this@wm.sd.trim().toInt() else -1
            district = this@wm.city.trim().toLong()
            temperature = if(this@wm.temp != "") this@wm.temp.toDouble() else -273.0
            wind_direction = findByWindDirectionName(this@wm.WD).code
            wind_force = chooseLevel(this@wm.WS.toInt()).code
        }

    }
    return weather
}