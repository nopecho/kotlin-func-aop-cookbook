package com.github.nopecho.funcaop.lock

import com.github.nopecho.concurrent.ConcurrentUtils
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@SpringJUnitConfig(
    classes = [LockFunc::class, ReentrantLockManger::class]
)
class LockFuncTest {

    @Test
    fun `should not using lock problem`() {
        val account = Account(0)

        ConcurrentUtils.run(callCount = 10_000) {
            runTest { account.suspendDeposit(100) }
        }

        account.balance shouldNotBe 100 * 10_000
    }


    @Test
    fun `should not using lock problem 2`() {
        val account = Account(0)

        ConcurrentUtils.runThread(threadCount = 200) {
            account.deposit(100)
        }

        account.balance shouldNotBe 100 * 200
    }

    @Test
    fun `should acquired lock`() {
        val account = Account(0)
        val lockOption = LockOption("account", 10)

        ConcurrentUtils.runThread(threadCount = 200) {
            LockFunc.execute(lockOption) {
                account.deposit(100)
            }
        }

        account.balance shouldBe 100 * 200
    }


    data class Account(var balance: Int) {

        fun deposit(amount: Int) {
            Thread.sleep((10..30).random().toLong())
            balance += amount
        }

        suspend fun suspendDeposit(amount: Int) {
            delay((10..30).random().toLong())
            balance += amount
        }
    }
}