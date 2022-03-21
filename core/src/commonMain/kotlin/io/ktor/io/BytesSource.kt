package io.ktor.io

public abstract class BytesSource {
    public abstract val closeCause: Throwable?
    public abstract fun canRead(): Boolean

    public abstract fun read(): Buffer
    public abstract suspend fun awaitContent()

    public abstract fun cancel(cause: Throwable = IOException("FooBar"))
}