package io.ktor.io;

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
}