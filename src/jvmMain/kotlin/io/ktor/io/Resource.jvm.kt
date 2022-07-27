package io.ktor.io

import java.lang.ref.*

public actual typealias Runnable = java.lang.Runnable

private val cleaner = Cleaner.create()

internal actual abstract class AutoCloseable actual constructor(cleanup: Runnable) : Closeable {
    private val cleanable = cleaner.register(this, cleanup)
    actual final override fun close() {
        beforeManualClose()
        cleanable.clean()
    }

    actual open fun beforeManualClose() {}
}
