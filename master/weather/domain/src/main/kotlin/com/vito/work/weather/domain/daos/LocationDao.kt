package com.vito.work.weather.domain.daos

import com.vito.work.weather.domain.entities.City
import com.vito.work.weather.domain.entities.District
import org.hibernate.Criteria
import org.hibernate.criterion.Restrictions
import org.springframework.stereotype.Repository

/**
 * Created by lingzhiyuan.
 * Date : 16/4/1.
 * Time : 下午9:51.
 * Description:
 *
 */

@Repository
open class LocationDao : BaseDao() {
    open fun findCities(provinceId: Long): MutableList<Any?>? {
        val criteria: Criteria = sessionFactory.currentSession.createCriteria(City::class.java)
        criteria.add(Restrictions.eq("province", provinceId))

        return criteria.list()
    }

    open fun findDistricts(cityId: Long): List<District>? {
        val criteria: Criteria = sessionFactory.currentSession.createCriteria(District::class.java)
        criteria.add(Restrictions.eq("city", cityId))
        return criteria.list() as List<District>
    }

    open fun findAQIDistrict(): List<District>? {
        val criteria: Criteria = sessionFactory.currentSession.createCriteria(District::class.java)
        criteria.add(Restrictions.neOrIsNotNull("pinyin_aqi", ""))

        return criteria.list() as List<District>
    }

    open fun findObsoleteDistricts(newIds: MutableList<Long>): List<District>? {
        val criteria: Criteria = sessionFactory.currentSession.createCriteria(District::class.java)
        criteria.add(Restrictions.not(Restrictions.`in`("id", newIds)))
        return criteria.list() as List<District>
    }
}