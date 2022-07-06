package io.ktor.io

import kotlin.math.min

public const val DEFAULT_POOL_CAPACITY: Int = 2000
public const val DEFAULT_BUFFER_SIZE: Int = 1024 * 16

public class ByteArrayBuffer(
    array: ByteArray,
    readIndex: Int = 0,
    writeIndex: Int = array.size,
    /**
     * The pool used for allocation of the [array].
     */
    public val pool: ObjectPool<ByteArray> = ByteArrayPool.Empty
) : Buffer {

    /**
     * Creates buffer of fixed [capacity].
     */
    public constructor(capacity: Int) : this(ByteArray(capacity), readIndex = 0, writeIndex = 0)

    public constructor(pool: ObjectPool<ByteArray> = ByteArrayPool.Empty) : this(
        pool.borrow(),
        readIndex = 0,
        writeIndex = 0,
        pool = pool
    )

    /**
     * Provides access to underlying byte array.
     *
     * Please note, all changes of the array will be reflected in the buffer.
     */
    @Suppress("CanBePrimaryConstructorProperty")
    public var array: ByteArray = array
        private set

    override val capacity: Int
        get() = array.size

    override var readIndex: Int = readIndex
        set(value) {
            if (value < 0 || value > writeIndex) {
                throw IndexOutOfBoundsException("readIndex must be >= 0")
            }
            field = value
        }

    override var writeIndex: Int = writeIndex
        set(value) {
            if (value < 0 || value > capacity) {
                throw IndexOutOfBoundsException("Write index $value is out of bounds: $capacity")
            }

            field = value
        }

    override fun getByteAt(index: Int): Byte {
        ensureCanRead(index, 1, writeIndex)
        return array[index]
    }

    override fun getShortAt(index: Int): Short {
        ensureCanRead(index, 2, writeIndex)
        return array.getShortAt(index)
    }

    override fun getIntAt(index: Int): Int {
        ensureCanRead(index, 4, writeIndex)
        return array.getIntAt(index)
    }

    override fun getLongAt(index: Int): Long {
        ensureCanRead(index, 8, writeIndex)
        return array.getLongAt(index)
    }

    override fun setByteAt(index: Int, value: Byte) {
        ensureCanWrite(index, 1, capacity)
        array[index] = value
    }

    override fun setShortAt(index: Int, value: Short) {
        ensureCanWrite(index, 2, capacity)
        array.setShortAt(index, value)
    }

    override fun setIntAt(index: Int, value: Int) {
        ensureCanWrite(index, 4, capacity)
        array.setIntAt(index, value)
    }

    override fun setLongAt(index: Int, value: Long) {
        ensureCanWrite(index, 8, capacity)
        array.setLongAt(index, value)
    }

    override fun writeBufferAt(index: Int, value: Buffer): Int {
        return value.readToByteArray(array, index, capacity)
    }

    override fun readToByteArrayAt(index: Int, destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startIndex($startIndex) must be >= 0" }
        require(endIndex <= destination.size) { "endIndex($endIndex) must be <= destination.size(${destination.size})" }
        require(startIndex <= endIndex) { "startIndex($startIndex) must be <= endIndex($endIndex)" }
        require(index < capacity) { "index($index) must be < capacity($capacity)" }

        val count = min(capacity - index, endIndex - startIndex)

        array.copyInto(destination, startIndex, index, index + count)
        readIndex += count
        return count
    }

    override fun readToByteArray(destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startIndex($startIndex) must be >= 0" }
        require(endIndex <= destination.size) { "endIndex($endIndex) must be <= destination.size(${destination.size})" }
        require(startIndex <= endIndex) { "startIndex($startIndex) must be <= endIndex($endIndex)" }

        val count = min(availableForRead, endIndex - startIndex)

        array.copyInto(destination, startIndex, readIndex, readIndex + count)
        readIndex += count
        return count
    }

    override fun writeByteArrayAt(index: Int, value: ByteArray, startPosition: Int, endPosition: Int): Int {
        require(startPosition >= 0) { "startPosition($startPosition) must be >= 0" }
        require(endPosition <= value.size) { "endPosition($endPosition) must be <= value.size(${value.size})" }
        require(startPosition <= endPosition) { "startPosition($startPosition) must be <= endPosition($endPosition)" }

        val count = min(capacity - index, endPosition - startPosition)
        value.copyInto(array, index, startPosition, startPosition + count)
        return count
    }

    /**
     * Returns this buffer back to the pool.
     */
    override fun close() {
        pool.recycle(array)
    }

    override fun compact() {
        if (readIndex == 0) return
        array.copyInto(array, 0, readIndex, writeIndex)
        writeIndex = availableForRead
        readIndex = 0
    }

    override fun steal(): Buffer {
        val buffer = ByteArrayBuffer(array, readIndex, writeIndex)
        readIndex = 0
        writeIndex = 0
        return buffer
    }
}
