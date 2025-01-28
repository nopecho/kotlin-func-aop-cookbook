package com.github.nopecho.funcaop.lock

import java.util.concurrent.locks.Lock

interface LockManager {

    fun getLockLevel(): LockLevel

    fun getLock(option: LockOption): Lock

    fun tryLock(lock: Lock, option: LockOption): Boolean

    fun unlock(lock: Lock)

    fun <T> lock(option: LockOption, block: () -> T): T {
        val lock = getLock(option)
        val isLocked = tryLock(lock, option)
        return if (isLocked) {
            try {
                block()
            } finally {
                unlock(lock)
            }
        } else {
            throw IllegalStateException("Lock acquisition failure. key: ${option.key}, waitTime: ${option.waitTime}, leaseTime: ${option.leaseTime}, timeUnit: ${option.timeUnit}")
        }
    }
}