package com.vito.work.weather.repo

import com.vito.work.weather.dto.City
import org.hibernate.Criteria
import org.hibernate.criterion.Restrictions
import org.springframework.stereotype.Repository

/**
 * Created by lingzhiyuan.
 * Date : 3/2/17.
 * Time : 17:36.
 * Description:
 *
 */

@Repository
class CityDao : BaseDao() {

    fun findCities(provinceId: Long): List<City> {
        val criteria: Criteria = sf.currentSession.createCriteria(City::class.java)
        criteria.add(Restrictions.eq("province", provinceId))
        return criteria.list().filterIsInstance<City>()
    }

}