package com.vito.work.weather.admin.controllers.ErrorConfig

/**
 * Created by lingzhiyuan.
 * Date : 16/4/14.
 * Time : 下午11:02.
 * Description:
 *
 */

//@Controller
//open class GlobalErrorController @Autowired constructor(var errorAttributes: DefaultErrorAttributes) : AbstractErrorController(errorAttributes)
//{
//
//    companion object{
//        const val DEFAULT_ERROR_PATH = "/error"
//
//        val logger = LoggerFactory.getLogger(GlobalErrorController::class.java)
//    }
//
//    override fun getErrorPath(): String?
//    {
//        return DEFAULT_ERROR_PATH
//    }
//
//    @RequestMapping(DEFAULT_ERROR_PATH)
//    open fun toErrorPage(exception: Exception,request: HttpServletRequest, response: HttpServletResponse)
//    {
//        val status = response.status
//        when(status)
//        {
//            HttpStatus.SC_NOT_FOUND ->
//            {
//                throw BusinessException(BusinessError.ERROR_URL_NOT_FOUND)
//            }
//            HttpStatus.SC_UNAUTHORIZED ->
//            {
//                return
//            }
//        }
//        val error: Throwable = errorAttributes.getError(ServletRequestAttributes(request)) ?: Exception()
//        throw error
//    }
//
//}