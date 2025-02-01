package com.github.nopecho.testcontainers

import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisNode
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.junit.jupiter.Container
import java.io.File
import java.time.Duration


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(RedisClusterTestcontainers.Companion.TestLettuceClusterConfig::class)
annotation class EnableLettuceClusterTestConfig

class RedisClusterTestcontainers : TestcontainersMark {

    companion object {
        private val COMPOSE_FILE = File("src/testFixtures/resources/testcontainers/redis/docker-compose.yml")
        private val REDIS_NODE_MAP = mapOf(
            "redis-node-1-1" to 17001,
            "redis-node-2-1" to 17002,
            "redis-node-3-1" to 17003
        )

        @Container
        val redisClusterContainer = ComposeContainer(COMPOSE_FILE).apply {
            REDIS_NODE_MAP.forEach { (name, port) -> withExposedService(name, port) }
            start()
        }

        @TestConfiguration
        class TestLettuceClusterConfig {

            @Bean
            @Primary
            fun testRedisConnectionFactory(): RedisConnectionFactory {
                val clusterConfig = RedisClusterConfiguration().apply {
                    password = RedisPassword.of("test")
                    maxRedirects = 3
                    REDIS_NODE_MAP.toRedisNodes().forEach(::addClusterNode)
                }

                val refreshOptions = ClusterTopologyRefreshOptions.builder()
                    .enableAllAdaptiveRefreshTriggers()
                    .refreshPeriod(Duration.ofSeconds(30))
                    .build()

                val clusterClientOptions = ClusterClientOptions.builder()
                    .topologyRefreshOptions(refreshOptions)
                    .build()

                val clientConfig = LettuceClientConfiguration.builder()
                    .clientOptions(clusterClientOptions)
                    .commandTimeout(Duration.ofMillis(500))
                    .shutdownTimeout(Duration.ZERO)
                    .build()

                return LettuceConnectionFactory(clusterConfig, clientConfig)
            }

            private fun Map<String, Int>.toRedisNodes(): Set<RedisNode> {
                return map { (name, port) ->
                    val redisHost = redisClusterContainer.getServiceHost(name, port)
                    val redisPort = redisClusterContainer.getServicePort(name, port)
                    println("redisHost: $redisHost, redisPort: $redisPort")
                    RedisNode(redisHost, redisPort)
                }.toSet()
            }
        }
    }
}