package com.vito.work.weather.repo

import org.hibernate.SessionFactory
import org.springframework.data.repository.NoRepositoryBean
import javax.persistence.*

/**
 * Created by lingzhiyuan.
 * Date : 16/4/1.
 * Time : 下午4:27.
 * Description:
 *
 */

@NoRepositoryBean
abstract class BaseDao {

    @PersistenceUnit
    private lateinit var emf: EntityManagerFactory
    @PersistenceContext
    private lateinit var em: EntityManager

    protected val sf: SessionFactory by lazy { emf.unwrap(SessionFactory::class.java) }

    open fun <T> findAll(clazz: Class<T>): List<T> {
        val builder = sf.criteriaBuilder
        val query = builder.createQuery(clazz)
        query.select(query.from(clazz))
        val typedQuery: TypedQuery<T> = em.createQuery(query)
        return typedQuery.resultList
    }

    open fun <T> findById(clazz: Class<T>,id: Long): T? {
        return sf.currentSession.get(clazz, id)
    }

    open fun <T> getById(clazz: Class<T>,id: Long): T? {
        return sf.currentSession.get(clazz, id)
    }

    open infix fun save(target: Any?) {
        if (target != null) {
            sf.currentSession.saveOrUpdate(target)
        }
    }

    open infix fun update(target: Any?) {
        if (target != null) {
            sf.currentSession.update(target)
        }
    }

    open infix fun batchDelete(list: List<Any>?) {
        list?.forEach { sf.currentSession.delete(it) }
    }

    open infix fun delete(target: Any?) {
        if (target != null) {
            sf.currentSession.delete(target)
        }
    }
}