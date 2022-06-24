package io.ktor.io

public class BufferedSource(
    private val delegate: Source
) : Source() {

    private var buffer: Buffer

    init {
        buffer = delegate.read()
    }

    override val cancelCause: Throwable?
        get() = delegate.cancelCause

    override fun read(): Buffer {
        cancelCause?.let { throw it }

        val result = buffer
        buffer = delegate.read()
        return result
    }

    public fun peek(): Buffer {
        cancelCause?.let { throw it }
        return buffer
    }

    override suspend fun awaitContent(): Boolean {
        cancelCause?.let { throw it }

        if (buffer.canRead()) return true
        return delegate.awaitContent()
    }

    override fun cancel(cause: Throwable) {
        delegate.cancel(cause)
    }

    public suspend fun readByte(): Byte {
        cancelCause?.let { throw it }

        while (!buffer.canRead()) {
            awaitContent()
            read()
        }
        return buffer.readByte()
    }

    public suspend fun readShort(): Short {
        cancelCause?.let { throw it }

        if (buffer.readCapacity() >= 2) {
            return buffer.readShort()
        }
        return Short(readByte(), readByte())
    }

    public suspend fun readInt(): Int {
        cancelCause?.let { throw it }

        if (buffer.readCapacity() >= 4) {
            return buffer.readInt()
        }
        return Int(readShort(), readShort())
    }

    public suspend fun readLong(): Long {
        cancelCause?.let { throw it }

        if (buffer.readCapacity() >= 8) {
            return buffer.readLong()
        }
        return Long(readInt(), readInt())
    }
}
