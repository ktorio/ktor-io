package io.ktor.io

import kotlin.math.max
import kotlin.math.min

/**
 * The [Buffer] class represents a mutable sequence of bytes in memory.
 *
 * The buffer is not thread-safe by default.
 */
public interface Buffer : Readable, Writable {
    /**
     * The number of bytes can be stored in the buffer. Upper bound for write operations.
     */
    public val capacity: Int
    override val readLimit: Int get() = writeIndex
    override val writeLimit: Int get() = capacity

    //TODO: may be move other buffer/ByteArray methods to Readable and Writable?

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

    //on buffer, we can read/write absolutely up to capacity, not read limit
    public override fun ensureCanReadAt(index: Int, count: Int) {
        if (index + count > capacity) {
            throw IndexOutOfBoundsException("Can't read $count bytes at index $index. Capacity: $capacity.")
        }
    }

    //buffer specific methods

    //TODO: do we need to save indexes when stealing? - looks like yes
    //TODO: readLimit should not affect steal operations?

    //current instance becomes unusable(empty), returned will store the data in read only state
    public fun stealReadOnly(): ReadOnlyBuffer

    //current instance becomes unusable(empty)
    //TODO: looks similar to netty5 Send<Buffer> - ownership change
    public fun steal(): Buffer //useful for composite buffers

    public fun stealHead(index: Int): Buffer

    public fun readStealing(size: Int): Buffer = stealHead(readIndex + size)

    /**
     * Release [Buffer] back to pool if necessary.
     */
    override fun close() {
    }

    public companion object {
        /**
         * The buffer with zero capacity.
         */
        public val Empty: Buffer = object : Buffer, ReadOnlyBuffer {
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
            override fun stealReadOnly(): ReadOnlyBuffer = this

            override fun stealHead(index: Int): Buffer {
                TODO("Not yet implemented")
            }


            override fun copy(): ReadOnlyBuffer = this

            override fun getBufferAt(index: Int, size: Int): ReadOnlyBuffer {
                TODO("Not yet implemented")
            }
        }
    }
}

internal fun Buffer.reset() {
    readIndex = 0
    writeIndex = 0
}


//TODO remove it
//buffer isn't closed in the end of function, so we can write to it later
private fun parse(buffer: Buffer): Pair<ReadOnlyBuffer, ReadOnlyBuffer> {
    val length = buffer.readInt()
    //we deattach head from base buffer,
    // so someone can safely write to it,
    // and we have head which we can send to another thread
    return buffer
        .readStealing(length)
        .stealReadOnly() //here we can already send this buffer somewhere, and process it there
        .use { frame ->
            //frame.readIndex = buffer.readIndex
            //frame.writeIndex|readLimit = buffer.readIndex + length

            //buffer.readIndex = 0
            //buffer.writeIndex = min(buffer.readIndex + length, buffer.writeIndex)

            val header = frame.readLong()
            val lengthSplit = frame.readInt()
            val b1 = frame.readBuffer(lengthSplit)
            val b2 = frame.readBuffer()
            b1 to b2
        }
}
