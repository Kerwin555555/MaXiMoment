package com.moment.app.localimages.logic

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

object ThreadTackler {
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private var CORE_POOL_SIZE = 0
    private var MAXIMUM_POOL_SIZE = 0
    private const val KEEP_ALIVE = 1L
    private var executor: ExecutorService? = null
    private fun create(): ThreadPoolExecutor {
        return PriorityExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
            LinkedBlockingDeque(), OKThreadFactory(Thread.NORM_PRIORITY)
        )
    }

    init {
        CORE_POOL_SIZE = CPU_COUNT + 1
        MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
    }

    fun push(task: Runnable?) {
        if (executor == null) {
            executor = create()
        }
        executor?.execute(task)
    }

    private class OKThreadFactory constructor(private val threadPriority: Int) :
        ThreadFactory {
        private val threadNumber = AtomicInteger(1)
        private val name = "OK-Thread"
        override fun newThread(r: Runnable): Thread {
            val thread = Thread(r, name + threadNumber.getAndIncrement())
            thread.priority = threadPriority
            return thread
        }
    }

    private class PriorityExecutor internal constructor(
        corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit?,
        workQueue: BlockingQueue<Runnable?>?, threadFactory: ThreadFactory?
    ) : ThreadPoolExecutor(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        threadFactory
    )
}