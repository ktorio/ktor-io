package io.ktor.io

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@OptIn(DelicateCoroutinesApi::class)
public actual fun testSuspend(
    context: CoroutineContext,
    timeoutMillis: Long,
    block: suspend CoroutineScope.() -> Unit
): dynamic = GlobalScope.promise(block = {
    withTimeout(timeoutMillis, block)
}, context = context)
