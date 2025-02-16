package com.github.nopecho.concurrent

import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

val Dispatchers.VIRTUAL: CoroutineDispatcher
    get() = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

object ConcurrentUtils {

    fun run(callCount: Int = 200, dispatcher: CoroutineDispatcher = Dispatchers.Default, task: () -> Unit) {
        val startSignal = CompletableDeferred<Unit>()
        runBlocking(dispatcher) {
            val jobs = (1..callCount).map {
                async {
                    try {
                        startSignal.await()
                        task()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
            startSignal.complete(Unit)
            jobs.awaitAll()
        }
    }

    fun runThread(threadCount: Int = 200, task: () -> Unit) {
        val (executor, startLatch, doneLatch) = setup(threadCount)
        repeat(threadCount) {
            executor.submit {
                try {
                    startLatch.await()
                    task()
                } catch (e: Throwable) {
                    e.printStackTrace()
                } finally {
                    doneLatch.countDown()
                }
            }
        }
        release(executor, startLatch, doneLatch)
    }

    private fun release(
        executor: ExecutorService,
        startLatch: CountDownLatch,
        doneLatch: CountDownLatch,
    ) {
        startLatch.countDown()
        doneLatch.await()
        executor.shutdown()
    }

    private fun setup(threadCount: Int): Triple<ExecutorService, CountDownLatch, CountDownLatch> {
        // 가상 스레드 풀
        val executor = Executors.newVirtualThreadPerTaskExecutor()
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        return Triple(executor, startLatch, doneLatch)
    }
}