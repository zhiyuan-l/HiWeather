package com.vito.work.weather.repo

import com.vito.work.weather.dto.InstantWeather
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
class InstantWeatherDao : BaseDao() {

    /**
     * 查找最新的天气, 但是必须要早于现在
     * */
    fun findLatestByDistrictId(districtId: Long): InstantWeather? {
        val criteria = sf.currentSession.createCriteria(InstantWeather::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        criteria.add(Restrictions.lt("datetime", Timestamp.valueOf(LocalDateTime.now())))
        criteria.addOrder(Order.desc("datetime"))
        return criteria.list().filterIsInstance<InstantWeather>().firstOrNull()
    }
}