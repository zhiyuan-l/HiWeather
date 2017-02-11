package com.vito.work.weather.util.http

import org.springframework.http.HttpStatus

/**
 * Created by lingzhiyuan.
 * Date : 16/4/14.
 * Time : 下午4:35.
 * Description:
 *
 */

enum class BusinessError(val code: Int, val message: String) {


    ERROR_DEFAULT_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误"),
    ERROR_URL_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "URL不存在"),
    ERROR_CITY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "城市 ID 不存在"),
    ERROR_DISTRICT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "城市 ID 不存在"),
    ERROR_PROVINCE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "城市 ID 不存在"),
    ERROR_RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "资源未找到"),
    ERROR_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "未找到可用的模板"),
    ERROR_TARGET_PAGE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "目标页面未找到"),

    ERROR_HISTORY_WEATHER_UPDATING(HttpStatus.CONFLICT.value(), "历史天气正在更新, 请稍候再试"),
    ERROR_FORECAST_WEATHER_UPDATING(HttpStatus.CONFLICT.value(), "未来天气正在更新, 请稍候再试"),
    ERROR_LOCATION_IS_UPDATING(HttpStatus.CONFLICT.value(), "区域信息正在更新, 请稍候再试"),
    ERROR_TODAY_WEATHER_UPDATING(HttpStatus.CONFLICT.value(), "今日天气正在更新, 请稍候再试"),
    ERROR_INSTANT_WEATHER_IS_UPDATING(HttpStatus.CONFLICT.value(), "即时天气正在更新, 请稍候再试"),

    ERROR_TYPE_NOT_SUPPORTED(1001, "不支持的类型"),

    ERROR_LOCATION_UPDATE_FAILED(1201, "区域更新失败"),

    ERROR_UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "未授权"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "用户未找到")

}