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
open class AQIDao : BaseDao() {
    open fun findLatestByDistrict(districtId: Long): AQI? {
        val criteria = sessionFactory.currentSession.createCriteria(AQI::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        criteria.addOrder(Order.desc("update_time"))
        criteria.setMaxResults(1)
        val list = criteria.list()
        return if (list != null && list.size != 0) list[0] as AQI else AQI()
    }
}