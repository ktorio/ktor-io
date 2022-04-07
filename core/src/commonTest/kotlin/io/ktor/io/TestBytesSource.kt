package io.ktor.io

class TestBytesSource(private vararg val buffers: ByteArrayBuffer) : BytesSource() {
    internal var readCount = 0
    override val closedCause: Throwable? = null
    override fun canRead() = readCount < buffers.size
    override fun read(): Buffer = buffers[readCount++]
    override suspend fun awaitContent() = Unit
    override fun cancel(cause: Throwable) = Unit
}
