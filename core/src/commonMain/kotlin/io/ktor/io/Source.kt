package io.ktor.io

public abstract class Source {
    public abstract val cancelCause: Throwable?

    public abstract fun read(): Buffer
    public abstract suspend fun awaitContent(): Boolean

    public abstract fun cancel(cause: Throwable = IOException("FooBar"))
}
