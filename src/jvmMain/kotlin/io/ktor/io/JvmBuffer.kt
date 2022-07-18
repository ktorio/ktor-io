package io.ktor.io

import io.ktor.io.internals.*
import java.nio.ByteBuffer
import kotlin.math.*

/**
 * The [Buffer] implementation using [ByteBuffer] class on the JVM.
 *
 * @constructor creates buffer prepared for reading.
 */
public class JvmBuffer internal constructor(
    view: ByteBuffer,
    private val referenceCounter: ReferenceCounter,
    private val pool: ObjectPool<JvmBuffer>,
    private val origin: JvmBuffer? = null
) : Buffer {

    public constructor(
        buffer: ByteBuffer,
        pool: ObjectPool<JvmBuffer> = JvmBufferPool.Default
    ) : this(buffer, AtomicReferenceCounter(), pool)

    /**
     * Creates a new [JvmBuffer] instance with the [ByteBuffer] instance from the [pool].
     *
     * The buffer is empty and prepared for write operations.
     */
    public constructor(capacity: Int = DEFAULT_BUFFER_SIZE) : this(
        ByteBuffer.allocateDirect(capacity).limit(0),
        referenceCounter = EmptyReferenceCounter,
        pool = JvmBufferPool.Empty
    )

    init {
        referenceCounter.retain()
    }

    /**
     * Provides access to the underlying [ByteBuffer].
     *
     * The [buffer.position()] reflects [readIndex] and [buffer.limit()] reflects [writeIndex].
     *
     * All modifications of the [ByteBuffer] is reflected by the [JvmBuffer] itself.
     */
    @Suppress("CanBePrimaryConstructorProperty")
    public var raw: ByteBuffer = view
        private set

    override var readIndex: Int
        get() = raw.position()
        set(value) {
            raw.position(value)
        }

    override var writeIndex: Int
        get() = raw.limit()
        set(value) {
            raw.limit(value)
        }

    override val capacity: Int
        get() = raw.capacity()

    override fun copyToByteArrayAt(index: Int, destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startIndex should be non-negative: $startIndex" }
        require(startIndex <= endIndex) { "startIndex should be less than or equal to endIndex: $startIndex, $endIndex" }
        require(endIndex <= destination.size) { "endIndex should be less than or equal to destination.size: $endIndex, ${destination.size}" }
        require(index < capacity) { "index should be less than capacity: $index, $capacity" }

        val count = min(endIndex - startIndex, capacity - index)
        randomAccess {
            it.position(index)
            raw.get(destination, startIndex, count)
        }

        return count
    }

    override fun copyToByteArray(destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startIndex should be non-negative: $startIndex" }
        require(startIndex <= endIndex) { "startIndex should be less than or equal to endIndex: $startIndex, $endIndex" }
        require(endIndex <= destination.size) { "endIndex should be less than or equal to destination.size: $endIndex, ${destination.size}" }

        val count = min(endIndex - startIndex, raw.remaining())
        raw.get(destination, startIndex, count)
        return count
    }

    /**
     * Return the underlying buffer to the pool.
     */
    override fun close() {
        if (referenceCounter.release()) {
            pool.recycle(origin ?: this)
        }
    }

    override fun compact() {
        raw.compact()
    }

    override fun getByteAt(index: Int): Byte = raw.get(index)

    override fun getShortAt(index: Int): Short = raw.getShort(index)

    override fun getIntAt(index: Int): Int = raw.getInt(index)

    override fun getLongAt(index: Int): Long = raw.getLong(index)

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

    override fun takeHead(index: Int): Buffer {
        require(index >= 0) { "index should be non-negative: $index" }
        val oldRead = readIndex
        val oldWrite = writeIndex

        raw.position(0)
        raw.limit(index)

        val head = JvmBuffer(raw.slice(), referenceCounter, pool, this).apply {
            readIndex = min(capacity, oldRead)
            writeIndex = min(capacity, oldWrite)
        }

        raw.position(index)
        raw.limit(raw.capacity())
        raw = raw.slice()

        readIndex = max(0, oldRead - index)
        writeIndex = max(0, oldWrite - index)

        return head
    }

    override fun copyFromBufferAt(index: Int, value: Buffer): Int {
        var current = index
        while (value.isNotEmpty) {
            setByteAt(current++, value.readByte())
        }

        return current - index
    }

    override fun copyFromByteArrayAt(index: Int, value: ByteArray, startIndex: Int, endIndex: Int): Int {
        check(index < capacity) { "Index should be less than capacity: $index, $capacity" }
        check(startIndex >= 0) { "startPosition should be non-negative: $startIndex" }
        check(startIndex <= endIndex) { "startPosition should be less than or equal to endPosition: $startIndex, $endIndex" }
        check(endIndex <= value.size) { "endPosition should be less than or equal to value.size: $endIndex, ${value.size}" }

        val count = min(endIndex - startIndex, capacity - index)

        randomAccess {
            it.position(index)
            raw.put(value, startIndex, count)
        }

        return count
    }

    private fun randomAccess(block: (ByteBuffer) -> Unit) {
        val oldPosition = raw.position()
        val oldLimit = raw.limit()
        try {
            raw.position(0)
            raw.limit(capacity)
            block(raw)
        } finally {
            raw.position(oldPosition)
            raw.limit(oldLimit)
        }
    }
}
