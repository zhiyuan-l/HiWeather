package com.vito.work.weather.repo

import com.vito.work.weather.dto.District
import org.hibernate.Criteria
import org.hibernate.criterion.Restrictions
import org.springframework.stereotype.Repository

/**
 * Created by lingzhiyuan.
 * Date : 3/2/17.
 * Time : 17:35.
 * Description:
 *
 */
@Repository
class DistrictDao : BaseDao(){

    fun findDistricts(cityId: Long): List<District> {
        val criteria: Criteria = sessionFactory.currentSession.createCriteria(District::class.java)
        criteria.add(Restrictions.eq("city", cityId))
        return criteria.list().filterIsInstance<District>()
    }

    fun findAQIDistrict(): List<District> {
        val criteria: Criteria = sessionFactory.currentSession.createCriteria(District::class.java)
        criteria.add(Restrictions.neOrIsNotNull("pinyin_aqi", ""))
        return criteria.list().filterIsInstance<District>()
    }

    fun findObsoleteDistricts(newIds: List<Long>): List<District> {
        val criteria: Criteria = sessionFactory.currentSession.createCriteria(District::class.java)
        criteria.add(Restrictions.not(Restrictions.`in`("id", newIds)))
        return criteria.list().filterIsInstance<District>()
    }
}