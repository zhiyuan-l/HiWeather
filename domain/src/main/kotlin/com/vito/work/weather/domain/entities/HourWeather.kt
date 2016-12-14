package com.vito.work.weather.domain.entities

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.persistence.*

/**
 * Created by lingzhiyuan.
 * Date : 16/4/16.
 * Time : 上午11:21.
 * Description:
 *
 */

@Entity
@Table(name = "weather_hour")
data class HourWeather(

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long = 0L,
        // 当前的时间, 精确到小时
        var datetime: Timestamp = Timestamp.valueOf(LocalDateTime.now()),
        var temperature: Int = 0,

        // 降水
        var precipitation: Double = .0,
        // 湿度
        var humidity: Int = 0,
        var wind_direction: Int = 0,
        var wind_force: Int = 0,
        // 空气质量
        var aqi: Int = 0,
        // 所属区县
        var district: Long = 0L,
        var update_time: Timestamp = Timestamp.valueOf(LocalDateTime.now())
                      )