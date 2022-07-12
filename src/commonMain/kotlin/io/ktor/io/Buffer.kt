package io.ktor.io

import kotlin.math.max
import kotlin.math.min

/**
 * The [Buffer] class represents a mutable sequence of bytes in memory.
 *
 * The buffer is not thread-safe by default.
 */
public interface Buffer : Closeable {
    /**
     * The number of bytes can be stored in the buffer. Upper bound for write operations.
     */
    public val capacity: Int

    /**
     * The index in buffer for the read operation.
     *
     * Should be between 0 and [writeIndex]
     */
    public var readIndex: Int

    /**
     * The index in buffer for the write operation.
     *
     * Should be between the [readIndex] and [capacity].
     */
    public var writeIndex: Int

    /**
     * Reads [Byte] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 1] is greater [capacity].
     */
    public fun getByteAt(index: Int): Byte

    /**
     * Writes [Byte] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 1] is greater than [capacity].
     */
    public fun setByteAt(index: Int, value: Byte)

    /**
     * Steal all bytes from the current buffer to the new instance.
     */
    public fun steal(): Buffer

    /**
     * Reads [Byte] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 1.
     */
    public fun readByte(): Byte {
        checkCanRead(1)
        return getByteAt(readIndex++)
    }

    /**
     * Writes [Byte] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 1.
     */
    public fun writeByte(value: Byte) {
        checkCanWrite(1)
        setByteAt(writeIndex++, value)
    }

    /**
     * Reads [Boolean] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 1] is greater [capacity].
     */
    public fun getBooleanAt(index: Int): Boolean = getByteAt(index) != 0.toByte()

    /**
     * Writes [Boolean] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 1] is greater than [capacity].
     */
    public fun setBooleanAt(index: Int, value: Boolean) {
        setByteAt(index, if (value) 1.toByte() else 0.toByte())
    }

    /**
     * Read boolean from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 1.
     */
    public fun readBoolean(): Boolean = getBooleanAt(readIndex++)

    /**
     * Write boolean to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 1.
     */
    public fun writeBoolean(value: Boolean) {
        checkCanWrite(1)
        setBooleanAt(writeIndex++, value)
    }

    /**
     * Reads [Short] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 2] is greater [capacity].
     */
    public fun getShortAt(index: Int): Short {
        checkCanRead(index, 2)

        val byte1 = getByteAt(index)
        val byte2 = getByteAt(index + 1)
        return Short(byte1, byte2)
    }

    /**
     * Writes [Short] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 2] is greater than [capacity].
     */
    public fun setShortAt(index: Int, value: Short) {
        checkCanWrite(index, 2)

        setByteAt(index, value.highByte)
        setByteAt(index + 1, value.lowByte)
    }

    /**
     * Writes [Short] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 2.
     */
    public fun writeShort(value: Short) {
        checkCanWrite(2)

        setShortAt(writeIndex, value)
        writeIndex += 2
    }

    /**
     * Reads [Short] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 2.
     */
    public fun readShort(): Short {
        checkCanRead(2)

        val result = getShortAt(readIndex)
        readIndex += 2
        return result
    }

    /**
     * Reads [Int] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 4] is greater than [capacity].
     */
    public fun getIntAt(index: Int): Int {
        checkCanRead(index, 4)

        val highShort = getShortAt(index)
        val lowShort = getShortAt(index + 2)
        return Int(highShort, lowShort)
    }

    /**
     * Writes [Int] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 4] is greater than [capacity].
     */
    public fun setIntAt(index: Int, value: Int) {
        checkCanWrite(index, 4)

        setShortAt(index, value.highShort)
        setShortAt(index + 2, value.lowShort)
    }

    /**
     * Reads [Int] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 4.
     */
    public fun readInt(): Int {
        checkCanRead(4)

        val result = getIntAt(readIndex)
        readIndex += 4
        return result
    }

    /**
     * Writes [Int] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 4.
     */
    public fun writeInt(value: Int) {
        checkCanWrite(4)

        setIntAt(writeIndex, value)
        writeIndex += 4
    }

    /**
     * Reads [Long] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 8] is greater than [capacity].
     */
    public fun getLongAt(index: Int): Long {
        checkCanRead(index, 8)

        val highInt = getIntAt(index)
        val lowInt = getIntAt(index + 4)
        return Long(highInt, lowInt)
    }

    /**
     * Writes [Long] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 8] is greater than [capacity] or not enough space available.
     */
    public fun setLongAt(index: Int, value: Long) {
        checkCanWrite(index, 8)

        setIntAt(index, value.highInt)
        setIntAt(index + 4, value.lowInt)
    }

    /**
     * Reads [Long] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 8.
     */
    public fun readLong(): Long {
        checkCanRead(8)

        val result = getLongAt(readIndex)
        readIndex += 8
        return result
    }

    /**
     * Writes [Long] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 8.
     */
    public fun writeLong(value: Long) {
        checkCanWrite(8)

        setLongAt(writeIndex, value)
        writeIndex += 8
    }

    /**
     * Writes as many bytes as possible from the [value] at specific [index].
     *
     * The [value.readIndex] increased by amount of copied bytes.
     *
     * @return Number of written bytes: `min(availableForWrite, buffer.availableForRead)`
     */
    public fun copyFromBufferAt(index: Int, value: Buffer): Int {
        val count = min(capacity - index, value.availableForRead)
        for (currentIndex in 0 until count) {
            setByteAt(index + currentIndex, value.getByteAt(value.readIndex++))
        }

        return max(count, 0)
    }

    /**
     * Write [value] to the current buffer. The implementation depends on the actual buffer implementation.
     */
    public fun copyFromBuffer(value: Buffer): Int {
        val count = copyFromBufferAt(writeIndex, value)
        writeIndex += count
        return count
    }

    /**
     * Copy as much as possible bytes from the current buffer to the [destination] between [startIndex] and [endIndex].
     *
     * This operation increase [readIndex] by the number of copied bytes.
     *
     * @return Number of copied bytes: `min(availableForRead, endPosition - startPosition)`
     */
    public fun copyToByteArrayAt(
        index: Int,
        destination: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = destination.size
    ): Int {
        val count = min(endIndex - startIndex, capacity - index)
        for (offset in 0 until count) {
            destination[startIndex + offset] = getByteAt(index + offset)
        }

        return max(count, 0)
    }

    /**
     * Copy as much as possible bytes from the current buffer to the [destination] between [startIndex] and [endIndex].
     *
     * This operation increase [readIndex] by the number of copied bytes.
     *
     * @return Number of copied bytes: `min(availableForRead, endPosition - startPosition)`
     */
    public fun copyToByteArray(
        destination: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = destination.size
    ): Int {
        val count = min(endIndex - startIndex, availableForRead)
        if (count < 0) return 0

        val result = copyToByteArrayAt(readIndex, destination, startIndex, startIndex + count)
        readIndex += result
        return result
    }

    /**
     * Copy all bytes from [value] between [startIndex] and [endIndex] to the buffer at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @return Number of written bytes: `min(availableForWrite, endPosition - startPosition)`
     * @throws IndexOutOfBoundsException if [index] is greater or equal [capacity].
     */
    public fun copyFromByteArrayAt(
        index: Int,
        value: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = value.size
    ): Int {
        val count = min(endIndex - startIndex, capacity - index)
        for (offset in 0 until count) {
            setByteAt(index + offset, value[startIndex + offset])
        }

        return max(count, 0)
    }

    /**
     * Copy values from byte array to the buffer at [writeIndex] between [startIndex] and [endIndex].
     *
     * @ return number of copied bytes = `min(availableForWrite, endIndex - startIndex)`
     */
    public fun copyFromByteArray(value: ByteArray, startIndex: Int = 0, endIndex: Int = value.size): Int {
        val result = copyFromByteArrayAt(writeIndex, value, startIndex, endIndex)
        writeIndex += result
        return result
    }

    /**
     * Move all bytes in range [readIndex], [writeIndex] to range [0] and [writeIndex - readIndex] and modifies the
     * [readIndex] and [writeIndex] accordingly.
     */
    public fun compact() {
        if (readIndex == 0) return

        if (readIndex == writeIndex) {
            readIndex = 0
            writeIndex = 0
            return
        }

        val count = writeIndex - readIndex
        for (index in 0 until count) {
            setByteAt(index, getByteAt(readIndex + index))
        }

        readIndex = 0
        writeIndex = count
    }

    /**
     * Release [Buffer] back to pool if necessary.
     */
    override fun close() {
    }

    public companion object {
        /**
         * The buffer with zero capacity.
         */
        public val Empty: Buffer = object : Buffer {
            override val capacity: Int
                get() = 0

            override var readIndex: Int
                get() = 0
                set(value) {
                    require(value == 0) { "Can't modify default empty buffer" }
                }

            override var writeIndex: Int
                get() = 0
                set(value) {
                    require(value == 0) { "Can't modify default empty buffer" }
                }

            override fun getByteAt(index: Int): Byte {
                throw IndexOutOfBoundsException("Can't read from empty buffer")
            }

            override fun setByteAt(index: Int, value: Byte) {
                throw IndexOutOfBoundsException("Can't write to empty buffer")
            }

            override fun steal(): Buffer = this
        }
    }
}
