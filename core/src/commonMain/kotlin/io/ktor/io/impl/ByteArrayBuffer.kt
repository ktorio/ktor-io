package io.ktor.io.impl

import io.ktor.io.*
import io.ktor.io.utils.*

public class ByteArrayBuffer(public override val capacity: Int) : Buffer() {

    private val array = ByteArray(capacity)

    override var readIndex: Int = 0
    override var writeIndex: Int = 0

    override fun get(index: Int): Byte {
        return array[index]
    }

    override fun set(index: Int, value: Byte) {
        array[index] = value
    }

    override fun readByte(): Byte {
        checkReadBounds(1)
        return array[readIndex++]
    }

    override fun readShort(): Short {
        checkReadBounds(2)
        return doReadShort()
    }

    override fun readInt(): Int {
        checkReadBounds(4)
        return doReadInt()
    }

    override fun readLong(): Long {
        checkReadBounds(8)
        return doReadLong()
    }

    override fun writeByte(value: Byte) {
        checkWriteBounds(1)
        doWriteByte(value)
    }

    override fun writeShort(value: Short) {
        checkWriteBounds(2)
        doWriteShort(value)
    }

    override fun writeInt(value: Int) {
        checkWriteBounds(4)
        doWriteInt(value)
    }

    override fun writeLong(value: Long) {
        checkWriteBounds(8)
        doWriteLong(value)
    }

    override fun read(array: ByteArray, offset: Int, count: Int) {
        checkReadBounds(count)
        this.array.copyInto(array, offset, readIndex, readIndex + count)
        readIndex += count
    }

    override fun write(array: ByteArray, offset: Int, count: Int) {
        checkWriteBounds(count)
        array.copyInto(this.array, writeIndex, offset, offset + count)
        writeIndex += count
    }

    override fun release() {}

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

    private fun doReadShort() = readByte().asHighByte(readByte())

    private fun doReadInt() = doReadShort().asHughShort(doReadShort())

    private fun doReadLong() = doReadInt().asHighInt(doReadInt())

    private fun checkReadBounds(count: Int) {
        if (readIndex + count > writeIndex) {
            throw IndexOutOfBoundsException(
                "Read overflow, " +
                        "trying to read $count bytes " +
                        "from buffer with ${writeIndex - 1} bytes " +
                        "and readIndex at $readIndex"
            )
        }
    }

    private fun checkWriteBounds(count: Int) {
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
