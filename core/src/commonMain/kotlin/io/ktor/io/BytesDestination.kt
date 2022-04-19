package io.ktor.io

public abstract class BytesDestination : Closeable {
    public abstract val closedCause: Throwable?
    public abstract fun canWrite(): Boolean

    public abstract fun write(buffer: Buffer)
    public abstract suspend fun flush()
    public abstract suspend fun awaitFreeSpace()

    public abstract fun close(cause: Throwable? = null)
}
