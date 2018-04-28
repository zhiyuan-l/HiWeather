package com.vito.work.weather.repo

import com.vito.work.weather.dto.Station
import org.hibernate.criterion.Restrictions
import org.springframework.stereotype.Repository

/**
 * Created by lingzhiyuan.
 * Date : 16/4/17.
 * Time : 下午5:40.
 * Description:
 *
 */

@Repository
class StationDao : BaseDao() {
    fun findByNames(names: List<String>): List<Station> {
        if (names.isEmpty()) {
            return listOf()
        }

        val criteria = sf.currentSession.createCriteria(Station::class.java)
        criteria.add(Restrictions.`in`("name_zh", names))
        return criteria.list().filterIsInstance<Station>()
    }

    fun findByDistrict(districtId: Long): List<Station> {
        val criteria = sf.currentSession.createCriteria(Station::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        return criteria.list().filterIsInstance<Station>()
    }
}