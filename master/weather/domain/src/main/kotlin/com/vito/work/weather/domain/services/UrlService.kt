package com.vito.work.weather.domain.services

import com.vito.work.weather.domain.daos.UrlDao
import com.vito.work.weather.domain.entities.Url
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Created by lingzhiyuan.
 * Date : 16/4/8.
 * Time : 上午10:29.
 * Description:
 *
 */

@Service
@Transactional
open class UrlService @Autowired constructor(val urlDao: UrlDao)
{
    open fun deleteAll(type: Int)
    {
        var list = urlDao.findByType(type) as List<Url>
        urlDao.batchDelete(list)
    }

    open fun getCount(type: Int): Int?
    {
        return urlDao.getCount(type)
    }
}