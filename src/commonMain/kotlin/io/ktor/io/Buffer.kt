package io.ktor.io

/**
 * The [Buffer] class represents a mutable sequence of bytes in memory.
 *
 * The buffer is not thread-safe by default.
 */
public abstract class Buffer : Closeable {
    /**
     * The number of bytes can be stored in the buffer. Upper bound for write operations.
     */
    public abstract val capacity: Int

    /**
     * The index in buffer for the read operation.
     *
     * Should be between 0 and [writeIndex]
     */
    public abstract var readIndex: Int

    /**
     * The index in buffer for the write operation.
     *
     * Should be between the [readIndex] and [capacity]
     */
    public abstract var writeIndex: Int

    /**
     * Reads [Boolean] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public open fun loadBooleanAt(index: Int): Boolean = loadByteAt(index) != 0.toByte()

    /**
     * Reads [Byte] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun loadByteAt(index: Int): Byte

    /**
     * Reads [Short] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun loadShortAt(index: Int): Short

    /**
     * Reads [Int] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun loadIntAt(index: Int): Int

    /**
     * Reads [Long] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun loadLongAt(index: Int): Long

    /**
     * Reads [Float] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun loadFloatAt(index: Int): Float

    /**
     * Reads [Double] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun loadDoubleAt(index: Int): Double

    /**
     * Writes [Boolean] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public open fun storeBooleanAt(index: Int, value: Boolean) {
        storeByteAt(index, if (value) 1.toByte() else 0.toByte())
    }

    /**
     * Writes [Byte] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun storeByteAt(index: Int, value: Byte)

    /**
     * Writes [Short] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun storeShortAt(index: Int, value: Short)

    /**
     * Writes [Int] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun storeIntAt(index: Int, value: Int)

    /**
     * Writes [Long] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun storeLongAt(index: Int, value: Long)

    /**
     * Writes [Float] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun storeFloatAt(index: Int, value: Float)

    /**
     * Writes [Double] at specific [index].
     *
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun storeDoubleAt(index: Int, value: Double)

    /**
     * Writes as many bytes as possible from the [buffer] at specific [index].
     *
     * @return Number of written bytes: `min(availableForWrite, buffer.availableForRead)`
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun storeBufferAt(index: Int, buffer: Buffer): Int

    /**
     * Copy all bytes from [value] between [startPosition] and [endPosition] to the buffer at specific [index].
     *
     * @return Number of written bytes: `min(availableForWrite, endPosition - startPosition)`
     * @throws IndexOutOfBoundsException if [index] is greater than [capacity].
     */
    public abstract fun storeArrayAt(
        index: Int,
        value: ByteArray,
        startPosition: Int = 0,
        endPosition: Int = value.size
    ): Int

    /**
     * Read boolean from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 1.
     */
    public open fun readBoolean(): Boolean = loadBooleanAt(readIndex++)

    /**
     * Reads [Byte] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 1.
     */
    public open fun readByte(): Byte = loadByteAt(readIndex++)

    /**
     * Reads [Short] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 2.
     */
    public open fun readShort(): Short = loadShortAt(readIndex).also { readIndex += 2 }

    /**
     * Reads [Int] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 4.
     */
    public open fun readInt(): Int = loadIntAt(readIndex).also { readIndex += 4 }

    /**
     * Reads [Long] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 8.
     */
    public open fun readLong(): Long = loadLongAt(readIndex).also { readIndex += 8 }

    /**
     * Reads [Float] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 4.
     */
    public open fun readFloat(): Float = loadFloatAt(readIndex).also { readIndex += 4 }

    /**
     * Reads [Double] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 8.
     */
    public open fun readDouble(): Double = loadDoubleAt(readIndex).also { readIndex += 8 }

    /**
     * Write boolean to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 1.
     */
    public open fun writeBoolean(value: Boolean) {
        storeBooleanAt(writeIndex++, value)
    }

    /**
     * Writes [Byte] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 1.
     */
    public open fun writeByte(value: Byte) {
        storeByteAt(writeIndex++, value)
    }

    /**
     * Writes [Short] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 2.
     */
    public open fun writeShort(value: Short) {
        storeShortAt(writeIndex, value)
        writeIndex += 2
    }

    /**
     * Writes [Int] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 4.
     */
    public open fun writeInt(value: Int) {
        storeIntAt(writeIndex, value)
        writeIndex += 4
    }

    /**
     * Writes [Long] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 8.
     */
    public open fun writeLong(value: Long) {
        storeLongAt(writeIndex, value)
        writeIndex += 8
    }

    /**
     * Writes [Float] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 4.
     */
    public open fun writeFloat(value: Float) {
        storeFloatAt(writeIndex, value)
        writeIndex += 4
    }

    /**
     * Writes [Double] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 8.
     */
    public open fun writeDouble(value: Double) {
        storeDoubleAt(writeIndex, value)
        writeIndex += 8
    }

    /**
     * Copy values from byte array to the buffer at [writeIndex] between [startIndex] and [endIndex].
     *
     * @return number of copied bytes = `min(availableForWrite, endIndex - startIndex)`
     */
    public abstract fun writeArray(value: ByteArray, startIndex: Int = 0, endIndex: Int = value.size): Int

    /**
     * Write buffer to the current buffer. The implementation depends on the actual buffer implementation. The [value]
     * will be consumed if it's possible to write all of its bytes.
     */
    public abstract fun writeBuffer(value: Buffer): Int

    /**
     * Copy all bytes from the buffer at [readIndex] between [startPosition] and [endPosition] to the [buffer].
     *
     * @return Number of copied bytes: `min(availableForRead, endPosition - startPosition)`
     */
    public abstract fun copyToArray(
        destination: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = destination.size
    ): Int

    /**
     * Move all bytes in range [readIndex], [writeIndex] to range [0] and [writeIndex - readIndex] and modifies the
     * [readIndex] and [writeIndex] accordingly.
     */
    public abstract fun compact()

    public companion object {
        /**
         * The buffer with zero capacity.
         */
        public val Empty: Buffer = ByteArrayBuffer(0)
    }
}
