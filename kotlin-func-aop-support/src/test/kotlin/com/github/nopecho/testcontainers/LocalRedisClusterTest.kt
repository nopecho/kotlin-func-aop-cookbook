package com.github.nopecho.testcontainers

import com.github.nopecho.concurrent.ConcurrentUtils
import com.github.nopecho.concurrent.VIRTUAL
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisNode
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import java.time.Duration
import kotlin.random.Random

class LocalRedisClusterTest {

    private val nodes = listOf(
        RedisNode("127.0.0.1", 17001),
        RedisNode("127.0.0.1", 17002),
        RedisNode("127.0.0.1", 17003)
    )
    private val password = RedisPassword.of("test")

    private val factory: LettuceConnectionFactory = localLettuceClusterFactory(password, nodes)

    private val redisTemplate: StringRedisTemplate = StringRedisTemplate(factory)

    private val stringOperation = redisTemplate.opsForValue()

    private val listOperations = redisTemplate.opsForList()

    private val CONCURRENT_CALL_COUNT = 100

    @BeforeEach
    fun setUp() {
        factory.start()
    }

    @AfterEach
    fun tearDown() {
        factory.clusterConnection.serverCommands().flushAll()
        factory.stop()
    }

    @Test
    fun `simple set of 10,000 items`() {
        ConcurrentUtils.run(CONCURRENT_CALL_COUNT, Dispatchers.VIRTUAL) {
            repeat(100) {
                stringOperation.set("test:set:${rand()}", "${rand()}")
            }
        }

        val actual = redisTemplate.keys("test:set:*").size

        actual shouldBe CONCURRENT_CALL_COUNT * 100
    }

    @Test
    fun `multi set of 10,000 items`() {
        ConcurrentUtils.run(CONCURRENT_CALL_COUNT, Dispatchers.VIRTUAL) {
            val map = mutableMapOf<String, String>()
            repeat(100) {
                map["test:mset:${rand()}"] = "${rand()}"
            }
            stringOperation.multiSet(map)
        }

        val actual = redisTemplate.keys("test:mset:*").size

        actual shouldBe CONCURRENT_CALL_COUNT * 100
    }

    @Test
    fun `pipeline set of 10,000 items`() {
        ConcurrentUtils.run(CONCURRENT_CALL_COUNT, Dispatchers.VIRTUAL) {
            redisTemplate.executePipelined {
                repeat(100) {
                    stringOperation.set("test:pipeline:${rand()}", "${rand()}")
                }
                null
            }
        }

        val actual = redisTemplate.keys("test:pipeline:*").size

        actual shouldBe CONCURRENT_CALL_COUNT * 100
    }

    @Test
    fun `list rpush of 1,000 items`() {
        ConcurrentUtils.run(CONCURRENT_CALL_COUNT, Dispatchers.VIRTUAL) {
            val key = "test:rpush:${Thread.currentThread().name}"
            repeat(1000) {
                listOperations.rightPush(key, "${rand()}")
            }
        }
    }

    @Test
    fun `list rpush all of 1,000 items (100 x 100)`() {
        ConcurrentUtils.run(CONCURRENT_CALL_COUNT, Dispatchers.VIRTUAL) {
            val key = "test:rpush-all:${Thread.currentThread().name}"
            val values = (1..100).map { "${rand()}" }.toList()
            listOperations.rightPushAll(key, values)
        }
    }

    @Test
    fun `lua script set of 100,000 items`() {
        val script = """
            local time = redis.call('TIME')
            local seed = tonumber(time[1]) + tonumber(time[2])
            math.randomseed(seed)
            for i = 1, 1000 do
                local key = KEYS[1] .. math.random(1, 2000000000)
                local value = math.random(1, 1000000000)
                redis.call('SET', key, value)
            end
            return redis.status_reply('OK')
        """.trimIndent()
        val luaScript = DefaultRedisScript(script, String::class.java)

        ConcurrentUtils.run(CONCURRENT_CALL_COUNT, Dispatchers.VIRTUAL) {
            val hashTagKey = "{test:lua:set}:"
            redisTemplate.execute(luaScript, listOf(hashTagKey))
        }

        val actual = redisTemplate.keys("{test:lua:set}:*").size

        actual shouldBe CONCURRENT_CALL_COUNT * 1000
    }

    @Test
    fun `lua script mset of 100,000 items`() {
        val script = """
            local time = redis.call('TIME')
            local seed = tonumber(time[1]) + tonumber(time[2])
            math.randomseed(seed)
            
            local args = {}
            for i = 1, 1000 do
                table.insert(args, KEYS[1] .. math.random(1, 2000000000))
                table.insert(args, math.random(1, 1000000000))
            end
            redis.call('MSET', unpack(args))
            return redis.status_reply('OK')
        """.trimIndent()
        val luaScript = DefaultRedisScript(script, String::class.java)

        ConcurrentUtils.run(CONCURRENT_CALL_COUNT, Dispatchers.VIRTUAL) {
            val hashTagKey = "{test:lua:mset}:"
            redisTemplate.execute(luaScript, listOf(hashTagKey))
        }

        val actual = redisTemplate.keys("{test:lua:mset}:*").size

        actual shouldBe CONCURRENT_CALL_COUNT * 1000
    }

    private fun rand() = Random.nextLong(1, Long.MAX_VALUE)

    private fun localLettuceClusterFactory(pass: RedisPassword, node: List<RedisNode>): LettuceConnectionFactory {
        val clusterConfig = RedisClusterConfiguration().apply {
            password = pass
            maxRedirects = 3
            node.forEach(::addClusterNode)
        }
        val clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(5))
            .shutdownTimeout(Duration.ZERO)
            .build()

        return LettuceConnectionFactory(clusterConfig, clientConfig)
    }
}