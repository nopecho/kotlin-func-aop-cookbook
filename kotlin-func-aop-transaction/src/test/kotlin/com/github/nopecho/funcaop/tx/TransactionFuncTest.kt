package com.github.nopecho.funcaop.tx

import com.github.nopecho.funcaop.tx.support.User
import com.github.nopecho.funcaop.tx.support.UserJdbcRepository
import com.github.nopecho.testcontainers.EnablePostgresTestcontainersConfig
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
@EnablePostgresTestcontainersConfig
class TransactionFuncTest : TestcontainersSupport(listOf(PostgresTestcontainers())) {

    @Autowired
    private lateinit var sut: TransactionTestService

    @Test
    fun `should execute read only transactional block`() {
        val actual = sut.getUser(777)

        actual.id shouldBe 777
    }

    @Test
    fun `should execute transactional block`() {
        val actual = sut.updateEmail(777, "update@email.com")

        actual.email shouldBe "update@email.com"
    }

    @Test
    fun `should rollback transaction`() {
        val before = sut.getUser(777)
        shouldThrowAny { sut.updateThrowing(before.id!!, "throw-rollback@email.com") }

        val actual = sut.getUser(before.id!!)

        actual.email shouldBe before.email
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