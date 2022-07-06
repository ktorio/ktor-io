package io.ktor.io

import java.lang.Integer.min
import java.nio.ByteBuffer

/**
 * The [Buffer] implementation using [ByteBuffer] class on the JVM.
 *
 * @constructor creates buffer prepared for reading.
 */
public class JvmBuffer(
    buffer: ByteBuffer,
    private val pool: ObjectPool<ByteBuffer> = ByteBufferPool.Default
) : Buffer {

    /**
     * Creates a new [JvmBuffer] instance with the [ByteBuffer] instance from the [pool].
     *
     * The buffer is empty and prepared for write operations.
     */
    public constructor(capacity: Int) : this(
        ByteBuffer.allocateDirect(capacity).limit(0),
        ByteBufferPool.NoPool
    )

    /**
     * Creates a new [JvmBuffer] instance with the [ByteBuffer] instance from the [pool].
     *
     * The buffer is empty and prepared for write operations.
     */
    public constructor(pool: ObjectPool<ByteBuffer> = ByteBufferPool.Default) : this(pool.borrow(), pool)

    /**
     * Provides access to the underlying [ByteBuffer].
     *
     * The [buffer.position] reflects [readIndex] and [buffer.limit] reflects [writeIndex].
     *
     * All modifications of the [ByteBuffer] is reflected by the [JvmBuffer] itself.
     */
    @Suppress("CanBePrimaryConstructorProperty")
    public var buffer: ByteBuffer = buffer
        private set

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

    override fun readToByteArrayAt(index: Int, destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startIndex should be non-negative: $startIndex" }
        require(startIndex <= endIndex) { "startIndex should be less than or equal to endIndex: $startIndex, $endIndex" }
        require(endIndex <= destination.size) { "endIndex should be less than or equal to destination.size: $endIndex, ${destination.size}" }
        require(index < capacity) { "index should be less than capacity: $index, $capacity" }

        val count = min(endIndex - startIndex, capacity - index)
        randomAccess {
            it.position(index)
            buffer.get(destination, startIndex, count)
        }

        return count
    }

    override fun readToByteArray(destination: ByteArray, startIndex: Int, endIndex: Int): Int {
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

    override fun getByteAt(index: Int): Byte = buffer.get(index)

    override fun getShortAt(index: Int): Short = buffer.getShort(index)

    override fun getIntAt(index: Int): Int = buffer.getInt(index)

    override fun getLongAt(index: Int): Long = buffer.getLong(index)

    override fun setByteAt(index: Int, value: Byte) {
        randomAccess {
            it.put(index, value)
        }
    }

    override fun setShortAt(index: Int, value: Short) {
        randomAccess {
            it.putShort(index, value)
        }
    }

    override fun setIntAt(index: Int, value: Int) {
        randomAccess {
            it.putInt(index, value)
        }
    }

    override fun setLongAt(index: Int, value: Long) {
        randomAccess {
            it.putLong(index, value)
        }
    }

    override fun writeBufferAt(index: Int, value: Buffer): Int {
        var current = index
        while (value.isNotEmpty) {
            setByteAt(current++, value.readByte())
        }

        return current - index
    }

    override fun writeByteArrayAt(index: Int, value: ByteArray, startPosition: Int, endPosition: Int): Int {
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

