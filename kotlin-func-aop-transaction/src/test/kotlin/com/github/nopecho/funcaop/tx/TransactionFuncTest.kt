package com.github.nopecho.funcaop.tx

import com.github.nopecho.funcaop.tx.support.User
import com.github.nopecho.funcaop.tx.support.UserJdbcRepository
import com.github.nopecho.testcontainers.EnableJdbcTestConfig
import com.github.nopecho.testcontainers.PostgresTestcontainers
import com.github.nopecho.testcontainers.TestcontainersSupport
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Service

@SpringBootTest(
    classes = [
        TransactionFunc::class,
        TransactionFunc.TransactionAdvice::class,
        TransactionFuncTest.TransactionTestService::class,
        UserJdbcRepository::class
    ]
)
@EnableJdbcTestConfig
class TransactionFuncTest : TestcontainersSupport(listOf(PostgresTestcontainers())) {

    @Autowired
    private lateinit var service: TransactionTestService

    @Autowired
    private lateinit var repository: UserJdbcRepository

    @Test
    fun `should execute read only transactional block`() {
        val actual = service.getUser(777)

        actual.id shouldBe 777
    }

    @Test
    fun `should execute transactional block`() {
        val actual = service.updateEmail(777, "update@email.com")

        actual.email shouldBe "update@email.com"
    }

    @Test
    fun `should rollback transaction`() {
        val before = service.getUser(777)
        shouldThrowAny { service.updateThrowing(before.id!!, "throw-rollback@email.com") }

        val actual = service.getUser(before.id!!)

        actual.email shouldBe before.email
    }

    @Test
    fun `should execute multiple transaction`() {
        TransactionFunc.execute {
            val user = repository.findById(777).orElseThrow()
            repository.save(user.copy(email = "update1@email.com"))
        }

        shouldThrowAny {
            TransactionFunc.execute {
                val user = repository.findById(777).orElseThrow()
                repository.save(user.copy(email = "update2@email.com"))
                throw IllegalStateException("Rollback Transaction!")
            }
        }

        repository.findById(777).orElseThrow().email shouldBe "update1@email.com"
    }


    @Service
    class TransactionTestService(private val repository: UserJdbcRepository) {
        fun getUser(id: Long): User = TransactionFunc.execute(readOnly = true) {
            repository.findById(id).orElseThrow()
        }

        fun updateEmail(id: Long, email: String): User = TransactionFunc.execute {
            val user = repository.findById(id).orElseThrow()
            user.copy(email = email).also { repository.save(it) }
        }

        fun updateThrowing(id: Long, email: String): User = TransactionFunc.execute {
            val user = repository.findById(id).orElseThrow()
            user.copy(email = email).also { repository.save(it) }
            throw IllegalStateException("Rollback Transaction!")
        }
    }
}