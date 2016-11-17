package com.vito.work.weather.domain.daos

import com.vito.work.weather.domain.entities.HistoryWeather
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
open class HistoryWeatherDao: BaseDao()
{
    open fun findByCityDate(cityId: Long, date: Date): HistoryWeather?
    {
        val criteria = sessionFactory.currentSession.createCriteria(HistoryWeather::class.java)
        criteria.add(Restrictions.eq("city", cityId))
        criteria.add(Restrictions.eq("date", date))
        val list = criteria.list()
        return if(list != null && list.size>0) list[0] as HistoryWeather? else null
    }

    open fun findByCityDates(cityId: Long, dates: List<Date>): List<HistoryWeather>?
    {
        if(dates.size == 0)
        {
            return null
        }
        val criteria = sessionFactory.currentSession.createCriteria(HistoryWeather::class.java)
        criteria.add(Restrictions.eq("city", cityId))
        criteria.add(Restrictions.`in`("date", dates))
        return criteria.list() as List<HistoryWeather>?
    }

    open fun findHighestTemperature(cityId: Long): HistoryWeather?
    {
        val criteria = sessionFactory.currentSession.createCriteria(HistoryWeather::class.java)
        criteria.add(Restrictions.eq("city", cityId))
        criteria.addOrder(Order.desc("max"))
        val maxHigest = criteria.list()[0] as HistoryWeather?

        return maxHigest
    }

    open fun findLowestTemperature(cityId: Long): HistoryWeather?
    {
        val criteria = sessionFactory.currentSession.createCriteria(HistoryWeather::class.java)
        criteria.add(Restrictions.eq("city", cityId))
        criteria.addOrder(Order.asc("min"))
        criteria.setMaxResults(1)
        return criteria.list()[0] as HistoryWeather?
    }

}