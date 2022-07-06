package io.ktor.io

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
        ensureCanWrite(1)
        setBooleanAt(writeIndex++, value)
    }

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
     * Reads [Byte] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 1.
     */
    public fun readByte(): Byte {
        ensureCanRead(1)
        return getByteAt(readIndex++)
    }

    /**
     * Writes [Byte] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 1.
     */
    public fun writeByte(value: Byte) {
        ensureCanWrite(1)
        setByteAt(writeIndex++, value)
    }

    /**
     * Reads [Short] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 2] is greater [capacity].
     */
    public fun getShortAt(index: Int): Short {
        val byte1 = getByteAt(index)
        val byte2 = getByteAt(index + 1)
        return ((byte1.toInt() shl 8) or byte2.toInt()).toShort()
    }

    /**
     * Writes [Short] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 2] is greater than [capacity].
     */
    public fun setShortAt(index: Int, value: Short) {
        val rawValue = value.toInt()
        setByteAt(index, (rawValue shr 8).and(0xFF).toByte())
        setByteAt(index, (rawValue and 0xFF).toByte())
    }

    /**
     * Reads [Short] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 2.
     */
    public fun readShort(): Short {
        ensureCanRead(2)
        val result = getShortAt(readIndex)
        readIndex += 2
        return result
    }

    /**
     * Writes [Short] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 2.
     */
    public fun writeShort(value: Short) {
        ensureCanWrite(2)
        setShortAt(writeIndex, value)
        writeIndex += 2
    }

    /**
     * Reads [Int] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 4] is greater than [capacity].
     */
    public fun getIntAt(index: Int): Int {
        val byte1 = getByteAt(index)
        val byte2 = getByteAt(index + 1)
        val byte3 = getByteAt(index + 2)
        val byte4 = getByteAt(index + 3)
        return ((byte1.toInt() shl 24) or (byte2.toInt() shl 16) or (byte3.toInt() shl 8) or byte4.toInt())
    }

    /**
     * Writes [Int] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 4] is greater than [capacity].
     */
    public fun setIntAt(index: Int, value: Int) {
        setByteAt(index, (value shr 24).and(0xFF).toByte())
        setByteAt(index, (value shr 16).and(0xFF).toByte())
        setByteAt(index, (value shr 8).and(0XFF).toByte())
        setByteAt(index, value.and(0xFF).toByte())
    }

    /**
     * Reads [Int] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 4.
     */
    public fun readInt(): Int {
        ensureCanRead(4)
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
        ensureCanWrite(4)
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
        val int1 = getIntAt(index)
        val int2 = getIntAt(index + 4)
        return ((int1.toLong() shl 32) or int2.toLong())
    }

    /**
     * Writes [Long] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 8] is greater than [capacity] or not enough space available.
     */
    public fun setLongAt(index: Int, value: Long) {
        setIntAt(index, (value shr 32).and(0xFFFFFFFF).toInt())
        setIntAt(index + 4, (value.and(0xFFFFFFFF)).toInt())
    }

    /**
     * Reads [Long] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 8.
     */
    public fun readLong(): Long {
        ensureCanRead(8)
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
        ensureCanWrite(8)
        setLongAt(writeIndex, value)
        writeIndex += 8
    }

    /**
     * Writes as many bytes as possible from the [value] at specific [index].
     *
     * The [value.readIndex] increased by amount of copied bytes.
     *
     * @return Number of written bytes: `min(availableForWrite, buffer.availableForRead)`
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public fun writeBufferAt(index: Int, value: Buffer): Int

    /**
     * Write buffer to the current buffer. The implementation depends on the actual buffer implementation. The [value]
     * will be consumed if it's possible to write all of its bytes.
     */
    public fun writeBuffer(value: Buffer): Int {
        val count = writeBufferAt(writeIndex, value)
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
    public fun readToByteArrayAt(
        index: Int,
        destination: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = destination.size
    ): Int

    /**
     * Copy as much as possible bytes from the current buffer to the [destination] between [startIndex] and [endIndex].
     *
     * This operation increase [readIndex] by the number of copied bytes.
     *
     * @return Number of copied bytes: `min(availableForRead, endPosition - startPosition)`
     */
    public fun readToByteArray(
        destination: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = destination.size
    ): Int

    /**
     * Copy all bytes from [value] between [startPosition] and [endPosition] to the buffer at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @return Number of written bytes: `min(availableForWrite, endPosition - startPosition)`
     * @throws IndexOutOfBoundsException if [index] is greater or equal [capacity].
     */
    public fun writeByteArrayAt(
        index: Int,
        value: ByteArray,
        startPosition: Int = 0,
        endPosition: Int = value.size
    ): Int

    /**
     * Copy values from byte array to the buffer at [writeIndex] between [startIndex] and [endIndex].
     *
     * @ return number of copied bytes = `min(availableForWrite, endIndex - startIndex)`
     */
    public fun writeByteArray(value: ByteArray, startIndex: Int = 0, endIndex: Int = value.size): Int {
        val result = writeByteArrayAt(writeIndex, value, startIndex, endIndex)
        writeIndex += result
        return result
    }

    /**
     * Move all bytes in range [readIndex], [writeIndex] to range [0] and [writeIndex - readIndex] and modifies the
     * [readIndex] and [writeIndex] accordingly.
     */
    public fun compact()

    public companion object {
        /**
         * The buffer with zero capacity.
         */
        public val Empty: Buffer = ByteArrayBuffer(0)
    }
}
