package com.vito.work.weather.dto

import com.vito.work.weather.config.Constant
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.persistence.*

/**
 * Created by lingzhiyuan.
 * Date : 16/4/8.
 * Time : 上午10:26.
 * Description:
 *
 */

@Entity
@Table(name = "url")
data class Url(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long = 0L,
        var url: String = "",
        var type: Int = 0,
        var status: Int = Constant.URL_STATUS_UNFINISHED,
        var update_time: Timestamp = Timestamp.valueOf(LocalDateTime.now())
              )