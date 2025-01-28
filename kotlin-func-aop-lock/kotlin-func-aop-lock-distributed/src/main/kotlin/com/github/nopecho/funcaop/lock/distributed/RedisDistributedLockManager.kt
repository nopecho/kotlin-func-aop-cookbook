package com.github.nopecho.funcaop.lock.distributed

import com.github.nopecho.funcaop.lock.LockLevel
import com.github.nopecho.funcaop.lock.LockManager
import com.github.nopecho.funcaop.lock.LockOption
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.locks.Lock

@Component
class RedisDistributedLockManager(private val client: RedissonClient) : LockManager {

    override fun getLockLevel(): LockLevel = LockLevel.DISTRIBUTED

    override fun getLock(option: LockOption): Lock {
        return client.getLock(option.key)
    }

    override fun tryLock(lock: Lock, option: LockOption): Boolean {
        lock as RLock
        return lock.tryLock(option.waitTime, option.leaseTime, option.timeUnit)
    }

    override fun unlock(lock: Lock) {
        lock as RLock
        lock.unlock()
    }
}