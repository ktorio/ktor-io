package io.ktor.io;

import io.ktor.io.impl.*
import kotlin.math.min

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

    public open fun read(buffer: Buffer) {
        val toRead = min(readCapacity(), buffer.writeCapacity())
        when (buffer) {
            is ByteArrayBuffer -> {
                read(buffer.array, buffer.writeIndex, toRead)
                buffer.writeIndex += toRead
            }
            else -> {
                repeat(toRead) {
                    buffer.writeByte(readByte())
                }
            }
        }
    }

    public open fun write(buffer: Buffer) {
        val toWrite = min(writeCapacity(), buffer.readCapacity())
        when (buffer) {
            is ByteArrayBuffer -> {
                write(buffer.array, buffer.readIndex, toWrite)
                buffer.readIndex += toWrite
            }
            else -> {
                repeat(toWrite) {
                    writeByte(buffer.readByte())
                }
            }
        }
    }

    public abstract fun release()
    public abstract fun compact()
}

public fun Buffer.canRead(): Boolean = readIndex < writeIndex
public fun Buffer.canWrite(): Boolean = writeIndex < capacity

public fun Buffer.readCapacity(): Int = writeIndex - readIndex
public fun Buffer.writeCapacity(): Int = capacity - writeIndex

public fun Buffer.reset() {
    readIndex = 0
    writeIndex = 0
}

public val EmptyBuffer: Buffer = ByteArrayBuffer(0)