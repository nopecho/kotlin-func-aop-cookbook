package com.github.nopecho.testcontainers

import com.github.nopecho.concurrent.ConcurrentUtils
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import kotlin.random.Random

@SpringBootTest(classes = [RedisConnectionFactory::class])
@EnableLettuceClusterTestConfig
class RedisClusterTestcontainersTest : TestcontainersSupport(
    containers = listOf(
        RedisClusterTestcontainers()
    )
) {

    @Autowired
    private lateinit var factory: RedisConnectionFactory

    private lateinit var redisTemplate: StringRedisTemplate

    @BeforeEach
    fun setUp() {
        redisTemplate = StringRedisTemplate(factory)
    }

    @Test
    fun `cluster set test`() {
        ConcurrentUtils.run(1000) {
            redisTemplate.opsForValue()
                .set("test-${Random.nextLong(1, Long.MAX_VALUE)}", "${Random.nextLong(1, Long.MAX_VALUE)}")
        }

        val actual = redisTemplate.keys("test-*").size
        println(actual)

        actual shouldBe 1000
    }
}