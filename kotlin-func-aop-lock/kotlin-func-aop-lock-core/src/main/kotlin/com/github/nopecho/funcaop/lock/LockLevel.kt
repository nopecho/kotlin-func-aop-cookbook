package com.github.nopecho.funcaop.lock


enum class LockLevel {
    APPLICATION, // Application level lock
    DISTRIBUTED, // Global level Distributed lock (e.g. Redis)
}
