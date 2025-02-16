package com.github.nopecho.funcaop.lock

import java.util.concurrent.locks.Lock

interface LockManager {

    fun getLockLevel(): LockLevel

    fun getLock(option: LockOption): Lock

    fun tryLock(lock: Lock, option: LockOption): Boolean

    fun unlock(lock: Lock)

    fun <T> lock(option: LockOption, block: () -> T): T {
        val lock = getLock(option)
        return if (tryLock(lock, option)) {
            try {
                block()
            } finally {
                unlock(lock)
            }
        } else {
            throw LockAcquisitionFailureException(option)
        }
    }
}