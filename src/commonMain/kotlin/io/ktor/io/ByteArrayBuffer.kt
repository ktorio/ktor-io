package io.ktor.io

import kotlin.math.min

public const val DEFAULT_POOL_CAPACITY: Int = 2000
public const val DEFAULT_BUFFER_SIZE: Int = 1024 * 16

public class ByteArrayBuffer(
    array: ByteArray,
    private val pool: ObjectPool<ByteArray>
) : Buffer() {

    /**
     * Creates buffer of fixed [capacity].
     */
    public constructor(capacity: Int) : this(ByteArray(capacity), ByteArrayPool.NoPool)

    public constructor(pool: ObjectPool<ByteArray>) : this(pool.borrow(), pool)

    /**
     * Provides access to underlying byte array.
     *
     * Please note, all changes of the array will be reflected in the buffer.
     */
    @Suppress("CanBePrimaryConstructorProperty")
    public var array: ByteArray = array

    override val capacity: Int
        get() = array.size

    override var readIndex: Int = 0
    override var writeIndex: Int = 0

    override fun loadByteAt(index: Int): Byte {
        checkCanRead(index, 1, writeIndex)
        return array[index]
    }

    override fun loadShortAt(index: Int): Short {
        checkCanRead(index, 2, writeIndex)
        return array.loadShortAt(index)
    }

    override fun loadIntAt(index: Int): Int {
        checkCanRead(index, 4, writeIndex)
        return array.loadIntAt(index)
    }

    override fun loadLongAt(index: Int): Long {
        checkCanRead(index, 8, writeIndex)
        return array.loadLongAt(index)
    }

    override fun storeByteAt(index: Int, value: Byte) {
        checkCanWrite(index, 1, capacity)
        array[index] = value
    }

    override fun storeShortAt(index: Int, value: Short) {
        checkCanWrite(index, 2, capacity)
        array.storeShortAt(index, value)
    }

    override fun storeIntAt(index: Int, value: Int) {
        checkCanWrite(index, 4, capacity)
        array.storeIntAt(index, value)
    }

    override fun storeLongAt(index: Int, value: Long) {
        checkCanWrite(index, 8, capacity)
        array.storeLongAt(index, value)
    }

    override fun storeBufferAt(index: Int, value: Buffer): Int {
        return value.copyToArray(array, index, capacity)
    }

    override fun storeArrayAt(index: Int, value: ByteArray, startPosition: Int, endPosition: Int): Int {
        require(startPosition >= 0) { "startPosition($startPosition) must be >= 0" }
        require(endPosition <= value.size) { "endPosition($endPosition) must be <= value.size(${value.size})" }
        require(startPosition <= endPosition) { "startPosition($startPosition) must be <= endPosition($endPosition)" }

        val count = min(capacity - index, endPosition - startPosition)
        value.copyInto(array, index, startPosition, startPosition + count)
        return count
    }

    override fun readArray(): ByteArray = array.sliceArray(readIndex until writeIndex)

    override fun copyToArray(destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startIndex($startIndex) must be >= 0" }
        require(endIndex <= destination.size) { "endIndex($endIndex) must be <= destination.size(${destination.size})" }
        require(startIndex <= endIndex) { "startIndex($startIndex) must be <= endIndex($endIndex)" }

        val count = min(availableForRead, endIndex - startIndex)

        array.copyInto(destination, startIndex, readIndex, readIndex + count)
        readIndex += count
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
