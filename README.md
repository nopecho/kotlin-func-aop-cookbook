# Kotlin Functional AOP

This project provides modules to replace Spring AOP features using Kotlin’s functional programming style.
By implementing core Spring AOP functionalities in a Kotlin-based functional approach, it aims to improve code
readability, reusability, and maintainability while separating business logic from technical concerns.

## Modules

* `kotlin-func-aop-transaction`: Supports transaction-related features.
* `kotlin-func-aop-cache`: Supports caching-related features.(TODO)
* `kotlin-func-aop-lock`: Supports locking-related features.(TODO)
* `kotlin-func-aop-support`: (Future expansion).

### kotlin-func-aop-transaction

[TransactionFunc](https://github.com/nopecho/kotlin-functional-aop/blob/main/kotlin-func-aop-transaction/src/main/kotlin/com/github/nopecho/funcaop/tx/TransactionFunc.kt)
class provides static methods to execute code blocks within transactional contexts.
During bean initialization, it injects `TransactionAdvice` into a static field for use.

### Example

```kotlin
@Service
class AnyService(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) {

    // Like @Transactional(readOnly = true)
    fun findUser(id: Long): User = TransactionFunc.execute(readOnly = true) {
        userRepository.findById(id).orElseThrow()
    }

    // Like @Transactional
    fun transactional(id: Long, email: String): User = TransactionFunc.execute {
        val user = userRepository.findById(id).orElseThrow()
        user.copy(email = email).also { userRepository.save(it) }
    }

    // Transaction rollback when an exception is thrown
    fun rollback(id: Long, email: String): User = TransactionFunc.execute {
        val user = userRepository.findById(id).orElseThrow()
        user.copy(email = email).also { userRepository.save(it) }
        throw IllegalStateException("Rollback Transaction!")
    }

    // Multiple transactions
    fun multiTransaction(userId: Long, postId: Long) {
        // Transaction 1
        TransactionFunc.execute {
            val user = userRepository.findById(userId).orElseThrow()
            userRepository.save(user.disable())
        }

        // Transaction 2
        TransactionFunc.execute {
            val post = postRepository.findById(postId).orElseThrow()
            postRepository.save(post.disable())
        }
    }
}
```

### kotlin-func-aop-cache

#### Example

```kotlin
```

### kotlin-func-aop-lock

#### Example

```kotlin
```