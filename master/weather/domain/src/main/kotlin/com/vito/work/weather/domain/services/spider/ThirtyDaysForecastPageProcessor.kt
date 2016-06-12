package com.vito.work.weather.domain.services.spider

import us.codecraft.webmagic.Page
import us.codecraft.webmagic.Site
import us.codecraft.webmagic.processor.PageProcessor

/**
 * Created by lingzhiyuan.
 * Date : 16/4/10.
 * Time : 下午9:25.
 * Description:
 *
 */
open class ThirtyDaysForecastPageProcessor : PageProcessor
{

    private var site: Site = Site.me()
            .setSleepTime(5)
            .setRetryTimes(5)
            .setCycleRetryTimes(5)

    override fun getSite(): Site?
    {
        return site
    }

    override fun process(page: Page?)
    {
        val path = "//div[@class='today_30t']"
        var html = page?.html !!

        page?.putField("date", html.xpath("$path/h4/span/text()").all())
        page?.putField("weather_day", html.xpath("$path/ul[1]/li[@class='t2']/span/text()").all())
        page?.putField("max", html.xpath("$path/ul[1]/li[@class='t3']/font/text()").all())
        page?.putField("wind_direction_day", html.xpath("$path/ul[1]/li[@class='t4']/text()").all())
        page?.putField("wind_force_day", html.xpath("$path/ul[1]/li[@class='t5']/text()").all())
        page?.putField("weather_night", html.xpath("$path/ul[2]/li[@class='t2']/span/text()").all())
        page?.putField("min", html.xpath("$path/ul[2]/li[@class='t3']/font/text()").all())
        page?.putField("wind_direction_night", html.xpath("$path/ul[2]/li[@class='t4']/text()").all())
        page?.putField("wind_force_night", html.xpath("$path/ul[2]/li[@class='t5']/text()").all())
    }
}