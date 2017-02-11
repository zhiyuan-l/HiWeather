package com.vito.work.weather.service.spider

import org.slf4j.LoggerFactory
import us.codecraft.webmagic.Page
import us.codecraft.webmagic.Site
import us.codecraft.webmagic.processor.PageProcessor

/**
 * Created by lingzhiyuan.
 * Date : 16/4/16.
 * Time : 下午12:54.
 * Description:
 *
 */

open class TodayViewPageProcessor : PageProcessor {
    companion object {
        val logger = LoggerFactory.getLogger(TodayViewPageProcessor::class.java)
    }

    private var site: Site = Site.me().setSleepTime(5).setRetryTimes(5).setCycleRetryTimes(3)

    override fun getSite(): Site? {
        return site
    }

    override fun process(page: Page?) {
        val path = "//script"
        val script = page?.html?.xpath(path)?.all() !![17]

        if (script != null) {
            var resultStr = script.toString()
            resultStr = resultStr.substringAfter("hours_tdate").substringBefore("hours_line").replace("var ", "").replace("=", "").replace(";", "")

            val tdate: String = resultStr.substringAfter("hours_tdate").substringBefore("hours_tvalue").replace("\"", "").replace("'", "\"").trim()
            val tvalue: String = resultStr.substringAfter("hours_tvalue").substringBefore("hours_pvalue").trim()
            val pvalue: String = resultStr.substringAfter("hours_pvalue").substringBefore("hours_hvalue").trim()
            val hvalue: String = resultStr.substringAfter("hours_hvalue").substringBefore("hours_airpressure").trim()
            val aqi: String = resultStr.substringAfter("hours_airpressure").trim()
            page?.putField("tdate", tdate)
            page?.putField("tvalue", tvalue)
            page?.putField("pvalue", pvalue)
            page?.putField("hvalue", hvalue)
            page?.putField("aqi", aqi)
        }

    }
}