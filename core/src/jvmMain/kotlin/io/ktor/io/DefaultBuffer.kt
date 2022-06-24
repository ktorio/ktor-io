package io.ktor.io

import java.lang.Integer.min
import java.nio.ByteBuffer

public val BufferPool: ObjectPool<DefaultBuffer> = DefaultBufferPool()

public class DefaultBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
) : DefaultPool<DefaultBuffer>(capacity) {

    override fun produceInstance(): DefaultBuffer =
        DefaultBuffer(DefaultDirectByteBufferPool.borrow(), DefaultDirectByteBufferPool)
}

public class DefaultBuffer(internal val buffer: ByteBuffer, private val pool: ObjectPool<ByteBuffer>) : Buffer() {

    override var readIndex: Int
        get() = buffer.position()
        set(value) {
            buffer.position(value)
        }

    override var writeIndex: Int
        get() = buffer.limit()
        set(value) {
            buffer.limit(value)
        }

    override val capacity: Int
        get() = buffer.capacity()

    override fun get(index: Int): Byte {
        return buffer.get(index)
    }

    override fun set(index: Int, value: Byte) {
        buffer.put(index, value)
    }

    override fun readByte(): Byte {
        return buffer.get()
    }

    override fun readShort(): Short {
        return buffer.short
    }

    override fun readInt(): Int {
        return buffer.int
    }

    override fun readLong(): Long {
        return buffer.long
    }

    override fun writeByte(value: Byte) {
        val oldLimit = buffer.limit()
        buffer.limit(capacity)
        buffer.put(oldLimit, value)
        writeIndex = oldLimit + 1
    }

    override fun writeShort(value: Short) {
        val oldLimit = buffer.limit()
        buffer.limit(capacity)
        buffer.putShort(oldLimit, value)
        writeIndex = oldLimit + 2
    }

    override fun writeInt(value: Int) {
        val oldLimit = buffer.limit()
        buffer.limit(capacity)
        buffer.putInt(oldLimit, value)
        writeIndex = oldLimit + 4
    }

    override fun writeLong(value: Long) {
        val oldLimit = buffer.limit()
        buffer.limit(capacity)
        buffer.putLong(oldLimit, value)
        writeIndex = oldLimit + 8
    }

    override fun read(destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startIndex should be non-negative: $startIndex" }
        require(startIndex <= endIndex) { "startIndex should be less than or equal to endIndex: $startIndex, $endIndex" }
        require(endIndex <= destination.size) { "endIndex should be less than or equal to destination.size: $endIndex, ${destination.size}" }

        val count = min(endIndex - startIndex, buffer.remaining())
        buffer.get(destination, startIndex, count)
        return count
    }

    override fun write(source: ByteArray, startIndex: Int, endIndex: Int): Int {
        require(startIndex >= 0) { "startIndex should be non-negative: $startIndex" }
        require(startIndex <= endIndex) { "startIndex should be less than or equal to endIndex: $startIndex, $endIndex" }
        require(endIndex <= source.size) { "endIndex should be less than or equal to source.size: $endIndex, ${source.size}" }

        val count = min(endIndex - startIndex, capacity - writeIndex)

        val storedReadIndex = readIndex
        val storedWriteIndex = writeIndex

        buffer.position(buffer.limit())
        buffer.limit(buffer.capacity())
        buffer.put(source, startIndex, count)

        readIndex = storedReadIndex
        writeIndex = storedWriteIndex + count
        return count
    }

    override fun read(destination: Buffer): Int {
        if (destination is DefaultBuffer) {
            val result = buffer.remaining()
            val output = destination.buffer
            check(result <= output.remaining())
            output.put(buffer)
            return result
        }

        var result = 0
        while (canRead() && destination.canWrite()) {
            destination.writeByte(readByte())
            result++
        }

        return result
    }

    override fun write(source: Buffer): Int {
        if (source is DefaultBuffer) {
            val sourceBuffer = source.buffer
            val result = sourceBuffer.remaining()
            check(result >= buffer.remaining())
            buffer.put(sourceBuffer)
            return result
        }

        var result = 0
        while (canWrite() && source.canRead()) {
            writeByte(source.readByte())
            result++
        }

        return result
    }

    override fun release() {
        pool.recycle(buffer)
    }

    override fun compact() {
        buffer.compact()
    }
}
