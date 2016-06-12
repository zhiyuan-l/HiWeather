package com.vito.work.weather.domain.daos

import com.vito.work.weather.domain.entities.HourWeather
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
open class HourWeatherDao: BaseDao()
{
    open fun findByDistrictDateTime(districtId: Long, dateTime: LocalDateTime): HourWeather?
    {
        var time = LocalDateTime.of(dateTime.year, dateTime.month, dateTime.dayOfMonth, dateTime.hour, 0, 0,0)
        var criteria = sessionFactory.currentSession.createCriteria(HourWeather::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        criteria.add(Restrictions.eq("datetime", Timestamp.valueOf(time)))
        criteria.setMaxResults(1)

        return criteria.list() as HourWeather
    }

    open fun find24HByDistrict(districtId: Long): List<HourWeather>?
    {
        var criteria = sessionFactory.currentSession.createCriteria(HourWeather::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        // 根据时间降序排列
        criteria.addOrder(Order.desc("datetime"))
        criteria.setMaxResults(24)
        return criteria.list() as List<HourWeather>?
    }

    open fun findByDistrictDatetimes(districtId: Long, datetimes: MutableList<Timestamp>): List<HourWeather>?
    {
        var criteria = sessionFactory.currentSession.createCriteria(HourWeather::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        // 根据时间降序排列
        criteria.add(Restrictions.`in`("datetime",datetimes))
        return criteria.list() as List<HourWeather>?
    }

    /**
     * 查找最新的天气, 但是必须要早于现在
     * */
    open fun findLatestByDistrictId(districtId: Long): HourWeather?
    {
        var criteria = sessionFactory.currentSession.createCriteria(HourWeather::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        criteria.add(Restrictions.lt("datetime", Timestamp.valueOf(LocalDateTime.now())))
        criteria.addOrder(Order.desc("datetime"))
        criteria.setMaxResults(1)
        return criteria.list() as HourWeather?
    }
}