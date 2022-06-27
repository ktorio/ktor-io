package io.ktor.io

public interface SourceReader {

    public suspend fun peekByte(): Byte

    public suspend fun peekShort(): Short

    public suspend fun peekInt(): Int

    public suspend fun peekLong(): Long

    public suspend fun discard(count: Int)
}

public suspend fun SourceReader.peekBoolean(): Boolean = peekByte().toInt() != 0

public suspend fun SourceReader.readBoolean(): Boolean = readByte().toInt() != 0

public suspend fun SourceReader.readByte(): Byte {
    val result = peekByte()
    discard(1)
    return result
}

public suspend fun SourceReader.readShort(): Short {
    val result = peekShort()
    discard(2)
    return result
}

public suspend fun SourceReader.readInt(): Int {
    val result = peekInt()
    discard(4)
    return result
}

public suspend fun SourceReader.readFloat(): Float {
    val result = Float.fromBits(peekInt())
    discard(4)
    return result
}

public suspend fun SourceReader.peekDouble(): Double = Double.fromBits(peekLong())

public suspend fun SourceReader.readDouble(): Double {
    val result = peekDouble()
    discard(8)
    return result
}
