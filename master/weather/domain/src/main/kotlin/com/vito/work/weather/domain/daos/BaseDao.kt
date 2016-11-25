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

    inline fun <reified T> findAll(): List<T>? {
        val session = sessionFactory.currentSession
        val criteria = session.createCriteria(T::class.javaClass)
        return criteria.list()?.filterIsInstance<T>()
    }

    inline fun <reified T> findById(id: Long)
            =hibernateTemplate.get(T::class.javaClass, id) as T?

    inline fun <reified T> getById(id: Long)
            = hibernateTemplate.get(T::class.javaClass, id) as T?

    open infix fun saveOrUpdate(`object`: Any)
            = hibernateTemplate.saveOrUpdate(`object`)

    open infix fun update(`object`: Any)
            = hibernateTemplate.update(`object`)

    open infix fun batchDelete(list: List<Any>)
            = hibernateTemplate.deleteAll(list)

    open infix fun delete(`object`: Any)
            = hibernateTemplate.delete(`object`)
}