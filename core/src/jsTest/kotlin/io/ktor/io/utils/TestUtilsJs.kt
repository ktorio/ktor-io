package io.ktor.io.utils

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@OptIn(DelicateCoroutinesApi::class)
actual fun testSuspend(
    context: CoroutineContext,
    timeoutMillis: Long,
    block: suspend CoroutineScope.() -> Unit
): dynamic = GlobalScope.promise(block = {
    withTimeout(timeoutMillis, block)
}, context = context)
