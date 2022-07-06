package io.ktor.io

/**
 * The abstraction on top of [RawSource] with easy to use reading API.
 */
public class Source(
    private val delegate: RawSource
) : RawSource {
    private var buffer = CompositeBuffer()

    override fun receive(): Buffer? {
        pullBuffer()?.let { return it }
        return delegate.receive()
    }

    override fun tryReceive(): Buffer? {
        pullBuffer()?.let { return it }
        return delegate.tryReceive()
    }

    override suspend fun awaitBuffer(): Boolean {
        if (buffer.isNotEmpty) return true
        return delegate.awaitBuffer()
    }

    override fun cancel(cause: Throwable) {
        delegate.cancel(cause)
    }

    public fun peek(): Buffer = buffer

    public fun peekBoolean(): Boolean {
        TODO()
    }

    public fun peekByte(): Byte {
        TODO()
    }

    public fun peekShort(): Short {
        TODO()
    }

    public fun peekInt(): Int {
        TODO()
    }

    public fun peekLong(): Long {
        TODO()
    }

    public fun peekFloat(): Float {
        TODO()
    }

    public fun peekDouble(): Double {
        TODO()
    }

    public fun readArray(): ByteArray {
        TODO()
    }

    public fun readBuffer(): Buffer {
        TODO()
    }

    public fun readBoolean(): Boolean {
        TODO()
    }

    public fun readByte(): Byte {
        TODO()
    }
    public fun readShort(): Short {
        TODO()
    }

    public fun readInt(): Int {
        TODO()
    }

    public fun readLong(): Long {
        TODO()
    }

    public fun readFloat(): Float {
        TODO()
    }

    public fun readDouble(): Double {
        TODO()
    }

    public fun requestBlocking(count: Int) {
        TODO()
    }

    public fun requestSuspend(count: Int) {
        TODO()
    }
}
