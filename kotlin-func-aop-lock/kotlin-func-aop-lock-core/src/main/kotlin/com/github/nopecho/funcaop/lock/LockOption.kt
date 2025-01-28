package com.github.nopecho.funcaop.lock

import java.util.concurrent.TimeUnit


data class LockOption(
    val key: String,
    val waitTime: Long = 3,
    val leaseTime: Long = 3,
    val timeUnit: TimeUnit = TimeUnit.SECONDS,
)
