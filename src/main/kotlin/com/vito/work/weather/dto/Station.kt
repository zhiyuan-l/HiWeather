package com.vito.work.weather.dto

import javax.persistence.*

/**
 * Created by lingzhiyuan.
 * Date : 16/4/17.
 * Time : 下午4:41.
 * Description:
 *
 */

@Entity
@Table(name = "station")
data class Station(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long = 0L,
        @Column(length = 30)
        var name_zh: String = "",
        @Column(length = 50)
        var name_en: String = "",
        var district: Long = 0L
                  )