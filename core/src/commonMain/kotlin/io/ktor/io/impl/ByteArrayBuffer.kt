package io.ktor.io.impl

import io.ktor.io.*
import io.ktor.io.utils.*

private const val DEFAULT_POOL_CAPACITY: Int = 2000
public const val DEFAULT_BUFFER_SIZE: Int = 1024 * 16

private val NoPool = object : NoPoolImpl<ByteArrayBuffer>() {
    override fun borrow(): ByteArrayBuffer {
        throw NotImplementedError()
    }
}

public val ByteArrayBuffersPool: ObjectPool<ByteArrayBuffer> = ByteArrayBufferPool()

public class ByteArrayBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
) : DefaultPool<ByteArrayBuffer>(capacity) {

    override fun produceInstance(): ByteArrayBuffer = ByteArrayBuffer(DEFAULT_BUFFER_SIZE, this)

    override fun clearInstance(instance: ByteArrayBuffer): ByteArrayBuffer {
        instance.reset()
        return instance
    }
}

public class ByteArrayBuffer(
    public override val capacity: Int,
    private val pool: ObjectPool<ByteArrayBuffer>
) : Buffer() {

    public constructor(capacity: Int) : this(capacity, NoPool)

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

    override fun read(buffer: Buffer) {
        if (platformRead(this, buffer)) {
            return
        }
        super.read(buffer)
    }

    override fun write(buffer: Buffer) {
        if (platformWrite(this, buffer)) {
            return
        }
        super.write(buffer)
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

internal expect fun platformRead(byteArrayBuffer: ByteArrayBuffer, buffer: Buffer): Boolean
internal expect fun platformWrite(byteArrayBuffer: ByteArrayBuffer, buffer: Buffer): Boolean
