package com.vito.work.weather.web.controllers.ErrorConfig

import com.vito.work.weather.domain.util.http.BusinessError
import com.vito.work.weather.domain.util.http.BusinessException
import org.apache.http.HttpStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.AbstractErrorController
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by lingzhiyuan.
 * Date : 16/4/14.
 * Time : 下午11:02.
 * Description:
 *
 */

@Controller
open class GlobalErrorController @Autowired constructor(var errorAttributes: DefaultErrorAttributes) : AbstractErrorController(errorAttributes) {

    companion object {
        const val DEFAULT_ERROR_PATH = "/error"

        val logger = LoggerFactory.getLogger(GlobalErrorController::class.java)
    }

    override fun getErrorPath(): String? {
        return DEFAULT_ERROR_PATH
    }

    // 捕获异常并抛出
    @RequestMapping(DEFAULT_ERROR_PATH)
    open fun toErrorPage(request: HttpServletRequest, response: HttpServletResponse) {
        val status = response.status
        when (status) {
            HttpStatus.SC_NOT_FOUND -> {
                throw BusinessException(BusinessError.ERROR_URL_NOT_FOUND)
            }
        }
        val error: Throwable = errorAttributes.getError(ServletRequestAttributes(request)) ?: Exception()
        throw error
    }

}