package com.vito.work.weather.domain.services.spider

import com.vito.work.weather.domain.util.http.BusinessError
import com.vito.work.weather.domain.util.http.BusinessException
import org.slf4j.LoggerFactory
import us.codecraft.webmagic.Page
import us.codecraft.webmagic.Site
import us.codecraft.webmagic.processor.PageProcessor

/**
 * Created by lingzhiyuan.
 * Date : 16/4/17.
 * Time : 下午4:21.
 * Description:
 *
 */
class AQIViewPageProcessor: PageProcessor
{

    companion object{
        val logger = LoggerFactory.getLogger(AQIViewPageProcessor::class.java)
    }

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
        var html = page?.html

        val path1= "//div[@class='num']/span/text()"
        if(html == null || html.xpath(path1).all().size == 0)
        {
            throw BusinessException(BusinessError.ERROR_TARGET_PAGE_NOT_FOUND)
        }

        page?.putField("aqi_value", html.xpath(path1))

        val path3 = "//table[@class='air_tab01']/tbody/tr"
        var trs = html.xpath("$path3")

        page?.putField("stations", trs.xpath("//td[1]/a/text()").all())
        page?.putField("station_urls", trs.xpath("//td[1]/a/@href").all())
        val valList = trs.xpath("//td[2]/text()").all()
        page?.putField("station_values", valList.subList(1, valList.size))
        val pm25List = trs.xpath("//td[4]/text()").all()
        page?.putField("station_pm25", pm25List.subList(1, pm25List.size))
        val o3List = trs.xpath("//td[5]/text()").all()
        page?.putField("station_o3", o3List.subList(1, o3List.size))
        val priList = trs.xpath("//td[6]/text()").all()
        page?.putField("station_primary", priList.subList(1, priList.size))

    }

}