# Kotlin Functional AOP

이 프로젝트는 Kotlin의 함수형 스타일을 사용하여 Spring의 AOP 기능을 대체하기 위한 모듈들을 제공합니다.
Spring AOP의 주요 기능을 코틀린 기반의 함수형 방식으로 구현함으로써, 코드의 가독성과 재사용성을 높이고, 비즈니스 로직과 기술적인 관심사를 분리합니다.

## Modules

* `kotlin-func-aop-transaction`: 트랜잭션 관련 기능을 지원하는 모듈입니다.
* `kotlin-func-aop-cache`: 캐시 관련 기능을 지원하는 모듈입니다.
* `kotlin-func-aop-lock`: 락 관련 기능을 지원하는 모듈입니다.
* `kotlin-func-aop-support`: -

### kotlin-func-aop-transaction

[TransactionFunc](https://github.com/nopecho/kotlin-functional-aop/blob/main/kotlin-func-aop-transaction/src/main/kotlin/com/github/nopecho/funcaop/tx/TransactionFunc.kt)
클래스는 트랜잭션 블록을 실행시키는 정적 메소드를 제공합니다.
빈 초기화 시점에 정적 필드에 `TransactionAdvice`를 주입받아 사용합니다.

#### Example

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