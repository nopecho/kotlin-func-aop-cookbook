package com.github.nopecho.funcaop.lock

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

@Component
class ReentrantLockManger : LockManager {

    private val concurrentMap = ConcurrentHashMap<String, ReentrantLock>()

    override fun getLockLevel(): LockLevel = LockLevel.APPLICATION

    override fun getLock(option: LockOption): Lock {
        return concurrentMap.getOrPut(option.key) { ReentrantLock(true) }
    }

    override fun tryLock(lock: Lock, option: LockOption): Boolean {
        return lock.tryLock(option.waitTime, option.timeUnit)
    }

    override fun unlock(lock: Lock) {
        lock.unlock()
    }
}