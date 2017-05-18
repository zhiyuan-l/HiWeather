package com.vito.work.weather.util.http

/**
 * Created by lingzhiyuan.
 * Date : 16/4/16.
 * Time : 下午11:19.
 * Description:
 *
 */

open class BaseResponse(var status: BaseResponseStatus)

open class ListResponse(var data: List<Any>) : BaseResponse(BaseResponseStatus.SUCCESS)

open class ObjectResponse(var data: Any?) : BaseResponse(BaseResponseStatus.SUCCESS)

open class ErrorResponse(var errorCode: Int, var errorMessage: String) : BaseResponse(BaseResponseStatus.FAIL)

enum class BaseResponseStatus(val status: Int) {
    SUCCESS(200),
    FAIL(400)
}