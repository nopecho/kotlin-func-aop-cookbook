package com.github.nopecho.funcaop.tx.support

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime


@Table("users")
data class User(
    @Id
    @Column("id")
    val id: Long? = null,
    @Column("email")
    val email: String,
    @Column("created_at")
    private val createdAt: LocalDateTime? = null
)

@Repository
interface UserJdbcRepository : CrudRepository<User, Long>