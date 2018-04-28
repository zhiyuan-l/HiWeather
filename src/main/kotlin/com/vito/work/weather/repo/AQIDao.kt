package com.vito.work.weather.repo

import com.vito.work.weather.dto.AQI
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import org.springframework.stereotype.Repository

/**
 * Created by lingzhiyuan.
 * Date : 16/4/17.
 * Time : 下午4:11.
 * Description:
 *
 */

@Repository
class AQIDao : BaseDao() {

    fun findLatestByDistrict(districtId: Long): AQI? {
        val criteria = sf.currentSession.createCriteria(AQI::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        criteria.addOrder(Order.desc("update_time"))
        criteria.setMaxResults(1)
        val list = criteria.list()
        return list.filterIsInstance<AQI>().firstOrNull() ?: AQI()
    }
}