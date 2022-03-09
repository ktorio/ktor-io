package io.ktor.io;

abstract class Buffer {
    abstract var readIndex: Int
    abstract var writeIndex: Int
    abstract val capacity: Int

    abstract operator fun get(index: Int): Byte
    abstract operator fun set(index: Int, value: Byte)

    abstract fun read[Byte, Short, Int, Long](): [Type]
    abstract fun write[Byte, Short, Int, Long](value: Type)
}