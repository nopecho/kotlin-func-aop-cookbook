package com.github.nopecho.funcaop.cache

import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component

@Component
class CacheFunc(private val _manager: CacheManager) {

    companion object {
        private lateinit var manager: CacheManager

        fun <T> cacheable(name: String, key: String, block: () -> T): T {
            val cache = manager.getCache(name)
            cache?.get(key)?.get()?.let {
                return runCatching { it as T }
                    .getOrNull()
                    ?: cache.executeAndPut(key, block)
            }

            return cache.executeAndPut(key, block)
        }

        fun evict(name: String, key: String) {
            manager.getCache(name)?.evict(key)
        }

        fun clear(name: String) {
            manager.getCache(name)?.clear()
        }

        fun put(name: String, key: String, block: () -> Any?) {
            block()?.let {
                manager.getCache(name)?.put(key, it)
            }
        }

        private fun <T> Cache?.executeAndPut(key: String, block: () -> T): T {
            return block().also {
                if (it == null || (it is Collection<*> && it.isEmpty())) {
                    return@also
                }
                this?.put(key, it)
            }
        }
    }

    init {
        manager = _manager
    }
}