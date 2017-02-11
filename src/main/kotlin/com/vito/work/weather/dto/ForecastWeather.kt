package com.vito.work.weather.dto

import java.sql.Date
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
@Table(name = "weather_forecast")
data class ForecastWeather(

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long = 0L,
        var district: Long = 0L,
        var date: Date = Date(0),
        var max: Int = 0,
        var min: Int = 0,
        @Column(length = 10)
        var weather_day: String = "",
        @Column(length = 10)
        var weather_night: String = "",
        var wind_direction_day: Int = 0,
        var wind_direction_night: Int = 0,
        @Column(length = 10)
        var wind_force_day: String = "",
        @Column(length = 10)
        var wind_force_night: String = "",
        @Column(length = 10)
        var sunrise: String = "",
        @Column(length = 10)
        var sunset: String = "",
        var update_time: Timestamp = Timestamp.valueOf(LocalDateTime.now())
                          )