package com.vito.work.weather.domain.services

import java.time.Duration
import java.time.Instant

/**
 * Created by lingzhiyuan.
 * Date : 2016/11/16.
 * Time : 17:03.
 * Description:
 *
 */

abstract class UseLock {

    var isLocked: Boolean = false
    var start: Instant? = null
    var end: Instant? = null

    fun lock() {
        if (isLocked) throw IllegalStateException("Already Locked")
        start = Instant.now()
        isLocked = true
    }

    fun unlock() {
        end = Instant.now()
        isLocked = false
    }

    fun getDuration(): Duration {
        if (isLocked) {
            throw IllegalStateException("Lock Is On")
        }
        if (start == null || end == null) {
            throw IllegalStateException("Lock hasn't run yet")
        }
        return Duration.between(start, end)
    }
}