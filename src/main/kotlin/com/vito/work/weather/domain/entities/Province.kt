package com.vito.work.weather.domain.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * Created by lingzhiyuan.
 * Date : 16/4/1.
 * Time : 下午4:23.
 * Description:
 *
 */

@Entity
@Table(name = "province")
data class Province(

        @Id
        var id: Long = 0L,
        @Column(length = 30)
        var title: String = "",
        @Column(length = 30)
        var pinyin: String = "",
        var ishot: Int = 0

                   )