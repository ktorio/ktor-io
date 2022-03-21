package io.ktor.io

import io.ktor.io.utils.*

public class BufferedBytesSource(
    private val delegate: BytesSource
) : BytesSource() {

    private lateinit var buffer: Buffer

    override val closeCause: Throwable?
        get() = delegate.closeCause

    override fun canRead(): Boolean {
        return (::buffer.isInitialized && buffer.canRead()) || delegate.canRead()
    }

    override fun read(): Buffer {
        if (::buffer.isInitialized && buffer.canRead()) {
            return buffer
        }

        if (::buffer.isInitialized) {
            buffer.release()
        }
        buffer = delegate.read()
        return buffer
    }

    override suspend fun awaitContent() {
        if (::buffer.isInitialized && buffer.canRead()) {
            return
        }
        delegate.awaitContent()
    }

    override fun cancel(cause: Throwable) {
        delegate.cancel(cause)
    }

    public suspend fun readByte(): Byte {
        while (!::buffer.isInitialized || !buffer.canRead()) {
            awaitContent()
            read()
        }
        return buffer.readByte()
    }

    public suspend fun readShort(): Short {
        if (::buffer.isInitialized && buffer.readCapacity() >= 2) {
            return buffer.readShort()
        }
        return readByte().asHighByte(readByte())
    }

    public suspend fun readInt(): Int {
        if (::buffer.isInitialized && buffer.readCapacity() >= 4) {
            return buffer.readInt()
        }
        return readShort().asHughShort(readShort())
    }

    public suspend fun readLong(): Long {
        if (::buffer.isInitialized && buffer.readCapacity() >= 8) {
            return buffer.readLong()
        }
        return readInt().asHighInt(readInt())
    }
}