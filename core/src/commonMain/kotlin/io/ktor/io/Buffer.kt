package io.ktor.io

public abstract class Buffer {
    public abstract var readIndex: Int
    public abstract var writeIndex: Int
    public abstract val capacity: Int

    public abstract operator fun get(index: Int): Byte
    public abstract operator fun set(index: Int, value: Byte)

    public open fun readBoolean(): Boolean = readByte() != 0.toByte()
    public abstract fun readByte(): Byte
    public abstract fun readShort(): Short
    public abstract fun readInt(): Int
    public open fun readFloat(): Float = Float.fromBits(readInt())
    public abstract fun readLong(): Long
    public open fun readDouble(): Double = Double.fromBits(readLong())

    public open fun writeBoolean(value: Boolean) {
        writeByte(if (value) 1 else 0)
    }

    public abstract fun writeByte(value: Byte)
    public abstract fun writeShort(value: Short)
    public abstract fun writeInt(value: Int)
    public open fun writeFloat(value: Float) {
        writeInt(value.toRawBits())
    }
    public abstract fun writeLong(value: Long)
    public open fun writeDouble(value: Double) {
        writeLong(value.toRawBits())
    }

    public abstract fun read(destination: ByteArray, startIndex: Int = 0, endIndex: Int = destination.size): Int
    public abstract fun write(source: ByteArray, startIndex: Int = 0, endIndex: Int = source.size): Int

    public abstract fun read(destination: Buffer): Int
    public abstract fun write(source: Buffer): Int

    public abstract fun release()
    public abstract fun compact()

    public companion object {
        public val Empty: Buffer = ByteArrayBuffer(0)
    }
}

public fun Buffer.canRead(): Boolean = readIndex < writeIndex
public fun Buffer.canWrite(): Boolean = writeIndex < capacity

public fun Buffer.readCapacity(): Int = writeIndex - readIndex
public fun Buffer.writeCapacity(): Int = capacity - writeIndex

public fun Buffer.reset() {
    readIndex = 0
    writeIndex = 0
}
