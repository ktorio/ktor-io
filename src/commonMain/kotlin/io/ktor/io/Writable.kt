package io.ktor.io

//TODO: update documentation
//TODO: do we need to allow to grow limit implicitly on write? (netty5)
public interface Writable : Closeable {
    public val writeLimit: Int

    /**
     * The index in buffer for the write operation.
     *
     * Should be between the [readIndex] and [capacity].
     */
    public var writeIndex: Int

    /**
     * Writes [Byte] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 1] is greater than [capacity].
     */
    public fun setByteAt(index: Int, value: Byte)


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
     * Writes [Short] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 2] is greater than [capacity].
     */
    public fun setShortAt(index: Int, value: Short) {
        ensureCanWriteAt(index, 2)

        setByteAt(index, value.highByte)
        setByteAt(index + 1, value.lowByte)
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
     * Writes [Int] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 4] is greater than [capacity].
     */
    public fun setIntAt(index: Int, value: Int) {
        ensureCanWriteAt(index, 4)

        setShortAt(index, value.highShort)
        setShortAt(index + 2, value.lowShort)
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
     * Writes [Long] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 8] is greater than [capacity] or not enough space available.
     */
    public fun setLongAt(index: Int, value: Long) {
        ensureCanWriteAt(index, 8)

        setIntAt(index, value.highInt)
        setIntAt(index + 4, value.lowInt)
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

    //TODO: should ensureCanWriteAt/ensureCanWrite increase writeLimit? (netty5 do this) - investigate

    public fun ensureCanWriteAt(index: Int, count: Int) {
        if (index + count > writeLimit) {
            throw IndexOutOfBoundsException("Can't write $count bytes at index $index. WriteLimit: $writeLimit.")
        }
    }

    public fun ensureCanWrite(count: Int) {
        if (writeIndex + count > writeLimit) {
            throw IndexOutOfBoundsException("Can't write $count bytes. Available space: $availableForWrite.")
        }
    }
}

public val Writable.availableForWrite: Int get() = writeLimit - writeIndex
public val Writable.isFull: Boolean get() = availableForWrite == 0
public val Writable.isNotFull: Boolean get() = !isFull

public operator fun Writable.set(index: Int, value: Byte) {
    setByteAt(index, value)
}
