package com.vito.work.weather.dto

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * Created by lingzhiyuan.
 * Date : 16/4/1.
 * Time : 下午6:34.
 * Description:
 *
 */

@Entity
@Table(name = "district")
data class District(
        @Id
        var id: Long = 0L,
        @Column(length = 30)
        var title: String = "",
        @Column(length = 30)
        var pinyin: String = "",
        @Column(length = 30)
        var pinyin_aqi: String = "",
        var city: Long = 0L,
        var longitude: Double = .0,
        var latitude: Double = .0,
        var altitude: Double = .0,
        @Column(length = 10)
        var zipcode: String = "",
        @Column(length = 10)
        var areaCode: String = "",
        var ishot: Int = 0
                   )