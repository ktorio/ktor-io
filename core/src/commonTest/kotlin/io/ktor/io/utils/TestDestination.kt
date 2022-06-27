package io.ktor.io.utils

import io.ktor.io.*

class TestDestination : Destination() {
    internal val buffers = mutableListOf<Buffer>()
    internal val writeCount: Int get() = buffers.size

    override var closedCause: Throwable? = null

    override fun write(buffer: Buffer): Int {
        val copy = ByteArrayBuffer(buffer.availableForRead)
        val array = ByteArray(buffer.availableForRead)
        buffer.read(array)
        copy.write(array)
        buffers.add(copy)
        buffer.readIndex = buffer.writeIndex
        return array.size
    }

    override suspend fun flush() {}
    override suspend fun awaitFreeSpace() {}

    override fun close(cause: Throwable?) {
        closedCause = cause ?: ClosedDestinationException()
    }

    override fun close() {
        close(null)
    }

    class ClosedDestinationException() : IOException("closed")
}
