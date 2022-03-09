package io.ktor.io

abstract class BytesSource {
    abstract val closeCause: Throwable?

    abstract fun read(): Buffer
    abstract suspend fun await()

    abstract fun cancel(cause: Throwable = IOException("FooBar"))
}