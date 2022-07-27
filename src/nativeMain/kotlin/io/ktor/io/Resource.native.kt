package io.ktor.io

import kotlinx.atomicfu.*
import kotlin.native.internal.*

public actual fun interface Runnable {
    public actual fun run()
}

internal actual abstract class AutoCloseable actual constructor(cleanup: Runnable) : Closeable {
    private val action = CleanupAction(cleanup)

    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(action, action)

    actual final override fun close() {
        beforeManualClose()
        action(action)
    }

    actual open fun beforeManualClose() {}
}

private class CleanupAction(private val cleanup: Runnable) : (CleanupAction) -> Unit {
    private val executed = atomic(false)
    override fun invoke(action: CleanupAction) {
        if (executed.compareAndSet(false, true)) cleanup.run()
    }
}
