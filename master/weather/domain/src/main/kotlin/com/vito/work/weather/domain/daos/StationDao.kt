package com.vito.work.weather.domain.daos

import com.vito.work.weather.domain.entities.Station
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
open class StationDao : BaseDao()
{
    open fun findByNames(names: List<String>): MutableList<Station>?
    {
        if(names.size == 0)
        {
            return null
        }

        val criteria = sessionFactory.currentSession.createCriteria(Station::class.java)
        criteria.add(Restrictions.`in`("name_zh", names))
        return criteria.list() as MutableList<Station>
    }

    open fun findByDistrict(districtId: Long): List<Station>?
    {
        val criteria = sessionFactory.currentSession.createCriteria(Station::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        return criteria.list() as List<Station>?
    }
}