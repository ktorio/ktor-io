package io.ktor.io

public class BufferedBytesSource(
    private val delegate: BytesSource
) : BytesSource() {

    private var buffer: Buffer

    init {
        buffer = delegate.read()
    }

    override val closedCause: Throwable?
        get() = delegate.closedCause

    override fun canRead(): Boolean {
        return delegate.canRead() || buffer.isNotEmpty
    }

    override fun read(): Buffer {
        closedCause?.let { throw it }

        if (buffer.isNotEmpty) {
            return buffer
        }

        buffer.close()
        buffer = delegate.read()
        return buffer
    }

    override suspend fun awaitContent() {
        closedCause?.let { throw it }

        if (buffer.isNotEmpty) return
        delegate.awaitContent()
    }

    override fun cancel(cause: Throwable) {
        delegate.cancel(cause)
    }

    public suspend fun readByte(): Byte {
        closedCause?.let { throw it }

        while (buffer.isEmpty) {
            awaitContent()
            read()
        }
        return buffer.readByte()
    }

    public suspend fun readShort(): Short {
        closedCause?.let { throw it }

        if (buffer.availableForRead >= 2) {
            return buffer.readShort()
        }
        return Short(readByte(), readByte())
    }

    public suspend fun readInt(): Int {
        closedCause?.let { throw it }

        if (buffer.availableForRead >= 4) {
            return buffer.readInt()
        }
        return Int(readShort(), readShort())
    }

    public suspend fun readLong(): Long {
        closedCause?.let { throw it }

        if (buffer.availableForRead >= 8) {
            return buffer.readLong()
        }
        return Long(readInt(), readInt())
    }
}
