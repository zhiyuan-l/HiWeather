package com.vito.work.weather.domain.entities

import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

/**
 * Created by lingzhiyuan.
 * Date : 16/4/11.
 * Time : 上午9:17.
 * Description:
 *
 */

@Entity
@Table(name = "aqi")
data class AQI(

        @Id
        @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
        var id: Long = 0L,
        // 日期
        var date: Date = Date(0),
        var value: Int = - 1, // 数值
        var PM25: Int = 0, //pm2.5 数值
        var O3: Int = 0, // 臭氧数值
        var SO2: Int = 0,
        var NO2: Int = 0,
        var CO: Int = 0,
        var PM10: Int = 0,
        var major: Int = 0, // 主要污染物
        var district: Long = 0L, // 区县
        var update_time: Timestamp = Timestamp.valueOf(LocalDateTime.now()) // 更新的时间
              )