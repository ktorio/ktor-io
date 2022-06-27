package io.ktor.io.internal

import io.ktor.io.*

public class CompositeBuffer(
    private val buffers: ArrayList<Buffer>
) : Buffer() {
    override var readIndex: Int = 0

    override var writeIndex: Int = 0

    override var capacity: Int = buffers.sumOf { it.capacity }
        private set

    override fun get(index: Int): Byte {
        check(index in 0..capacity)

        return bufferForIndex(index) { buffer, bufferIndex ->
            buffer[bufferIndex]
        }
    }

    override fun set(index: Int, value: Byte) {
        bufferForIndex(index) { buffer, bufferIndex ->
            buffer[bufferIndex] = value
        }

        throw IndexOutOfBoundsException("index: $index, capacity: $capacity")
    }

    override fun readByte(): Byte {
        val result = bufferForIndex(readIndex) { buffer, bufferIndex ->
            buffer[bufferIndex]
        }

        readIndex++

        return result
    }

    override fun readShort(): Short {
        TODO("")
    }

    override fun readInt(): Int {
        TODO("Not yet implemented")
    }

    override fun readLong(): Long {
        TODO("Not yet implemented")
    }

    override fun writeByte(value: Byte) {
        TODO("Not yet implemented")
    }

    override fun writeShort(value: Short) {
        TODO("Not yet implemented")
    }

    override fun writeInt(value: Int) {
        TODO("Not yet implemented")
    }

    override fun writeLong(value: Long) {
        TODO("Not yet implemented")
    }

    override fun read(destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        TODO("Not yet implemented")
    }

    override fun read(destination: Buffer): Int {
        TODO("Not yet implemented")
    }

    override fun write(source: ByteArray, startIndex: Int, endIndex: Int): Int {
        TODO("Not yet implemented")
    }

    override fun write(source: Buffer): Int {
        buffers.add(source)
        capacity += source.capacity
        return source.capacity
    }

    public fun appendBuffer(buffer: Buffer) {
        buffers.add(buffer)
        capacity += buffer.capacity
    }

    override fun compact() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    private inline fun <T> bufferForIndex(index: Int, block: (buffer: Buffer, bufferIndex: Int) -> T): T {
        var offset = index
        for (buffer in buffers) {
            if (offset < buffer.capacity) {
                return block(buffer, offset)
            }

            offset -= buffer.capacity
        }

        throw IndexOutOfBoundsException("index: $index, capacity: $capacity")
    }
}
