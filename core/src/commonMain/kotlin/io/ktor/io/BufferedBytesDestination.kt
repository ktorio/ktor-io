package io.ktor.io

import io.ktor.io.impl.*
import io.ktor.io.utils.*
import kotlin.math.min

public class BufferedBytesDestination(
    private val delegate: BytesDestination, bufferSize: Int = 8 * 1024
) : BytesDestination() {

    private val buffer = ByteArrayBuffer(bufferSize)

    override val closeCause: Throwable?
        get() = delegate.closeCause

    override fun canWrite(): Boolean = delegate.canWrite()

    override fun write(buffer: Buffer) {
        val read = min(this.buffer.writeCapacity(), buffer.readCapacity())
        repeat(read) {
            this.buffer.writeByte(buffer.readByte())
        }
    }

    override suspend fun flush() {
        while (buffer.canRead()) {
            delegate.write(buffer)
            delegate.awaitFreeSpace()
        }
        buffer.readIndex = 0
        buffer.writeIndex = 0
        delegate.flush()
    }

    override suspend fun awaitFreeSpace() {
        if (buffer.canWrite()) {
            return
        }
        flush()
        delegate.awaitFreeSpace()
    }

    override fun close(cause: Throwable?) {
        delegate.close(cause)
    }

    override fun close() {
        delegate.close()
    }

    public suspend fun writeByte(value: Byte) {
        if (!buffer.canWrite()) awaitFreeSpace()
        buffer.writeByte(value)
        flushIfFull()
    }

    public suspend fun writeShort(value: Short) {
        awaitFreeSpace()
        if (buffer.writeCapacity() >= 2) {
            buffer.writeShort(value)
            flushIfFull()
            return
        }

        writeByte(value.highByte)
        writeByte(value.lowByte)
    }

    public suspend fun writeInt(value: Int) {
        awaitFreeSpace()
        if (buffer.writeCapacity() >= 4) {
            buffer.writeInt(value)
            flushIfFull()
            return
        }

        writeShort(value.highShort)
        writeShort(value.lowShort)
    }

    public suspend fun writeLong(value: Long) {
        awaitFreeSpace()
        if (buffer.writeCapacity() >= 8) {
            buffer.writeLong(value)
            flushIfFull()
            return
        }

        writeInt(value.highInt)
        writeInt(value.lowInt)
    }

    private suspend fun flushIfFull() {
        if (!buffer.canWrite()) {
            flush()
        }
    }
}
