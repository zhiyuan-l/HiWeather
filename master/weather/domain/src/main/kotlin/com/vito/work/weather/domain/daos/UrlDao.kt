package com.vito.work.weather.domain.daos

import com.vito.work.weather.domain.config.Constant
import com.vito.work.weather.domain.entities.Url
import org.hibernate.Criteria
import org.hibernate.criterion.Restrictions
import org.springframework.stereotype.Repository

/**
 * Created by lingzhiyuan.
 * Date : 16/4/8.
 * Time : 上午10:30.
 * Description:
 *
 */

@Repository
open class UrlDao: BaseDao()
{

    open fun findByUrl(url: String): Url?
    {
        var criteria: Criteria = sessionFactory.currentSession.createCriteria(Url::class.java)
        criteria.add(Restrictions.eq("url", url))
        val list = criteria.list()
        if(list != null && list.size > 0)
        {
            return list[0] as Url
        }
        return null
    }

    open fun findByType(type: Int, status: Int = Constant.URL_STATUS_FINISHED): List<Url>?
    {
        var criteria: Criteria = sessionFactory.currentSession.createCriteria(Url::class.java)
        criteria.add(Restrictions.eq("type", type))
        criteria.add(Restrictions.eq("status", status))
        return criteria.list() as List<Url>?
    }

    open fun getCount(type: Int): Int?
    {
        var criteria: Criteria = sessionFactory.currentSession.createCriteria(Url::class.java)
        criteria.add(Restrictions.eq("type", type))
        return criteria.list()?.size
    }

    open fun findAll(status: Int): List<Url>?
    {
        var criteria: Criteria = sessionFactory.currentSession.createCriteria(Url::class.java)
        criteria.add(Restrictions.eq("status", status))
        return criteria.list() as List<Url>?
    }

    open fun findByUrls(urls: List<String>): List<Url>?
    {
        if(urls.size == 0)
        {
            return null
        }
        var criteria: Criteria = sessionFactory.currentSession.createCriteria(Url::class.java)
        criteria.add(Restrictions.`in`("url", urls))
        return criteria.list() as List<Url>?
    }

}