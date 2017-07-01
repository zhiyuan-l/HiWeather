package com.vito.work.weather.dto

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.persistence.*

/**
 * Created by lingzhiyuan.
 * Date : 16/4/10.
 * Time : 下午9:30.
 * Description:
 *
 */

@Entity
@Table(name = "weather_instant")
data class InstantWeather(

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long = 0L,
        var district: Long = 0L,
        var weather: Int = 0,
        var datetime: Timestamp = Timestamp.valueOf(LocalDateTime.now()),
        var temperature: Double = .0,
        // 体感温度
        var temperature_sensible: Double = .0,

        // 降水
        var precipitation: Double = .0,
        // 湿度
        var humidity: Int = 0,
        var pm25: Int = 0,

        var wind_direction: Int = 0,
        var wind_force: Int = 0,
        // 空气质量
        var aqi: Int = 0
                         )