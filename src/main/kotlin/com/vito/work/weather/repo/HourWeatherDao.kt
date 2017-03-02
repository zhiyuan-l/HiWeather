package com.vito.work.weather.repo

import com.vito.work.weather.dto.HourWeather
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
class HourWeatherDao : BaseDao() {
    fun findByDistrictDateTime(districtId: Long, dateTime: LocalDateTime): HourWeather? {
        val time = LocalDateTime.of(dateTime.year, dateTime.month, dateTime.dayOfMonth, dateTime.hour, 0, 0, 0)
        val criteria = sessionFactory.currentSession.createCriteria(HourWeather::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        criteria.add(Restrictions.eq("datetime", Timestamp.valueOf(time)))
        criteria.setMaxResults(1)

        return criteria.list().filterIsInstance<HourWeather>().firstOrNull()
    }

    fun find24HByDistrict(districtId: Long): List<HourWeather> {
        val criteria = sessionFactory.currentSession.createCriteria(HourWeather::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        // 根据时间降序排列
        criteria.addOrder(Order.desc("datetime"))
        criteria.setMaxResults(24)
        return criteria.list().filterIsInstance<HourWeather>()
    }

    fun findByDistrictDatetimes(districtId: Long, datetimes: List<Timestamp>): List<HourWeather> {
        val criteria = sessionFactory.currentSession.createCriteria(HourWeather::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        // 根据时间降序排列
        criteria.add(Restrictions.`in`("datetime", datetimes))
        return criteria.list().filterIsInstance<HourWeather>()
    }

    /**
     * 查找最新的天气, 但是必须要早于现在
     * */
    fun findLatestByDistrictId(districtId: Long): HourWeather? {
        val criteria = sessionFactory.currentSession.createCriteria(HourWeather::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        criteria.add(Restrictions.lt("datetime", Timestamp.valueOf(LocalDateTime.now())))
        criteria.addOrder(Order.desc("datetime"))
        return criteria.list().filterIsInstance<HourWeather>().firstOrNull()
    }
}