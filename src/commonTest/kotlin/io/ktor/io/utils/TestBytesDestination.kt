package io.ktor.io.utils

import io.ktor.io.*

class TestBytesDestination : BytesDestination() {
    internal val buffers = mutableListOf<Buffer>()
    internal val writeCount: Int get() = buffers.size

    override var closedCause: Throwable? = null

    override fun canWrite(): Boolean = closedCause != null

    override fun write(buffer: Buffer): Int {
        val copy = ByteArrayBuffer(buffer.availableForRead)
        val array = ByteArray(buffer.availableForRead)
        buffer.copyToByteArray(array)
        copy.copyFromByteArray(array)
        buffers.add(copy)
        buffer.readIndex = buffer.writeIndex
        return array.size
    }

    override suspend fun flush() = Unit
    override suspend fun awaitFreeSpace() = Unit

    override fun close(cause: Throwable?) {
        closedCause = cause ?: ClosedDestinationException()
    }

    override fun close() {
        close(null)
    }

    class ClosedDestinationException : IOException("closed")
}
