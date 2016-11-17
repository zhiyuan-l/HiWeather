package com.vito.work.weather.domain.daos

import org.hibernate.SessionFactory
import org.springframework.orm.hibernate5.HibernateTemplate
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/1.
 * Time : 下午4:27.
 * Description:
 *
 */

open class BaseDao
{
    @Resource
    lateinit var hibernateTemplate: HibernateTemplate
    @Resource
    lateinit var sessionFactory: SessionFactory

    open fun <T> findAll(clazz: Class<T>): List<T?>?
    {
        val session = sessionFactory.currentSession
        val criteria = session.createCriteria(clazz)
        return criteria.list() as List<T?>
    }

    open fun <T> findById(clazz: Class<T>, id: Long): Any?
    {
        return hibernateTemplate.get(clazz, id)
    }

    open fun <T> getById(clazz: Class<T>, id: Long): T
    {
        val `object` = hibernateTemplate.get(clazz, id)
        return `object`

    }

    open fun saveOrUpdate(`object`: Any)
    {
        hibernateTemplate.saveOrUpdate(`object`)
    }

    open fun update(`object`: Any)
    {
        hibernateTemplate.update(`object`)
    }

    open fun batchDelete(list: List<Any>)
    {
        hibernateTemplate.deleteAll(list)
    }

    open fun delete(`object`: Any)
    {
        hibernateTemplate.delete(`object`)
    }

}