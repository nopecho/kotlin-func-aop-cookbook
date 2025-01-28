package com.github.nopecho.funcaop.lock.distributed

import com.github.nopecho.concurrent.ConcurrentUtils
import com.github.nopecho.funcaop.lock.LockFunc
import com.github.nopecho.funcaop.lock.LockOption
import com.github.nopecho.testcontainers.EnableRedissonTestConfig
import com.github.nopecho.testcontainers.RedisTestcontainers
import com.github.nopecho.testcontainers.TestcontainersSupport
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.redisson.api.RedissonClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit

@ActiveProfiles("test")
@SpringBootTest(
    classes = [
        LockFunc::class,
        RedissonDistributedLockManager::class,
        RedissonClient::class
    ]
)
@EnableRedissonTestConfig
class RedissonDistributedLockManagerTest : TestcontainersSupport(
    containers = listOf(RedisTestcontainers())
) {

    @Test
    fun `should redisson lock acquired`() {
        val account = Account(0)
        val lockOption = LockOption(
            key = "account",
            waitTime = 1500,
            leaseTime = 100,
            timeUnit = TimeUnit.MILLISECONDS
        )
        val concurrentCount = 1_000

        ConcurrentUtils.run(concurrentCount) {
            LockFunc.execute(lockOption, isDistributed = true) {
                runTest { account.deposit(100) }
            }
        }

        account.balance shouldBe 100 * concurrentCount
    }

    data class Account(var balance: Int) {

        suspend fun deposit(amount: Int) {
            delay((10..30).random().toLong())
            balance += amount
        }
    }
}