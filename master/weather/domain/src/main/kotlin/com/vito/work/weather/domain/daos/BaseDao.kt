package com.vito.work.weather.domain.daos

import org.hibernate.SessionFactory
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.orm.hibernate5.HibernateTemplate
import javax.annotation.Resource

/**
 * Created by lingzhiyuan.
 * Date : 16/4/1.
 * Time : 下午4:27.
 * Description:
 *
 */

@NoRepositoryBean
abstract class BaseDao {
    @Resource
    lateinit var hibernateTemplate: HibernateTemplate
    @Resource
    lateinit var sessionFactory: SessionFactory

    inline fun <reified T> findAll(): List<T> {
        val session = sessionFactory.currentSession
        val criteria = session.createCriteria(T::class.javaClass)
        return criteria.list()?.filterIsInstance<T>() ?: listOf()
    }

    inline fun <reified T> findById(id: Long)
            = hibernateTemplate.get(T::class.javaClass, id) as T?

    inline fun <reified T> getById(id: Long)
            = hibernateTemplate.get(T::class.javaClass, id) as T?

    infix inline fun <reified T> save(target: T?) = {
        if (target != null) {
            hibernateTemplate.saveOrUpdate(target)
        }
    }

    infix inline fun <reified T> update(target: T?) = {
        if (target != null) {
            hibernateTemplate.update(target)
        }
    }

    infix inline fun <reified T> batchDelete(list: List<T>?)
            = hibernateTemplate.deleteAll(list ?: listOf<Any?>())

    infix inline fun <reified T> delete(target: T?) = {
        if (target != null) {
            hibernateTemplate.delete(target)
        }
    }
}