package com.github.nopecho.testcontainers

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName.parse
import javax.sql.DataSource


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(PostgresTestcontainers.Companion.TestJdbcConfig::class)
annotation class EnablePostgresTestcontainersConfig


class PostgresTestcontainers : TestcontainersMark {

    companion object {
        private const val POSTGRES_IMAGE: String = "postgres:16-alpine"
        private const val POSTGRES_INIT_SCRIPT: String = "init.sql" // resources path

        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>(parse(POSTGRES_IMAGE)).apply {
            withDatabaseName("test")
            withUsername("test")
            withPassword("test")
            withInitScript(POSTGRES_INIT_SCRIPT)
            start()
        }

        @EnableJdbcAuditing
        @EnableTransactionManagement
        @EnableJdbcRepositories(basePackages = ["com.github.nopecho"])
        @TestConfiguration
        class TestJdbcConfig : AbstractJdbcConfiguration() {
            @Bean
            @Primary
            fun dataSource(): DataSource = HikariDataSource().apply {
                driverClassName = postgresContainer.driverClassName
                jdbcUrl = postgresContainer.jdbcUrl
                username = postgresContainer.username
                password = postgresContainer.password
            }

            @Bean
            @Primary
            fun namedParameterJdbcOperations(source: DataSource): NamedParameterJdbcOperations =
                NamedParameterJdbcTemplate(source)

            @Bean
            @Primary
            fun transactionManager(source: DataSource): PlatformTransactionManager =
                DataSourceTransactionManager(source)
        }
    }
}