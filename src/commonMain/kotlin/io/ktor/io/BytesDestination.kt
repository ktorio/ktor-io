package io.ktor.io

public abstract class BytesDestination : Closeable {
    public abstract val closeCause: Throwable?

    public abstract fun write(buffer: Buffer)
    public abstract suspend fun flush()

    public abstract fun close(cause: Throwable? = null)
}