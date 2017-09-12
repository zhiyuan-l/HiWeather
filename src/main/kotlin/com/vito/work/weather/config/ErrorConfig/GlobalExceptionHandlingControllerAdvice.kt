package com.vito.work.weather.config.ErrorConfig

import com.vito.work.weather.util.http.BaseResponse
import com.vito.work.weather.util.http.BusinessException
import com.vito.work.weather.util.http.ErrorResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody

/**
 * Created by lingzhiyuan.
 * Date : 16/4/14.
 * Time : 下午4:06.
 * Description:
 *
 */


/**
 * 处理所有异常, 包括 GlobalErrorController抛出的异常
 * */
@ControllerAdvice
class GlobalExceptionHandlingControllerAdvice {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandlingControllerAdvice::class.java)
    }

    @ExceptionHandler(value = BusinessException::class)
    @ResponseBody
    fun exceptionHandler(exception: BusinessException): BaseResponse {
        //        val statusName = "errorStatus"
        //        val messageName = "errorMessage"

        val errorCode: Int
        val errorMessage: String

        exception.printStackTrace()

        errorCode = exception.error.code
        errorMessage = exception.error.message

        //        model.addAttribute(statusName, errorCode)
        //        model.addAttribute(messageName, errorMessage)

        logger.error("错误代码 : $errorCode")
        logger.error("错误描述 : $errorMessage")

        val response = ErrorResponse(errorCode, errorMessage)

        return response
    }

}
