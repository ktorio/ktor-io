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
                throw IndexOutOfBoundsException("readIndex($value) must be >= 0 and < writeIndex: $writeIndex")
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

    override fun setByteAt(index: Int, value: Byte) {
        ensureCanWrite(index, 1, capacity)
        array[index] = value
    }

    override fun copyFromBufferAt(index: Int, value: Buffer): Int {
        return value.copyToByteArray(array, index, capacity)
    }

    override fun copyToByteArrayAt(index: Int, destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startIndex($startIndex) must be >= 0" }
        require(endIndex <= destination.size) { "endIndex($endIndex) must be <= destination.size(${destination.size})" }
        require(startIndex <= endIndex) { "startIndex($startIndex) must be <= endIndex($endIndex)" }
        require(index < capacity) { "index($index) must be < capacity($capacity)" }

        val count = min(capacity - index, endIndex - startIndex)

        array.copyInto(destination, startIndex, index, index + count)
        readIndex += count
        return count
    }

    override fun copyToByteArray(destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startIndex($startIndex) must be >= 0" }
        require(endIndex <= destination.size) { "endIndex($endIndex) must be <= destination.size(${destination.size})" }
        require(startIndex <= endIndex) { "startIndex($startIndex) must be <= endIndex($endIndex)" }

        val count = min(availableForRead, endIndex - startIndex)

        array.copyInto(destination, startIndex, readIndex, readIndex + count)
        readIndex += count
        return count
    }

    override fun copyFromByteArrayAt(index: Int, value: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startPosition($startIndex) must be >= 0" }
        require(endIndex <= value.size) { "endPosition($endIndex) must be <= value.size(${value.size})" }
        require(startIndex <= endIndex) { "startPosition($startIndex) must be <= endPosition($endIndex)" }

        val count = min(capacity - index, endIndex - startIndex)
        value.copyInto(array, index, startIndex, startIndex + count)
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
}
