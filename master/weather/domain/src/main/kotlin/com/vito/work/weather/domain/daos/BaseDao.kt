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
    protected lateinit var hibernateTemplate: HibernateTemplate
    @Resource
    protected lateinit var sessionFactory: SessionFactory

    open fun <T> findAll(clazz: Class<T>): List<T> {
        val session = sessionFactory.currentSession
        val criteria = session.createCriteria(clazz)
        return criteria.list().filterIsInstance(clazz)
    }

    open fun <T> findById(clazz: Class<T>,id: Long): T? {
        return hibernateTemplate.get(clazz, id)
    }

    open fun <T> getById(clazz: Class<T>,id: Long): T? {
        return hibernateTemplate.get(clazz, id)
    }

    open infix fun save(target: Any?) {
        if (target != null) {
            hibernateTemplate.saveOrUpdate(target)
        }
    }

    open infix fun update(target: Any?) {
        if (target != null) {
            hibernateTemplate.update(target)
        }
    }

    open infix fun batchDelete(list: List<Any>?) {
        hibernateTemplate.deleteAll(list ?: listOf<Any?>())
    }

    open infix fun delete(target: Any?) {
        if (target != null) {
            hibernateTemplate.delete(target)
        }
    }
}