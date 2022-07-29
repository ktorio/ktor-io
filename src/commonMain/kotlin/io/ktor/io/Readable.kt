package io.ktor.io

//TODO: update documentation
//TODO: absolute access - when buffer, absolute access is possible until `capacity` not until `readLimit`?
public interface Readable : Closeable {
    //TODO: readLimit/readIndex(same for writable) naming clash with readByte/Int other operations - is it a problem?
    public val readLimit: Int

    /**
     * The index in buffer for the read operation.
     *
     * Should be between 0 and [writeIndex]
     */
    public var readIndex: Int

    /**
     * Reads [Byte] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 1] is greater [capacity].
     */
    public fun getByteAt(index: Int): Byte

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
     * Reads [Short] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 2] is greater [capacity].
     */
    public fun getShortAt(index: Int): Short {
        ensureCanReadAt(index, 2)

        val byte1 = getByteAt(index)
        val byte2 = getByteAt(index + 1)
        return Short(byte1, byte2)
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
     * Reads [Int] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 4] is greater than [capacity].
     */
    public fun getIntAt(index: Int): Int {
        ensureCanReadAt(index, 4)

        val highShort = getShortAt(index)
        val lowShort = getShortAt(index + 2)
        return Int(highShort, lowShort)
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
     * Reads [Long] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 8] is greater than [capacity].
     */
    public fun getLongAt(index: Int): Long {
        ensureCanReadAt(index, 8)

        val highInt = getIntAt(index)
        val lowInt = getIntAt(index + 4)
        return Long(highInt, lowInt)
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

    public fun ensureCanReadAt(index: Int, count: Int) {
        if (index + count > readLimit) {
            throw IndexOutOfBoundsException("Can't read $count bytes at index $index. ReadLimit: $readLimit.")
        }
    }

    public fun ensureCanRead(count: Int) {
        if (readIndex + count > readLimit) {
            throw IndexOutOfBoundsException("Can't read $count bytes. Available: $availableForRead.")
        }
    }
}

public val Readable.availableForRead: Int get() = readLimit - readIndex
public val Readable.isEmpty: Boolean get() = availableForRead == 0
public val Readable.isNotEmpty: Boolean get() = !isEmpty

public operator fun Readable.get(index: Int): Byte = getByteAt(index)
