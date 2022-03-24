package io.ktor.io

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public expect fun testSuspend(
    context: CoroutineContext = EmptyCoroutineContext,
    timeoutMillis: Long = 60L * 1000L,
    block: suspend CoroutineScope.() -> Unit
)
