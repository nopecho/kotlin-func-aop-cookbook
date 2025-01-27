package com.github.nopecho.testcontainers

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName.parse


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(RedisTestcontainers.Companion.TestRedisConfig::class)
annotation class EnableRedisTestcontainersConfig


class RedisTestcontainers : TestcontainersMark {

    companion object {
        private const val REDIS_IMAGE = "redis:7.0-alpine"

        @Container
        val redisContainer = GenericContainer<Nothing>(parse(REDIS_IMAGE)).apply {
            exposedPorts = listOf(6379)
            start()
        }

        @TestConfiguration
        class TestRedisConfig {
            @Bean
            @Primary
            fun testConnectionFactory(): RedisConnectionFactory {
                val configuration = RedisStandaloneConfiguration().apply {
                    hostName = redisContainer.host
                    port = redisContainer.firstMappedPort
                }
                return LettuceConnectionFactory(configuration)
            }
        }
    }
}