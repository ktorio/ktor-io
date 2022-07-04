package io.ktor.io

import io.ktor.io.internal.*
import kotlin.math.min

public const val DEFAULT_POOL_CAPACITY: Int = 2000
public const val DEFAULT_BUFFER_SIZE: Int = 1024 * 16

public class ByteArrayBuffer(
    public override val capacity: Int,
    private val pool: ObjectPool<ByteArrayBuffer>
) : Buffer() {

    public constructor(capacity: Int) : this(capacity, ByteArrayBufferPool.NoPool)

    internal val array = ByteArray(capacity)

    override var readIndex: Int = 0
    override var writeIndex: Int = 0

    override fun get(index: Int): Byte {
        return array[index]
    }

    override fun set(index: Int, value: Byte) {
        array[index] = value
    }

    override fun readByte(): Byte {
        checkHasBytesToRead(1)
        return array[readIndex++]
    }

    override fun readShort(): Short {
        checkHasBytesToRead(2)
        return doReadShort()
    }

    override fun readInt(): Int {
        checkHasBytesToRead(4)
        return doReadInt()
    }

    override fun readLong(): Long {
        checkHasBytesToRead(8)
        return doReadLong()
    }

    override fun writeByte(value: Byte) {
        checkHasSpaceToWrite(1)
        doWriteByte(value)
    }

    override fun writeShort(value: Short) {
        checkHasSpaceToWrite(2)
        doWriteShort(value)
    }

    override fun writeInt(value: Int) {
        checkHasSpaceToWrite(4)
        doWriteInt(value)
    }

    override fun writeLong(value: Long) {
        checkHasSpaceToWrite(8)
        doWriteLong(value)
    }

    override fun read(destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        checkArrayBounds(destination, startIndex, endIndex)

        val count = min(endIndex - startIndex, writeIndex - readIndex)
        array.copyInto(destination, startIndex, readIndex, readIndex + count)
        readIndex += count

        return count
    }

    override fun write(source: ByteArray, startIndex: Int, endIndex: Int): Int {
        checkArrayBounds(source, startIndex, endIndex)

        val count = min(endIndex - startIndex, capacity - writeIndex)
        source.copyInto(array, writeIndex, startIndex, startIndex + count)
        writeIndex += endIndex

        return count
    }

    override fun read(destination: Buffer) {
        if (platformRead(this, destination)) {
            return
        }

        if (destination is ByteArrayBuffer) {
            val count = read(destination.array, destination.writeIndex, destination.capacity)
            destination.writeIndex += count
            return
        }

        while (destination.canWrite() && canRead()) {
            destination.writeByte(readByte())
        }
    }

    override fun write(source: Buffer) {
        if (platformWrite(this, source)) {
            return
        }

        if (source is ByteArrayBuffer) {
            val count = write(source.array, source.readIndex, source.writeIndex)
            source.readIndex += count
            return
        }

        while (source.canRead() && canWrite()) {
            writeByte(source.readByte())
        }
    }

    override fun release() {
        pool.recycle(this)
    }

    override fun compact() {
        if (readIndex == 0) return
        array.copyInto(array, 0, readIndex, writeIndex)
        writeIndex = readCapacity()
        readIndex = 0
    }

    private fun doWriteByte(value: Byte) {
        array[writeIndex++] = value
    }

    private fun doWriteShort(value: Short) {
        doWriteByte(value.highByte)
        doWriteByte(value.lowByte)
    }

    private fun doWriteInt(value: Int) {
        doWriteShort(value.highShort)
        doWriteShort(value.lowShort)
    }

    private fun doWriteLong(value: Long) {
        doWriteInt(value.highInt)
        doWriteInt(value.lowInt)
    }

    private fun doReadShort() = Short(readByte(), readByte())

    private fun doReadInt() = Int(doReadShort(), doReadShort())

    private fun doReadLong() = Long(doReadInt(), doReadInt())

    private fun checkHasBytesToRead(count: Int) {
        if (readIndex + count > writeIndex) {
            throw IndexOutOfBoundsException(
                "Read overflow, " +
                    "trying to read $count bytes " +
                    "from buffer with ${writeIndex - 1} bytes " +
                    "and readIndex at $readIndex"
            )
        }
    }

    private fun checkHasSpaceToWrite(count: Int) {
        if (writeIndex + count > capacity) {
            throw IndexOutOfBoundsException(
                "Write overflow, " +
                    "trying to write $count bytes " +
                    "to buffer of $capacity capacity " +
                    "and writeIndex at $writeIndex"
            )
        }
    }
}

private fun checkArrayBounds(array: ByteArray, startIndex: Int, endIndex: Int) {
    require(startIndex >= 0) { "The startIndex must be non-negative, was $startIndex" }
    require(endIndex <= array.size) { "The endIndex must be less than or equal to size, was $endIndex" }
    require(startIndex <= endIndex) { "Range of negative size is given: [$startIndex, $endIndex)" }
}
