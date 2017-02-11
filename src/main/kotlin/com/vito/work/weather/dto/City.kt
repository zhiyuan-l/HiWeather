package com.vito.work.weather.dto

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * Created by lingzhiyuan.
 * Date : 16/4/1.
 * Time : 下午4:25.
 * Description:
 *
 */

@Entity
@Table(name = "city")
data class City(

        @Id
        var id: Long = 0L,
        @Column(length = 30)
        var title: String = "",
        @Column(length = 30)
        var pinyin: String = "",
        var province: Long = 0L,
        var ishot: Int = 0
               )