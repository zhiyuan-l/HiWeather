package com.vito.work.weather.repo

import com.vito.work.weather.dto.ForecastWeather
import org.hibernate.Criteria
import org.hibernate.criterion.Restrictions
import org.springframework.stereotype.Repository
import java.sql.Date

/**
 * Created by lingzhiyuan.
 * Date : 16/4/10.
 * Time : 下午9:40.
 * Description:
 *
 */

@Repository
class ForcastWeatherDao : BaseDao() {

    /**
     * 获取特定区县一个日期的天气预报
     * */
    fun findByDistrictDate(districtId: Long, date: Date): ForecastWeather? {
        val criteria: Criteria = sessionFactory.currentSession.createCriteria(ForecastWeather::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        criteria.add(Restrictions.eq("date", date))
        val list = criteria.list().filterIsInstance<ForecastWeather>()
        return list.firstOrNull()
    }

    /**
     * 获取特定区县一些日期的预报天气
     * */
    fun findByDistrictDates(districtId: Long, dates: List<Date>): List<ForecastWeather>? {
        if (dates.isEmpty()) return null
        val criteria: Criteria = sessionFactory.currentSession.createCriteria(ForecastWeather::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        criteria.add(Restrictions.`in`("date", dates))
        return criteria.list().filterIsInstance<ForecastWeather>()
    }

    /**
     * 获取特定区县的特定日期区间的天气
     * */
    fun findWeathersAfter(districtId: Long, date: Date, maxResult: Int): List<ForecastWeather> {
        val criteria: Criteria = sessionFactory.currentSession.createCriteria(ForecastWeather::class.java)
        criteria.add(Restrictions.eq("district", districtId))
        criteria.add(Restrictions.ge("date", date))
        criteria.setMaxResults(maxResult)
        return criteria.list().filterIsInstance<ForecastWeather>()
    }
}