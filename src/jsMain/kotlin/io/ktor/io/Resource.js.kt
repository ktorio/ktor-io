package io.ktor.io

import kotlinx.atomicfu.*

public actual fun interface Runnable {
    public actual fun run()
}

private external class FinalizationRegistry(cleanup: (CleanupAction) -> Unit) {
    fun register(obj: Any, handle: CleanupAction)
    fun unregister(obj: Any)
}

private val registry = FinalizationRegistry(CleanupAction::clean)

internal actual abstract class AutoCloseable actual constructor(cleanup: Runnable) : Closeable {
    private val action = CleanupAction(cleanup)

    init {
        registry.register(this, action)
    }

    actual final override fun close() {
        registry.unregister(this)
        beforeManualClose()
        action.clean()
    }

    actual open fun beforeManualClose() {}
}

private class CleanupAction(private val cleanup: Runnable) {
    private val executed = atomic(false)
    fun clean() {
        if (executed.compareAndSet(false, true)) cleanup.run()
    }
}
