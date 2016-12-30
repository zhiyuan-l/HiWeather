package com.vito.work.weather.domain.util.http

/**
 * Created by lingzhiyuan.
 * Date : 16/4/14.
 * Time : 下午4:51.
 * Description:
 *
 */

open class BusinessException(val error: BusinessError) : RuntimeException() {
    private val serialVersionUID: Long = Long.MIN_VALUE
}