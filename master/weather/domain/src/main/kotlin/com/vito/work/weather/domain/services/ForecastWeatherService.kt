package com.vito.work.weather.domain.services

import com.vito.work.weather.domain.config.Constant
import com.vito.work.weather.domain.daos.ForcastWeatherDao
import com.vito.work.weather.domain.entities.City
import com.vito.work.weather.domain.entities.District
import com.vito.work.weather.domain.entities.ForecastWeather
import com.vito.work.weather.domain.services.spider.ThirtyDaysForecastPageProcessor
import com.vito.work.weather.domain.util.cnweather.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import us.codecraft.webmagic.ResultItems
import us.codecraft.webmagic.Spider
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.annotation.PreDestroy
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/10.
 * Time : 下午9:41.
 * Description:
 *
 */


@Service("forecastWeatherService")
@Transactional
open class ForecastWeatherService: AbstractSpiderTask()
{
    @Resource
    lateinit var forcastWeatherDao: ForcastWeatherDao

    @PreDestroy
    open fun destroy()
    {
        spider.close()
    }

    companion object
    {
        var spider: Spider = Spider.create(ThirtyDaysForecastPageProcessor())
                .thread(Constant.SPIDER_THREAD_COUNT)

        val logger = LoggerFactory.getLogger(ForecastWeatherService::class.java) !!
    }

    /**
     * 查询未来三天的天气预报（不包括当天）
     *
     * @param   districtId  需要查询的区县 id
     *
     * @return  返回获取到的天气
     * */
    open fun findThreeDaysWeather(districtId: Long): List<ForecastWeather>?
    {
        val MAX_RESULT = 3
        val list = forcastWeatherDao.findWeathersAfter(districtId,Date.valueOf(LocalDate.now().plusDays(1)), MAX_RESULT)
        return list
    }

    /**
     * 从网上更新未来天气
     *
     * @param   city        待更新的区县所属的城市
     * @param   district    待更新的区县
     *
     * 1. 创建爬虫更新的 url
     * 2. 执行爬虫更新三十天预报
     * 3. 执行保存操作
     * 4. 执行 api 更新三天预报
     * 5. 执行保存操作
     *
     * @return  fws     获取到的天气
     *
     * */
    open fun execute(city: City, district: District){
        task(){
            val targetUrl = forecastViewUrlBuilder(Constant.FORCAST_WEATHER_BASE_URL, city.pinyin, district.pinyin)
            val fws = fetchDataViaSpider(targetUrl, district)
            saveWeathers(fws, district)
            val fws2 = fetchDataViaAPI(district)
            saveWeathers(fws2,district)
        }
    }

    /**
     * 保存天气预报的 list
     * @param weathers      待保存的所有天气
     * @param district      待保存的天气所属的区县
     *
     * 保存前需要先查出数据库中是否有对应的天气, 根据区县和日期判断是否存在旧项
     *  若有, 则更新旧项
     *  没有, 则保存为新项
     * */
    open fun saveWeathers(weathers: List<ForecastWeather>, district: District)
    {
        val dates = mutableListOf<Date>()
        val savedWeathers: MutableList<ForecastWeather> = mutableListOf()
        weathers.forEach { dates.add(it.date) }
        val temp = forcastWeatherDao.findByDistrictDates(district.id, dates)
        if (temp != null)
        {
            savedWeathers.addAll(temp)
        }
        weathers.forEach { new ->
            val ori = savedWeathers.firstOrNull() { it -> it.district == new.district && it.date == new.date }
            if (ori == null )
            {
                savedWeathers.add(new)
            }
            else
            {
                with(ori){
                    max = if(new.max == -273) max else new.max
                    min = if(new.min == -273) min else new.min
                    weather_day = if(new.weather_day == Weather.UNKNOWN.code.toString()) weather_day else new.weather_day
                    weather_night = if(new.weather_night == Weather.UNKNOWN.code.toString()) weather_night else new.weather_night
                    wind_direction_day = if(new.wind_direction_day == WindDirection.NOWIND.code) wind_direction_day else new.wind_direction_day
                    wind_direction_night = if(new.wind_direction_night == WindDirection.NOWIND.code) wind_direction_night else new.wind_direction_night
                    wind_force_day = if(new.wind_force_day == WindForce.LEVEL_ZERO.code.toString()) wind_force_day else new.wind_force_day
                    wind_force_night = if(new.wind_force_night == WindForce.LEVEL_ZERO.code.toString()) wind_force_night else new.wind_force_night
                    update_time = new.update_time
                    sunrise = if(new.sunrise == "") sunrise else new.sunrise
                    sunset = if(new.sunset == "") sunset else new.sunset
                }
            }
        }

        savedWeathers.forEach { forcastWeatherDao save it }
    }

    /**
     * 获取特定日期的天气预报
     * */
    open fun findByDate(districtId: Long, date: LocalDate): ForecastWeather?
    {
        val result = forcastWeatherDao.findByDistrictDate(districtId, Date.valueOf(date))
        return result
    }

    /**
     * 获取今天之后的30天天气
     *
     * */
    open fun findThirtyDaysWeather(districtId: Long): List<ForecastWeather>?
    {
        val MAX_RESULT = 30
        val list = forcastWeatherDao.findWeathersAfter(districtId, Date.valueOf(LocalDate.now()), MAX_RESULT)
        return list
    }

}

/**
 * URL Builder
 * */
private fun forecastViewUrlBuilder(baseUrl: String, cityPinyin: String, districtPinyin: String): String
{

    val urlSeparator = "/"
    val urlSuffix = "/30/"

    val urlBuffer: StringBuffer = StringBuffer()
    with(urlBuffer){
        if (! baseUrl.startsWith("http://") && ! baseUrl.startsWith("https://"))
        {
            append("http://")
        }

        append(baseUrl.replace("www", cityPinyin))
        append(urlSeparator)
        append(districtPinyin)
        append(urlSuffix)
    }


    return urlBuffer.toString()
}

/**
 * 通过爬虫获取一个月的天气预报数据
 *
 * @param   targetUrl   目标 url
 * @param   district    待更新天气的区县
 *
 * 爬虫获取到的 ResultItems的 request 可能为空, 需要先做判断
 *
 * 1. 首先将 url 加入爬虫的爬去队列
 * 2. 从 ResultItems 中获取信息列表, 其中每一项都是一个列表, 列表中的信息一一对应
 * 3. 转换所有日期
 * 4. 组装成天气对象, 返回所有结果
 *
 * 其中 weather_day, weather_night, wind_force_night, wind_force_day可能包含『转』, 这说明有多种天气, 天气代码之间用 「,」隔开
 * wind_direction_day, wind_direction_night 可能包含 wind_force_day 和 wind_force_night 的值, 需要作判断
 *
 * */
private fun fetchDataViaSpider(targetUrl: String, district: District): List<ForecastWeather>
{

    var resultItems: ResultItems? = null
    val fws = mutableListOf<ForecastWeather>()

    try
    {
        resultItems = ForecastWeatherService.spider.get(targetUrl)
    }
    catch(ex: Exception)
    {
        ex.printStackTrace()
    }

    if (resultItems?.request == null)
    {
        throw Exception("Request Can't Be Null")
    }

    with(resultItems!!){
        val dateStrs: List<String> = get("date")
        val maxes: List<String> = get("max")
        val mines: List<String> = get("min")
        val dayWeathers: List<String> = get("weather_day")
        val nightWeathers: List<String> = get("weather_night")
        val dayWindDirections: List<String> = get("wind_direction_day")
        val nightWindDirections: List<String> = get("wind_direction_night")
        val dayWindForces: List<String> = get("wind_force_day")
        val nightWindForces: List<String> = get("wind_force_night")

        // Get Date Strings
        val dates = mutableListOf<Date>()
        // 将所有的 string 转换成日期存储到数组中
        dateStrs.forEach { dates.add(Date.valueOf(LocalDate.parse(it.toString().split(" ")[0].replace("月", "").replace("日", "").trim().plus(LocalDate.now().year), DateTimeFormatter.ofPattern("MMddyyyy")))) }

        for ((index, date) in dates.withIndex())
        {
            val fw: ForecastWeather = ForecastWeather()
            with(fw){
                this.district = district.id
                this.date = dates[index]

                // 获取白天的天气
                max = try
                {
                    Integer.parseInt(maxes[index].replace("℃", "").trim())
                }
                catch (ex: Exception)
                {
                    - 273
                }
                weather_day = parseWeather(dayWeathers[index])
                // 风向有三种情况: 1.只包含风向的名称 2.另外包含了风力并用空格隔开 3. 只包含了风力的信息（带有级或微风）
                val parsedWDD = dayWindDirections[index].trim().split(" ")
                // 如果
                if(!parsedWDD[0].contains("级") && !parsedWDD[0].contains("微风"))
                {
                    wind_direction_day = findByWindDirectionName(parsedWDD[0]).code
                    if(dayWindForces[index].trim() == "")
                        wind_force_day = parsedWDD[1]
                    else
                        wind_force_day = parseWindForce(dayWindForces[index])
                }
                else
                {
                    wind_direction_day = WindDirection.NOWIND.code
                    wind_force_day = parseWindForce(parsedWDD[0])
                }

                // 获取晚上的天气
                min = try
                {
                    Integer.parseInt(mines[index].toString().replace("℃", "").trim())
                }
                catch(ex: Exception)
                {
                    - 273
                }
                val parsedWDN = nightWindDirections[index].trim().split(" ")
                weather_night = parseWeather(nightWeathers[index])
                if(parsedWDN[0].contains("级") || parsedWDN[0].contains("微风"))
                {
                    wind_direction_night = WindDirection.NOWIND.code
                    wind_force_night = parseWindForce(parsedWDN[0])
                }
                else
                {
                    wind_direction_night = findByWindDirectionName(parsedWDN.first()).code
                    if(nightWindForces[index].trim() == "")
                        wind_force_night = parsedWDN[1]
                    else
                        wind_force_night = parseWindForce(nightWindForces[index])
                }

            }

            fws.add(fw)
        }
    }

    return fws
}

/**
 * 将天气名称的字符串转换成天气代码的字符串
 * 天气代码之间用逗号隔开,天气名称之间用「转」字连接
 * 例: 晴转多云 -> 0,1
 *
 * 若只有一种天气, 则直接返回对应的天气代码
 *
 * */
private fun parseWeather(weatherStr: String): String
{
    var result = ""
    val list = weatherStr.trim().split("转")
    when(list.size)
    {
        1 ->
        {
            val codeone = findByWeatherName(list[0]).code.toString()
            result = codeone
        }
        2 ->
        {
            val codeone = findByWeatherName(list[0]).code.toString()
            val codetwo = findByWeatherName(list[1]).code.toString()
            result = "$codeone,$codetwo"
        }
    }

    return result
}

/**
 * 将风力名称字符串转换成风力代码字符串
 * 不同风力代码之间用逗号隔开, 风力名称之间用「转」字连接
 *
 * 例: 3-4级转4-5级 -> 2,3
 *
 * 若只有一种风力, 则直接返回对应的风力代码
 * */
private fun parseWindForce(windForceStr: String): String
{
    var result = ""
    val list = windForceStr.trim().split("转")
    when(list.size)
    {
        1 ->
        {
            val codeone = findByWindForceName(list[0]).code.toString()
            result = codeone
        }
        2 ->
        {
            val codeone = findByWindForceName(list[0]).code.toString()
            val codetwo = findByWindForceName(list[1]).code.toString()
            result = "$codeone,$codetwo"
        }
    }

    return result
}

/**
 * 通过调用中国天气网的 官方api 获取三天预报的数据
 * */
fun fetchDataViaAPI(district: District): List<ForecastWeather>
{
    val fws = mutableListOf<ForecastWeather>()
    try
    {
        val resultBean = getResultBean(district)
        val f = resultBean?.f
        if(f != null)
        {
            val f0 = f.f0
            val f1 = f.f1
            val datetime = LocalDateTime.parse(f0, DateTimeFormatter.ofPattern("yyyyMMddHHmm"))

            for((index, forecast) in f1.withIndex())
            {
                val fw = ForecastWeather()
                with(fw){
                    date = Date.valueOf(datetime.toLocalDate().plusDays(index.toLong()))
                    this.district = district.id
                    weather_day = findByWeatherCode(forecast.fa).code.toString()
                    weather_night = findByWeatherCode(forecast.fb).code.toString()
                    // 若无温度数据, 则设置温度为-273
                    max = (if(forecast.fc.trim() == "") "-273" else forecast.fc.trim()).toInt()
                    min = (if(forecast.fd.trim() == "") "-273" else forecast.fd.trim()).toInt()
                    wind_direction_day = findByWindDirectionCode(forecast.fe).code
                    wind_direction_night = findByWindDirectionCode(forecast.ff).code
                    wind_force_day = findByWindForceCode(forecast.fg).code.toString()
                    wind_force_night = findByWindForceCode(forecast.fh).code.toString()
                    sunrise = forecast.fi.split("|").first()
                    sunset = forecast.fi.split("|").last()
                    update_time = Timestamp.valueOf(datetime)
                }

                fws.add(fw)
            }
        }

    }
    catch(ex: Exception)
    {
        ex.printStackTrace()
    }

    return fws
}
