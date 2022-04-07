package io.ktor.io

public class BufferedBytesDestination(
    private val delegate: BytesDestination,
    bufferSize: Int = DEFAULT_BUFFER_SIZE
) : BytesDestination() {

    private val buffer: Buffer

    init {
        buffer = if (bufferSize == DEFAULT_BUFFER_SIZE) {
            ByteArrayBufferPool.Default.borrow()
        } else {
            ByteArrayBuffer(bufferSize)
        }
    }

    override val closedCause: Throwable?
        get() = delegate.closedCause

    override fun canWrite(): Boolean = delegate.canWrite()

    override fun write(buffer: Buffer) {
        closedCause?.let { throw it }

        this.buffer.write(buffer)
    }

    override suspend fun flush() {
        closedCause?.let { throw it }

        while (buffer.canRead()) {
            delegate.write(buffer)
            delegate.awaitFreeSpace()
        }

        buffer.reset()
        delegate.flush()
    }

    override suspend fun awaitFreeSpace() {
        closedCause?.let { throw it }

        while (!buffer.canWrite()) {
            delegate.awaitFreeSpace()
            delegate.write(buffer)
            buffer.compact()
        }
    }

    override fun close(cause: Throwable?) {
        buffer.release()
        delegate.close(cause)
    }

    override fun close() {
        delegate.close()
    }

    public suspend fun writeByte(value: Byte) {
        closedCause?.let { throw it }

        if (buffer.canWrite()) {
            buffer.writeByte(value)
        } else {
            awaitFreeSpace()
            buffer.writeByte(value)
        }

        flushIfFull()
    }

    public suspend fun writeShort(value: Short) {
        closedCause?.let { throw it }

        if (buffer.writeCapacity() >= 2) {
            buffer.writeShort(value)
        } else {
            writeByte(value.highByte)
            writeByte(value.lowByte)
        }

        flushIfFull()
    }

    public suspend fun writeInt(value: Int) {
        closedCause?.let { throw it }

        if (buffer.writeCapacity() >= 4) {
            buffer.writeInt(value)
        } else {
            writeShort(value.highShort)
            writeShort(value.lowShort)
        }

        flushIfFull()
    }

    public suspend fun writeLong(value: Long) {
        closedCause?.let { throw it }

        if (buffer.writeCapacity() >= 8) {
            buffer.writeLong(value)
        } else {
            writeInt(value.highInt)
            writeInt(value.lowInt)
        }

        flushIfFull()
    }

    private suspend fun flushIfFull() {
        if (!buffer.canWrite()) {
            flush()
        }
    }
}