package com.vito.work.weather.domain.services.spider

import us.codecraft.webmagic.Page
import us.codecraft.webmagic.Site
import us.codecraft.webmagic.processor.PageProcessor

/**
 * Created by lingzhiyuan.
 * Date : 16/4/6.
 * Time : 下午3:07.
 * Description:
 *
 */
open class MonthViewPageProcessor : PageProcessor {

    private var site: Site = Site.me().setSleepTime(5).setRetryTimes(5).setCycleRetryTimes(3)

    override fun getSite(): Site? {
        return site
    }

    override fun process(page: Page?) {
        val path = "//div[@class='tqtongji2']/ul[@class!='t1']/li"
        val html = page?.html !!
        val checkDatePath = html.xpath("$path[1]/a").match()
        page?.putField("date", html.xpath(if (checkDatePath) "$path[1]/a/text()" else "$path[1]/text()").all())
        page?.putField("max", html.xpath("$path[2]/text()").all())
        page?.putField("min", html.xpath("$path[3]/text()").all())
        page?.putField("weather", html.xpath("$path[4]/text()").all())
        page?.putField("wind_direction", html.xpath("$path[5]/text()").all())
        page?.putField("wind_force", html.xpath("$path[6]/text()").all())
    }
}