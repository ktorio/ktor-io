package io.ktor.io

class TestSource(private vararg val buffers: ByteArrayBuffer) : Source() {
    internal var readCount = 0
    override val cancelCause: Throwable? = null
    override fun read(): Buffer = buffers[readCount++]
    override suspend fun awaitContent(): Boolean = readCount < buffers.size
    override fun cancel(cause: Throwable) {}
}
