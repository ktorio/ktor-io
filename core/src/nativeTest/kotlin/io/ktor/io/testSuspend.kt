package io.ktor.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import platform.posix.usleep
import kotlin.coroutines.CoroutineContext
import kotlin.native.concurrent.FutureState
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.system.getTimeMillis
import kotlin.time.Duration.Companion.milliseconds

public actual fun testSuspend(
    context: CoroutineContext,
    timeoutMillis: Long,
    block: suspend CoroutineScope.() -> Unit
) {
    executeInWorker(timeoutMillis) {
        runBlocking {
            block()
        }
    }
}

private var TEST_WORKER: Worker = createTestWorker()
private val SLEEP_TIME: UInt = 10.milliseconds.inWholeMicroseconds.toUInt()

internal fun executeInWorker(timeout: Long, block: () -> Unit) {
    val result = TEST_WORKER.execute(TransferMode.UNSAFE, { block }) {
        it()
    }

    val endTime = getTimeMillis() + timeout
    while (result.state == FutureState.SCHEDULED && endTime > getTimeMillis()) {
        usleep(SLEEP_TIME)
    }

    when (result.state) {
        FutureState.SCHEDULED -> {
            TEST_WORKER.requestTermination(processScheduledJobs = false)
            TEST_WORKER = createTestWorker()
            error("Test is timed out")
        }
        else -> {
            result.consume { }
        }
    }
}

private fun createTestWorker(): Worker = Worker.start(
    name = "Ktor Test Worker",
    errorReporting = true
)
