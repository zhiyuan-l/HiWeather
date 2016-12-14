package com.vito.work.weather.domain.util.cnweather

import com.fasterxml.jackson.databind.ObjectMapper
import com.vito.work.weather.domain.beans.api.CnWeatherModel
import com.vito.work.weather.domain.config.Constant
import com.vito.work.weather.domain.entities.District
import com.vito.work.weather.domain.util.cnweather.APIType.*
import com.vito.work.weather.domain.util.http.HttpUtil
import com.vito.work.weather.domain.util.http.sendGetRequestViaHttpClient
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Created by lingzhiyuan.
 * Date : 16/4/17.
 * Time : 上午11:49.
 * Description:
 *
 */

class API {
    companion object {
        val logger = LoggerFactory.getLogger(API::class.java)
    }
}

/**
 * @property    INDEX_F     基础指数接口, 只包含城市
 * @property    INDEX_V     常规指数接口, 包含区县及城市
 * @property    FORECAST_F  基础天气预报接口, 只包含城市
 * @property    FORECAST_V  常规天气预报接口, 包含区县及城市
 * */
enum class APIType(val type: Int, val title: String) {
    INDEX_F(0, "index_f"),
    INDEX_V(1, "index_v"),
    FORECAST_F(3, "forecast_f"),
    FORECAST_V(4, "forecast_v")
}

data class ReqeustBean(val areaids: List<Long>, val type: APIType, val datetime: LocalDateTime, val appid: String, val private_key: String)

fun getResultBean(district: District): CnWeatherModel? {
    val districtIds = mutableListOf(district.id)

    val requestBean = ReqeustBean(districtIds, APIType.FORECAST_V, LocalDateTime.now(), Constant.CNWEATHER_APPID, Constant.CNWEATHER_PRIVATE_KEY)

    try {
        var result = invokeAPI(Constant.CNWEATHER_BASE_URL, requestBean)
        var mapper = ObjectMapper()
        var resultBean = mapper.readValue(result, CnWeatherModel::class.java)
        return resultBean
    } catch(ex: Exception) {
        ex.printStackTrace()
        throw ex
    }
}

private fun invokeAPI(baseUrl: String, requestBean: ReqeustBean): String? {
    val url = urlBuilder(baseUrl, requestBean)
    val result: String? = HttpUtil.sendGetRequestViaHttpClient(url, hashMapOf(), hashMapOf(), Charset.forName("utf-8"))
    if (result != null) {
        API.logger.info(result)
    }
    return result
}

private fun urlBuilder(baseUrl: String, requestBean: ReqeustBean): String {

    val url = StringBuffer()

    val paramSeperator = "&"

    val date: String
    var areaid: String = ""
    val type: String
    val appid: String
    val key: String

    // Parse Areaid
    val areaidSeperator = "|"
    requestBean.areaids.forEach { areaid = ("$areaid$it$areaidSeperator") }
    areaid = areaid.removeSuffix(areaidSeperator)

    // Parse Date
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    date = formatter.format(requestBean.datetime)

    // Parse Type
    type = requestBean.type.title

    // Parse Appid
    appid = requestBean.appid

    url.append(baseUrl)
    url.append("?")
    url.append("areaid=")
    url.append(areaid)
    url.append(paramSeperator)
    url.append("type=")
    url.append(type)
    url.append(paramSeperator)
    url.append("date=")
    url.append(date)
    url.append(paramSeperator)
    url.append("appid=")

    var publicKey = url.toString().plus(appid)

    url.append(appid.substring(0, 6))

    key = APIURLEncoder.standardURLEncoder(publicKey, requestBean.private_key)

    url.append(paramSeperator)
    url.append("key=")
    url.append(key)

    return url.toString()
}

