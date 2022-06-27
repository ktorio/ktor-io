package io.ktor.io

public class BufferedSource(
    private val delegate: Source
) : Source(), SourceReader {
    private var buffer: Buffer = delegate.read()

    override val cancelCause: Throwable?
        get() = delegate.cancelCause

    override fun read(): Buffer {
        checkCancelled()

        val result = buffer
        buffer = delegate.read()
        return result
    }

    public fun peek(): Buffer {
        checkCancelled()
        return buffer
    }

    override suspend fun awaitContent(): Boolean {
        checkCancelled()

        if (buffer.isNotEmpty) return true
        val result = delegate.awaitContent()
        buffer = delegate.read()
        return result
    }

    public override suspend fun peekByte(): Byte {
        checkCancelled()

        awaitContent()
        return buffer.readByte()
    }

    public override suspend fun peekShort(): Short {
        checkCancelled()

        if (buffer.availableForRead >= 2) return buffer.readShort()
        return Short(readByte(), readByte())
    }

    public override suspend fun peekInt(): Int {
        checkCancelled()

        if (buffer.availableForRead >= 4) return buffer.readInt()
        return Int(readShort(), readShort())
    }

    public override suspend fun peekLong(): Long {
        checkCancelled()

        if (buffer.availableForRead >= 8) {
            return buffer.readLong()
        }
        return Long(readInt(), readInt())
    }

    public override suspend fun discard(count: Int) {
        var remaining = count
        while (remaining > buffer.availableForRead) {
            remaining -= buffer.availableForRead

            buffer.close()
            buffer = delegate.read()
        }

        buffer.readIndex += remaining
    }

    override fun cancel(cause: Throwable) {
        buffer.close()
        delegate.cancel(cause)
    }

    private fun checkCancelled() {
        cancelCause?.let { throw it }
    }
}
