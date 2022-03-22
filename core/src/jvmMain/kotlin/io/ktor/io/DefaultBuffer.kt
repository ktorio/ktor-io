package io.ktor.io

import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val DEFAULT_POOL_CAPACITY: Int = 2000
private const val DEFAULT_BUFFER_SIZE: Int = 1024 * 16

public val DefaultDirectByteBufferPool: ObjectPool<ByteBuffer> = DirectByteBufferPool()

public class DirectByteBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE
) : DefaultPool<ByteBuffer>(capacity) {

    override fun produceInstance(): ByteBuffer = ByteBuffer.allocateDirect(bufferSize)!!

    override fun clearInstance(instance: ByteBuffer): ByteBuffer = instance.apply {
        clear()
        order(ByteOrder.BIG_ENDIAN)
    }

    override fun validateInstance(instance: ByteBuffer) {
        check(instance.capacity() == bufferSize)
        check(instance.isDirect)
    }
}

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

    override fun read(array: ByteArray, offset: Int, count: Int) {
        buffer.get(array, offset, count)
    }

    override fun write(array: ByteArray, offset: Int, count: Int) {
        val oldPosition = buffer.position()
        val oldLimit = buffer.limit()
        buffer.position(oldLimit)
        buffer.limit(capacity)
        buffer.put(array, offset, count)
        readIndex = oldPosition
        writeIndex = oldLimit + count
    }

    override fun read(buffer: Buffer) {
        if (buffer is DefaultBuffer) {
            buffer.buffer.put(this.buffer)
            return
        }
        super.read(buffer)
    }

    override fun write(buffer: Buffer) {
        if (buffer is DefaultBuffer) {
            this.buffer.put(buffer.buffer)
            return
        }
        super.write(buffer)
    }

    override fun release() {
        pool.recycle(buffer)
    }

    override fun compact() {
        buffer.compact()
    }
}