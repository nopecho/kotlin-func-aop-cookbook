package com.github.nopecho.funcaop.lock

import org.springframework.stereotype.Component

@Component
class LockFunc(private val managers: List<LockManager>) {

    companion object {
        private val lockMap = mutableMapOf<LockLevel, LockManager>()

        fun execute(option: LockOption, isDistributed: Boolean = false, block: () -> Unit) {
            when {
                isDistributed -> lockMap[LockLevel.DISTRIBUTED]?.lock(option, block)
                    ?: throw IllegalStateException("Distributed lock manager not found.")

                else -> lockMap[LockLevel.APPLICATION]?.lock(option, block)
                    ?: throw IllegalStateException("Application lock manager not found.")
            }
        }
    }

    init {
        managers.forEach { lockMap[it.getLockLevel()] = it }
    }
}