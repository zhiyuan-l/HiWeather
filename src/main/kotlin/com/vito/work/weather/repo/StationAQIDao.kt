package com.vito.work.weather.repo

import com.vito.work.weather.dto.StationAQI
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import org.springframework.stereotype.Repository

/**
 * Created by lingzhiyuan.
 * Date : 16/4/17.
 * Time : 下午5:35.
 * Description:
 *
 */

@Repository
class StationAQIDao : BaseDao() {
    fun findLatestByStations(stationIds: List<Long>): List<StationAQI> {
        if (stationIds.isEmpty()) {
            return listOf()
        }
        val criteria = sf.currentSession.createCriteria(StationAQI::class.java)
        criteria.add(Restrictions.`in`("station", stationIds))
        criteria.addOrder(Order.desc("datetime"))
        criteria.setMaxResults(stationIds.size)
        return criteria.list().filterIsInstance<StationAQI>()
    }

}