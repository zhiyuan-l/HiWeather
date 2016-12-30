package com.vito.work.weather.domain.entities

import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.persistence.*

/**
 * Created by lingzhiyuan.
 * Date : 16/4/5.
 * Time : 下午6:25.
 * Description:
 *
 */

@Entity
@Table(name = "weather_history")
data class HistoryWeather(

        @Id
        @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
        var id: Long = 0L,
        var city: Long = 0L,
        var max: Int = 0,
        var min: Int = 0,
        @Column(length = 20)
        var weather: String = "",
        @Column(length = 20)
        var wind_direction: String = "",
        @Column(length = 20)
        var wind_force: String = "",
        var date: Date = Date(0),
        var update_time: Timestamp = Timestamp.valueOf(LocalDateTime.now())
                         )