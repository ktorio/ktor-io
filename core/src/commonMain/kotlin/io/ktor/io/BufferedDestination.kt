package io.ktor.io

public class BufferedDestination(
    private val delegate: Destination,
    bufferSize: Int = DEFAULT_BUFFER_SIZE
) : Destination() {

    private val buffer: Buffer = if (bufferSize == DEFAULT_BUFFER_SIZE) {
        ByteArrayBufferPool.Default.borrow()
    } else {
        ByteArrayBuffer(bufferSize)
    }

    override val closedCause: Throwable?
        get() = delegate.closedCause

    override fun write(data: Buffer): Int {
        closedCause?.let { throw it }

        return buffer.write(data)
    }

    override suspend fun flush() {
        closedCause?.let { throw it }

        while (buffer.isNotEmpty) {
            delegate.write(buffer)
            delegate.awaitFreeSpace()
        }

        buffer.reset()
        delegate.flush()
    }

    override suspend fun awaitFreeSpace() {
        closedCause?.let { throw it }

        while (!buffer.isNotEmpty) {
            delegate.awaitFreeSpace()
            delegate.write(buffer)
            buffer.compact()
        }
    }

    override fun close(cause: Throwable?) {
        buffer.close()
        delegate.close(cause)
    }

    override fun close() {
        delegate.close()
    }

    public suspend fun writeByte(value: Byte) {
        closedCause?.let { throw it }

        awaitFreeSpace()
        buffer.writeByte(value)

        flushIfFull()
    }

    public suspend fun writeBoolean(value: Boolean) {
        closedCause?.let { throw it }

        awaitFreeSpace()
        buffer.writeBoolean(value)

        flushIfFull()
    }

    public suspend fun writeShort(value: Short) {
        closedCause?.let { throw it }

        if (buffer.availableForWrite >= 2) {
            buffer.writeShort(value)
        } else {
            writeByte(value.highByte)
            writeByte(value.lowByte)
        }

        flushIfFull()
    }

    public suspend fun writeInt(value: Int) {
        closedCause?.let { throw it }

        if (buffer.availableForWrite >= 4) {
            buffer.writeInt(value)
        } else {
            writeShort(value.highShort)
            writeShort(value.lowShort)
        }

        flushIfFull()
    }

    public suspend fun writeFloat(value: Float) {
        writeInt(value.toRawBits())
    }

    public suspend fun writeLong(value: Long) {
        closedCause?.let { throw it }

        if (buffer.availableForWrite >= 8) {
            buffer.writeLong(value)
        } else {
            writeInt(value.highInt)
            writeInt(value.lowInt)
        }

        flushIfFull()
    }

    public suspend fun writeDouble(value: Double) {
        writeLong(value.toRawBits())
    }

    private suspend fun flushIfFull() {
        if (!buffer.isFull) flush()
    }
}
