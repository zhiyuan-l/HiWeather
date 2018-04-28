package com.vito.work.weather.repo

import com.vito.work.weather.dto.HistoryWeather
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import org.springframework.stereotype.Repository
import java.sql.Date

/**
 * Created by lingzhiyuan.
 * Date : 16/4/5.
 * Time : 下午6:39.
 * Description:
 *
 */

@Repository
class HistoryWeatherDao : BaseDao() {
    fun findByCityDate(cityId: Long, date: Date): HistoryWeather? {
        val criteria = sf.currentSession.createCriteria(HistoryWeather::class.java)
        criteria.add(Restrictions.eq("city", cityId))
        criteria.add(Restrictions.eq("date", date))
        val list = criteria.list().filterIsInstance<HistoryWeather>()
        return list.firstOrNull()
    }

    fun findByCityDates(cityId: Long, dates: List<Date>): List<HistoryWeather> {
        if (dates.isEmpty()) return listOf()
        val criteria = sf.currentSession.createCriteria(HistoryWeather::class.java)
        criteria.add(Restrictions.eq("city", cityId))
        criteria.add(Restrictions.`in`("date", dates))
        return criteria.list().filterIsInstance<HistoryWeather>()
    }

    fun findHighestTemperature(cityId: Long): HistoryWeather? {
        val criteria = sf.currentSession.createCriteria(HistoryWeather::class.java)
        criteria.add(Restrictions.eq("city", cityId))
        criteria.addOrder(Order.desc("max"))
        return criteria.list().firstOrNull() as HistoryWeather?
    }

    fun findLowestTemperature(cityId: Long): HistoryWeather? {
        val criteria = sf.currentSession.createCriteria(HistoryWeather::class.java)
        criteria.add(Restrictions.eq("city", cityId))
        criteria.addOrder(Order.asc("min"))
        criteria.setMaxResults(1)
        return criteria.list().firstOrNull() as HistoryWeather?
    }
}