package com.vito.work.weather.web.controllers.ErrorConfig

import com.vito.work.weather.domain.util.http.BusinessError
import com.vito.work.weather.domain.util.http.BusinessException
import com.vito.work.weather.domain.util.http.ErrorResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.thymeleaf.exceptions.TemplateInputException

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
open class GlobalExceptionHandlingControllerAdvice
{

    companion object{
        val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandlingControllerAdvice::class.java)
    }

    @ExceptionHandler(value = Exception::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    open fun exceptionHandler(exception: Exception, model: Model): Any
    {
        val statusName = "errorStatus"
        val messageName = "errorMessage"

        var errorCode = 0
        var errorMessage = ""

        exception.printStackTrace()

        when (exception)
        {
            is BusinessException ->
            {
                errorCode = exception.error.code
                errorMessage = exception.error.message
            }
            is TemplateInputException ->
            {
                errorCode = HttpStatus.NOT_FOUND.value()
                errorMessage = BusinessError.ERROR_TEMPLATE_NOT_FOUND.message
            }
            else                 ->
            {
                errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
                errorMessage = "服务器内部错误"
            }
        }

        model.addAttribute(statusName, errorCode)
        model.addAttribute(messageName, errorMessage)

        logger.error("错误代码 : $errorCode")
        logger.error("错误描述 : $errorMessage")

        if (AnnotationUtils.findAnnotation(exception.javaClass, ResponseStatus::class.java) != null)
        {
            logger.info("RETURN ERROR RESPONSE")
            return ErrorResponse(errorCode, errorMessage)
        }

        logger.info("TO ERROR PAGE")

        return "error"
    }

}
