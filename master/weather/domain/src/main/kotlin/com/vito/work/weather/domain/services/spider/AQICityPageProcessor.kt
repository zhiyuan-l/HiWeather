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
 * Time : 下午11:30.
 * Description:
 *
 */
class AQICityPageProcessor: PageProcessor
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

        val path1= "//div[@class='meta']/ul/li/span[@class='td-2nd']/a"
        if(html == null || html.xpath(path1).all().size == 0)
        {
            throw BusinessException(BusinessError.ERROR_TARGET_PAGE_NOT_FOUND)
        }

        page?.putField("urls", html.xpath("$path1/@href").all())
        page?.putField("titles", html.xpath("$path1/text()").all())
    }

}