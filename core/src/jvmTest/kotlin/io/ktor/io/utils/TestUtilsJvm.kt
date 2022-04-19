package io.ktor.io.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.CoroutineContext

actual fun testSuspend(
    context: CoroutineContext,
    timeoutMillis: Long,
    block: suspend CoroutineScope.() -> Unit
): Unit = runBlocking(context) {
    withTimeout(timeoutMillis, block)
}
