package com.vito.work.weather.domain.daos

import com.vito.work.weather.domain.entities.StationAQI
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
open class StationAQIDao : BaseDao()
{
    open fun findLatestByStations(stationIds: List<Long>): List<StationAQI>?
    {
        if(stationIds.size == 0)
        {
            return null
        }
        val criteria = sessionFactory.currentSession.createCriteria(StationAQI::class.java)
        criteria.add(Restrictions.`in`("station",stationIds))
        criteria.addOrder(Order.desc("datetime"))
        criteria.setMaxResults(stationIds.size)

        val result = criteria.list() as List<StationAQI>?

        return result
    }

}