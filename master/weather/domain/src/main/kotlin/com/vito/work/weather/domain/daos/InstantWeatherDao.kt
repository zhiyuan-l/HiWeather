package com.vito.work.weather.domain.daos

import com.vito.work.weather.domain.entities.InstantWeather
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.LocalDateTime

/**
 * Created by lingzhiyuan.
 * Date : 16/4/19.
 * Time : 下午2:44.
 * Description:
 *
 */

@Repository
open class InstantWeatherDao : BaseDao()
{

    /**
     * 查找最新的天气, 但是必须要早于现在
     * */
    open fun findLatestByDistrictId(districtId: Long): InstantWeather?
    {
        val criteria = sessionFactory.currentSession.createCriteria(InstantWeather::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        criteria.add(Restrictions.lt("datetime", Timestamp.valueOf(LocalDateTime.now())))
        criteria.addOrder(Order.desc("datetime"))
        criteria.setMaxResults(1)
        return criteria.list().getOrNull(0) as InstantWeather?
    }
}