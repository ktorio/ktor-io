package io.ktor.io;

abstract class Buffer {
    abstract var readIndex: Int
    abstract var writeIndex: Int
    abstract val capacity: Int

    abstract operator fun get(index: Int): Byte
    abstract operator fun set(index: Int, value: Byte)

    abstract fun readByte(): Byte
    abstract fun readShort(): Short
    abstract fun readInt(): Int
    abstract fun readLong(): Long
    abstract fun writeByte(value: Byte)
    abstract fun writeShort(value: Short)
    abstract fun writeInt(value: Int)
    abstract fun writeLong(value: Long)
}