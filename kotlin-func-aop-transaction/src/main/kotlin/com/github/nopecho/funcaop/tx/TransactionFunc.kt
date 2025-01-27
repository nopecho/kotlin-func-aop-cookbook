package com.github.nopecho.funcaop.tx

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional


@Component
class TransactionFunc(private val _advice: TransactionAdvice) {

    companion object {
        private lateinit var advice: TransactionAdvice

        fun <T> execute(readOnly: Boolean = false, block: () -> T): T = when {
            readOnly -> advice.readOnly(block)
            else -> advice.transactional(block)
        }
    }

    init {
        advice = _advice
    }

    @Component
    class TransactionAdvice {

        @Transactional
        fun <T> transactional(block: () -> T): T = block()

        @Transactional(readOnly = true)
        fun <T> readOnly(block: () -> T): T = block()

        // @Transactional(propagation = ..., isolation = ...)
    }
}