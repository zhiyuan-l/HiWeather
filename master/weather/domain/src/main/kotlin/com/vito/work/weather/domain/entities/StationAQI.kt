package com.vito.work.weather.domain.entities

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.persistence.*

/**
 * Created by lingzhiyuan.
 * Date : 16/4/17.
 * Time : 下午4:53.
 * Description:
 *
 */

@Entity
@Table(name = "aqi_station")
data class StationAQI(

        @Id
        @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
        var id: Long = 0L,
        var station: Long = 0L,
        @Transient
        var station_name: String = "",
        var datetime: Timestamp = Timestamp.valueOf(LocalDateTime.now()),
        var value: Int = 0, // 数值
        var PM25: Int = 0, //pm2.5 数值
        var PM10: Int = 0,
        var O3: Int = 0,  // 臭氧数值
        var major: Int = 0 // 主要污染物
                     )