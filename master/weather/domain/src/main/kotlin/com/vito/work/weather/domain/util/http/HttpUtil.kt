package com.vito.work.weather.domain.util.http

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import kotlin.jvm.internal.iterator

/**
 * Created by lingzhiyuan.
 * Date : 16/4/1.
 * Time : 下午4:39.
 * Description:
 *
 */

class HttpUtil()
{
    companion object{
        val logger = LoggerFactory.getLogger(HttpUtil::class.java)
    }

}

fun sendGetRequestViaHttpClient(baseUrl: String, params: HashMap<String, Any>, headers: HashMap<String, String>, charset: Charset): String?
{

    var url = if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) baseUrl
    else
        "http://$baseUrl"
    var it = iterator(params.keys.toTypedArray())
    while (it.hasNext())
    {
        var key = it.next()
        var value = params.get(key)
        url = "$url&$key=$value"
    }
    val get = HttpGet(url);

    headers.forEach { k, v -> get.setHeader(k, v) }

    val http = HttpClients.createDefault()
    val response = http.execute(get)
    val entity = response.getEntity();

    HttpUtil.logger.info("Send Get : $url")
    HttpUtil.logger.info("Response Status : ${response.statusLine}")

    if (entity != null) {
        return EntityUtils.toString(entity, charset)
    }

    return null
}


fun sendGetRequest(url: String, params: HashMap<String, Any> = HashMap(), charset: Charset = Charset.forName("utf-8"), headers: HashMap<String, String> = HashMap()): String
{

    var url = if (url.startsWith("http://") || url.startsWith("https://")) url
    else
        "http://$url"
    var it = iterator(params.keys.toTypedArray())
    while (it.hasNext())
    {
        var key = it.next()
        var value = params.get(key)
        url = "$url&$key=$value"
    }
    var urlObj = URL(url)
    var connection = urlObj.openConnection()
    for((k, v) in headers)
    {
        connection.addRequestProperty(k, v)
    }
    connection.doOutput = true

    var answer = StringBuffer()
    var reader: BufferedReader = BufferedReader(InputStreamReader(connection.inputStream, charset))
    reader.forEachLine { answer.append(it) }

    reader.close()
    return answer.toString()
}

fun sendPostRequest(url: String, params: HashMap<String, Any>, charset: Charset = Charset.forName("UTF-8")): String
{

//    val log = LoggerFactory.getLogger(AppStarter::class.java)
    var data = ""
    var url = if (url.startsWith("http://") || url.startsWith("https://")) url
    else
        "http://$url"
    var it = iterator(params.keys.toTypedArray())
    while (it.hasNext())
    {
        var key = it.next()
        var value = params.get(key)
        data = "$data&$key=$value"
    }
    data.removePrefix("&")
    var urlObj = URL(url)
    var connection = urlObj.openConnection()
    connection.doOutput = true

    var writer = OutputStreamWriter(connection.outputStream)
    writer.write(data)
    writer.flush()

//    log.info("Send POST Request : ${url.toString()}")

    var answer = StringBuffer()
    var reader: BufferedReader = BufferedReader(InputStreamReader(connection.inputStream, charset))
    reader.forEachLine { answer.append(it) }

    writer.close()
    reader.close()
    return answer.toString()
}