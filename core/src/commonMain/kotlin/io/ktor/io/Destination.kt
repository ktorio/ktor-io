package io.ktor.io

public abstract class Destination : Closeable {
    public abstract val closedCause: Throwable?

    public abstract fun write(data: Buffer): Int
    public abstract suspend fun flush()
    public abstract suspend fun awaitFreeSpace()

    public abstract fun close(cause: Throwable? = null)
}
