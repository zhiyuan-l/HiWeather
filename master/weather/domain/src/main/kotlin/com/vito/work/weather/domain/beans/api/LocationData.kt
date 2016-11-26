package com.vito.work.weather.domain.beans.api

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.StringWriter
import java.util.*

/**
 * Created by lingzhiyuan.
 * Date : 16/4/1.
 * Time : 下午6:39.
 * Description:
 *
 */

class LocationData {

    companion object {
        val LOCATION_INFO_TYPE_ZERO: Int = 0
        val LOCATION_INFO_TYPE_ONE: Int = 1
        val LOCATION_INFO_TYPE_TWO: Int = 2
    }

}

data class LocationInfo(
        var id: String = "",
        var value: HashMap<Long, String> = HashMap(),
        var py: HashMap<Long, String> = HashMap(),
        var defval: Long = 0L,
        var ishot: HashMap<Long, Int> = HashMap()
                       )

/**
 * 对于 LOCATION_INFO 的区域信息转换器
 *  将获取到的区域信息 JSON 数据通过 OBJECT MAPPER 转换成对应的对象
 *  由于 data 的结构为 List , 且获取的 JSON 信息中只有一个对象有效, 因此只对其中一个元素进行转换
 *  */
fun locationInfoParser(type: Int, data: String): LocationInfo? {

    // 有效信息的位置
    var infoIndex: Int = 1

    // 根据数据接口不同判断有效信息所在的元素
    when (type) {
        LocationData.LOCATION_INFO_TYPE_ZERO                                     -> infoIndex = 1
        LocationData.LOCATION_INFO_TYPE_ONE, LocationData.LOCATION_INFO_TYPE_TWO -> infoIndex = 0
    }

    var mapper = ObjectMapper()
    var list = mapper.readValue(data, Array<Any>::class.java)
    var locationInfo: LocationInfo?

    var writer = StringWriter()
    mapper.writeValue(writer, list[infoIndex])
    var tempData: String = writer.toString()
    try {
        locationInfo = mapper.readValue(tempData,
                                        LocationInfo::class.java)
    } catch (ex: Exception) {
        // 转换失败则跳过
        locationInfo = null
    }

    return locationInfo
}
