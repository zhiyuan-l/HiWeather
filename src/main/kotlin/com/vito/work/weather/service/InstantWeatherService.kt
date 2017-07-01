package com.vito.work.weather.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.vito.work.weather.domain.beans.api.CnWeatherNowAPIModel
import com.vito.work.weather.dto.District
import com.vito.work.weather.dto.InstantWeather
import com.vito.work.weather.repo.DistrictDao
import com.vito.work.weather.repo.InstantWeatherDao
import com.vito.work.weather.service.spider.AbstractSpiderTask
import com.vito.work.weather.util.cnweather.Weather
import com.vito.work.weather.util.cnweather.chooseLevel
import com.vito.work.weather.util.cnweather.findByWindDirectionName
import com.vito.work.weather.util.cnweather.invokeAPI
import com.vito.work.weather.util.http.BusinessError
import com.vito.work.weather.util.http.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.annotation.Resource

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
class InstantWeatherService : AbstractSpiderTask() {
    @Resource
    lateinit var instantWeatherDao: InstantWeatherDao
    @Resource
    lateinit var districtDao: DistrictDao

    companion object {
        val logger = LoggerFactory.getLogger(HourWeatherService::class.java)
    }

    /**
     * 根据区县的 id 获取最近的天气
     *
     * 没找到则抛出资源未找到的异常
     *
     * */
    fun findLatestInstantWeather(districtId: Long): InstantWeather {
        val weather: InstantWeather = instantWeatherDao.findLatestByDistrictId(districtId) ?: throw BusinessException(BusinessError.ERROR_RESOURCE_NOT_FOUND)

        return weather
    }

    fun execute() {
        task {
            val districts: List<District?> = districtDao.findAll(District::class.java)
            districts.forEach {
                val weather = fetchAndSaveInstantWeather(it?.id ?: - 1)
                if (weather != null) {
                    instantWeatherDao save weather
                }
            }
        }
    }
}

private fun fetchAndSaveInstantWeather(districtId: Long): InstantWeather? {
    val jsonDataList = invokeAPI(districtId.toString()).removePrefix("var dataSK =").replace("SD", "sd2")

    val mapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val weather: InstantWeather
    try {
        val weatherModel = mapper.readValue(jsonDataList, CnWeatherNowAPIModel::class.java)
        weather = InstantWeather()
        with(weatherModel) wm@ {
            val date = LocalDate.now()
            val time = LocalTime.parse(this@wm.time.trim(), DateTimeFormatter.ofPattern("HH:mm"))
            val datetime = LocalDateTime.of(date, time)

            this@wm.date = this@wm.date?.split("(")?.first()?.trim() ?: "${LocalDate.now().monthValue}月${LocalDate.now().dayOfMonth}日"
            this@wm.sd = if (this@wm.sd.trim() != "暂无实况" && this@wm.sd.trim() != "") this@wm.sd.trim().removeSuffix("%") else "-1"
            this@wm.WS = this@wm.WS.replace("级", "").trim()

            with(weather) w@ {
                aqi = if (! this@wm.aqi.isNullOrBlank() && this@wm.aqi?.trim() != "?" && this@wm.aqi?.trim() != "—") this@wm.aqi !!.trim().toInt() else - 1
                pm25 = if (! this@wm.aqi_pm25.isNullOrBlank()) this@wm.aqi_pm25 !!.trim().toInt() else - 1
                this.weather = if (this@wm.weathercode.trim() != "暂无实况") this@wm.weathercode.trim().substring(1).toInt() else Weather.UNKNOWN.code
                this.datetime = Timestamp.valueOf(datetime)
                precipitation = if (! this@wm.rain.isNullOrBlank() && this@wm.rain?.trim() != "暂无实况") this@wm.rain !!.trim().toDouble() else - 1.0
                humidity = if (this@wm.sd != "") this@wm.sd.trim().toInt() else - 1
                district = this@wm.city.trim().toLong()
                temperature = if (this@wm.temp != "") this@wm.temp.toDouble() else - 273.0
                wind_direction = findByWindDirectionName(this@wm.WD).code
                wind_force = chooseLevel(this@wm.WS.toIntOrNull()).code
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
        return null
    }
    return weather
}