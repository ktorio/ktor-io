package io.ktor.io;

import io.ktor.io.impl.*

public abstract class Buffer {
    public abstract var readIndex: Int
    public abstract var writeIndex: Int
    public abstract val capacity: Int

    public abstract operator fun get(index: Int): Byte
    public abstract operator fun set(index: Int, value: Byte)

    public abstract fun readByte(): Byte
    public abstract fun readShort(): Short
    public abstract fun readInt(): Int
    public abstract fun readLong(): Long
    public abstract fun writeByte(value: Byte)
    public abstract fun writeShort(value: Short)
    public abstract fun writeInt(value: Int)
    public abstract fun writeLong(value: Long)

    public abstract fun read(array: ByteArray, offset: Int = 0, count: Int = array.size)
    public abstract fun write(array: ByteArray, offset: Int = 0, count: Int = array.size)

    public abstract fun release()
}

public fun Buffer.canRead(): Boolean = readIndex < writeIndex
public fun Buffer.canWrite(): Boolean = writeIndex < capacity

public fun Buffer.readCapacity(): Int = writeIndex - readIndex
public fun Buffer.writeCapacity(): Int = capacity - writeIndex

public val EmptyBuffer: Buffer = ByteArrayBuffer(0)