package com.github.nopecho.funcaop.lock

class LockAcquisitionFailureException(option: LockOption) :
    RuntimeException("Failed to acquire lock. key: ${option.key}, waitTime: ${option.waitTime}, leaseTime: ${option.leaseTime}, timeUnit: ${option.timeUnit}")