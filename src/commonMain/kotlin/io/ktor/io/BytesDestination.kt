package io.ktor.io

abstract class BytesDestination : Closeable {
    abstract val closeCause: Throwable?

    abstract fun write(buffer: Buffer)
    abstract suspend fun flush()

    abstract fun close(cause: Throwable? = null)
}