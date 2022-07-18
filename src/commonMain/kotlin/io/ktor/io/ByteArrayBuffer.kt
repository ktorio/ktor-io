package io.ktor.io

import io.ktor.io.internals.*
import io.ktor.io.internals.ReferenceCounter
import kotlin.math.max
import kotlin.math.min

public const val DEFAULT_POOL_CAPACITY: Int = 2000
public const val DEFAULT_BUFFER_SIZE: Int = 1024 * 16

public class ByteArrayBuffer internal constructor(
    array: ByteArray,
    startIndex: Int,
    endIndex: Int,
    readIndex: Int = 0,
    writeIndex: Int = startIndex - endIndex,
    private val referenceCounter: ReferenceCounter = EmptyReferenceCounter,
    public val pool: ObjectPool<ByteArrayBuffer> = ByteArrayBufferPool.Empty
) : Buffer {

    init {
        require(startIndex >= 0) { "startIndex must be non-negative" }
        require(endIndex >= 0) { "endIndex must be non-negative" }
        require(startIndex <= endIndex) { "startIndex must be less than or equal to endIndex" }
        require(readIndex >= 0) { "readIndex must be non-negative" }
        require(writeIndex >= 0) { "writeIndex must be non-negative" }
        require(readIndex <= writeIndex) { "readIndex $readIndex must be less than or equal to writeIndex $readIndex" }
        require(endIndex <= array.size) { "endIndex $endIndex must be less than or equal to array.size ${array.size}" }
    }

    public constructor(
        array: ByteArray,
        readIndex: Int = 0,
        writeIndex: Int = array.size,
        /**
         * The pool used for allocation of the [array].
         */
        pool: ObjectPool<ByteArrayBuffer> = ByteArrayBufferPool.Empty
    ) : this(
        array,
        0,
        array.size,
        readIndex,
        writeIndex,
        if (pool == ByteArrayBufferPool.Empty) EmptyReferenceCounter else AtomicReferenceCounter(),
        pool
    )

    /**
     * Creates buffer of fixed [capacity].
     */
    public constructor(capacity: Int = DEFAULT_BUFFER_SIZE) : this(ByteArray(capacity), readIndex = 0, writeIndex = 0)

    private var startIndex: Int = startIndex
        set(value) {
            require(value >= 0) { "startIndex must be non-negative: $value" }
            require(value < array.size) { "startIndex must be less than array.size: $value" }

            field = value
        }

    private var endIndex: Int = endIndex
        set(value) {
            require(value >= 0) { "endIndex must be non-negative: $value" }
            require(value <= array.size) { "endIndex must be less than or equal to array.size: $value" }

            field = value
        }

    /**
     * Provides access to underlying byte array.
     *
     * Please note, all changes of the array will be reflected in the buffer.
     */
    @Suppress("CanBePrimaryConstructorProperty")
    public var array: ByteArray = array
        private set

    override val capacity: Int
        get() = endIndex - startIndex

    init {
        referenceCounter.retain()
    }

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
        return array[startIndex + index]
    }

    override fun setByteAt(index: Int, value: Byte) {
        ensureCanWrite(index, 1, capacity)
        array[startIndex + index] = value
    }

    override fun takeHead(index: Int): Buffer {
        if (index < 0 || index >= capacity) {
            throw IndexOutOfBoundsException("Split index $index should be in range [0, $capacity)")
        }

        val head = ByteArrayBuffer(
            array,
            startIndex,
            startIndex + index,
            min(readIndex, index),
            min(writeIndex, index),
            referenceCounter,
            pool
        )

        startIndex += index
        readIndex = max(0, readIndex - index)
        writeIndex = max(0, writeIndex - index)

        return head
    }

    override fun copyFromBufferAt(index: Int, value: Buffer): Int {
        return value.copyToByteArray(array, index + startIndex, capacity)
    }

    override fun copyToByteArrayAt(index: Int, destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startIndex($startIndex) must be >= 0" }
        require(endIndex <= destination.size) { "endIndex($endIndex) must be <= destination.size(${destination.size})" }
        require(startIndex <= endIndex) { "startIndex($startIndex) must be <= endIndex($endIndex)" }
        require(index < capacity) { "index($index) must be < capacity($capacity)" }

        val count = min(capacity - index, endIndex - startIndex)

        array.copyInto(destination, startIndex, this.startIndex + index, index + count)
        readIndex += count
        return count
    }

    override fun copyToByteArray(destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startIndex($startIndex) must be >= 0" }
        require(endIndex <= destination.size) { "endIndex($endIndex) must be <= destination.size(${destination.size})" }
        require(startIndex <= endIndex) { "startIndex($startIndex) must be <= endIndex($endIndex)" }

        val count = min(availableForRead, endIndex - startIndex)

        array.copyInto(destination, startIndex, this.startIndex + readIndex, this.startIndex + readIndex + count)
        readIndex += count
        return count
    }

    override fun copyFromByteArrayAt(index: Int, value: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startPosition($startIndex) must be >= 0" }
        require(endIndex <= value.size) { "endPosition($endIndex) must be <= value.size(${value.size})" }
        require(startIndex <= endIndex) { "startPosition($startIndex) must be <= endPosition($endIndex)" }

        val count = min(capacity - index, endIndex - startIndex)
        value.copyInto(array, this.startIndex + index, startIndex, startIndex + count)
        return count
    }

    /**
     * Returns this buffer back to the pool.
     */
    override fun close() {
        if (referenceCounter.release()) {
            pool.recycle(this)
        }
    }

    override fun compact() {
        if (readIndex == 0) return
        array.copyInto(array, 0, readIndex, writeIndex)
        writeIndex = availableForRead
        readIndex = 0
    }
}
