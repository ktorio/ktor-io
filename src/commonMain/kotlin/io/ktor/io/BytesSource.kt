package io.ktor.io

public abstract class BytesSource {
    public abstract val closeCause: Throwable?

    public abstract fun read(): Buffer
    public abstract suspend fun await()

    public abstract fun cancel(cause: Throwable = IOException("FooBar"))
}