package io.ktor.io

import java.lang.Integer.min
import java.nio.ByteBuffer

/**
 * The [Buffer] implementation using [ByteBuffer] class on the JVM.
 */
public class JvmBuffer(
    buffer: ByteBuffer,
    private val pool: ObjectPool<ByteBuffer>
) : Buffer() {

    /**
     * Creates a new [JvmBuffer] instance with the [ByteBuffer] instance from the [pool].
     */
    public constructor(pool: ObjectPool<ByteBuffer> = DirectByteBufferPool.Default) : this(pool.borrow(), pool)

    /**
     * Provides access to the underlying [ByteBuffer].
     *
     * The [buffer.position] reflects [readIndex] and [buffer.limit] reflects [writeIndex].
     *
     * All modifications of the [ByteBuffer] is reflected by the [JvmBuffer] itself.
     */
    @Suppress("CanBePrimaryConstructorProperty")
    public var buffer: ByteBuffer = buffer

    override var readIndex: Int
        get() = buffer.position()
        set(value) {
            buffer.position(value)
        }

    override var writeIndex: Int
        get() = buffer.limit()
        set(value) {
            buffer.limit(value)
        }

    override val capacity: Int
        get() = buffer.capacity()

    override fun copyToArray(destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startIndex should be non-negative: $startIndex" }
        require(startIndex <= endIndex) { "startIndex should be less than or equal to endIndex: $startIndex, $endIndex" }
        require(endIndex <= destination.size) { "endIndex should be less than or equal to destination.size: $endIndex, ${destination.size}" }

        val count = min(endIndex - startIndex, buffer.remaining())
        buffer.get(destination, startIndex, count)
        return count
    }

    /**
     * Return the underlying buffer to the pool.
     */
    override fun close() {
        pool.recycle(buffer)
    }

    override fun compact() {
        buffer.compact()
    }

    override fun loadByteAt(index: Int): Byte = buffer.get(index)

    override fun loadShortAt(index: Int): Short = buffer.getShort(index)

    override fun loadIntAt(index: Int): Int = buffer.getInt(index)

    override fun loadLongAt(index: Int): Long = buffer.getLong(index)

    override fun storeByteAt(index: Int, value: Byte) {
        randomAccess {
            it.put(index, value)
        }
    }

    override fun storeShortAt(index: Int, value: Short) {
        randomAccess {
            it.putShort(index, value)
        }
    }

    override fun storeIntAt(index: Int, value: Int) {
        randomAccess {
            it.putInt(index, value)
        }
    }

    override fun storeLongAt(index: Int, value: Long) {
        randomAccess {
            it.putLong(index, value)
        }
    }

    override fun storeBufferAt(index: Int, value: Buffer): Int {
        var current = index
        while (value.canRead) {
            storeByteAt(current++, value.readByte())
        }

        return current - index
    }

    override fun storeArrayAt(index: Int, value: ByteArray, startPosition: Int, endPosition: Int): Int {
        check(index < capacity) { "Index should be less than capacity: $index, $capacity" }
        check(startPosition >= 0) { "startPosition should be non-negative: $startPosition" }
        check(startPosition <= endPosition) { "startPosition should be less than or equal to endPosition: $startPosition, $endPosition" }
        check(endPosition <= value.size) { "endPosition should be less than or equal to value.size: $endPosition, ${value.size}" }


        val count = min(endPosition - startPosition, capacity - index)

        randomAccess {
            it.position(index)
            buffer.put(value, startPosition, count)
        }

        return count
    }

    override fun readArray(): ByteArray {
        val result = ByteArray(buffer.remaining())
        copyToArray(result, 0, result.size)
        return result
    }

    private fun randomAccess(block: (ByteBuffer) -> Unit) {
        val oldPosition = buffer.position()
        val oldLimit = buffer.limit()
        try {
            buffer.position(0)
            buffer.limit(capacity)
            block(buffer)
        } finally {
            buffer.position(oldPosition)
            buffer.limit(oldLimit)
        }
    }
}

