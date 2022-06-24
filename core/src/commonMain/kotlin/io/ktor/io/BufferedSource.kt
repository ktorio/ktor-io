package io.ktor.io

public class BufferedSource(
    private val delegate: Source
) : Source() {

    private var buffer: Buffer

    init {
        buffer = delegate.read()
    }

    override val closedCause: Throwable?
        get() = delegate.closedCause

    override fun read(): Buffer {
        closedCause?.let { throw it }

        if (buffer.canRead()) {
            return buffer
        }

        buffer.release()
        buffer = delegate.read()
        return buffer
    }

    override suspend fun awaitContent(): Boolean {
        closedCause?.let { throw it }

        if (buffer.canRead()) return true
        return delegate.awaitContent()
    }

    override fun cancel(cause: Throwable) {
        delegate.cancel(cause)
    }

    public suspend fun readByte(): Byte {
        closedCause?.let { throw it }

        while (!buffer.canRead()) {
            awaitContent()
            read()
        }
        return buffer.readByte()
    }

    public suspend fun readShort(): Short {
        closedCause?.let { throw it }

        if (buffer.readCapacity() >= 2) {
            return buffer.readShort()
        }
        return Short(readByte(), readByte())
    }

    public suspend fun readInt(): Int {
        closedCause?.let { throw it }

        if (buffer.readCapacity() >= 4) {
            return buffer.readInt()
        }
        return Int(readShort(), readShort())
    }

    public suspend fun readLong(): Long {
        closedCause?.let { throw it }

        if (buffer.readCapacity() >= 8) {
            return buffer.readLong()
        }
        return Long(readInt(), readInt())
    }
}
