package io.ktor.io

/**
 * Loads [Short] from the byte array at the specified [index].
 *
 * @throws IndexOutOfBoundsException if there are not enough bytes in the array.
 */
public fun ByteArray.loadShortAt(index: Int): Short {
    checkCanRead(index, 2, size)
    val high = rawByte(index).shl(8)
    val low = rawByte(index + 1)
    return high.or(low).toShort()
}

/**
 * Loads [Int] from the byte array at the specified [index].
 *
 * @throws IndexOutOfBoundsException if there are not enough bytes in the array.
 */
public fun ByteArray.loadIntAt(index: Int): Int {
    checkCanRead(index, 4, size)
    val first = rawByte(index).shl(24)
    val second = rawByte(index + 1).shl(16)
    val third = rawByte(index + 2).shl(8)
    val fourth = rawByte(index + 3)

    return first.or(second).or(third).or(fourth)
}

/**
 * Loads [Long] from the byte array at the specified [index].
 *
 * @throws IndexOutOfBoundsException if there are not enough bytes in the array.
 */
public fun ByteArray.loadLongAt(index: Int): Long {
    checkCanRead(index, 8, size)
    val first = rawByte(index).toLong().shl(56)
    val second = rawByte(index + 1).toLong().shl(48)
    val third = rawByte(index + 2).toLong().shl(40)
    val fourth = rawByte(index + 3).toLong().shl(32)
    val fifth = rawByte(index + 4).toLong().shl(24)
    val sixth = rawByte(index + 5).toLong().shl(16)
    val seventh = rawByte(index + 6).toLong().shl(8)
    val eighth = rawByte(index + 7).toLong()

    return first.or(second).or(third).or(fourth).or(fifth).or(sixth).or(seventh).or(eighth)
}

/**
 * Stores [value] to the byte array at the specified [index].
 *
 * @throws IndexOutOfBoundsException if there are not enough bytes in the array.
 */
public fun ByteArray.storeShortAt(index: Int, value: Short) {
    checkCanWrite(index, 2, size)

    val high = value.toInt().and(0xFFFF).shr(8).toByte()
    val low = value.toByte()
    this[index] = high
    this[index + 1] = low
}

/**
 * Stores [value] to the byte array at the specified [index].
 *
 * @throws IndexOutOfBoundsException if there are not enough bytes in the array.
 */
public fun ByteArray.storeIntAt(index: Int, value: Int) {
    checkCanWrite(index, 4, size)

    val first = value.shr(24).toByte()
    val second = value.shr(16).toByte()
    val third = value.shr(8).toByte()
    val fourth = value.toByte()

    this[index] = first
    this[index + 1] = second
    this[index + 2] = third
    this[index + 3] = fourth
}

/**
 * Stores [value] to the byte array at the specified [index].
 *
 * @throws IndexOutOfBoundsException if there are not enough bytes in the array.
 */
public fun ByteArray.storeLongAt(index: Int, value: Long) {
    checkCanWrite(index, 8, size)

    val first = value.shr(56).toByte()
    val second = value.shr(48).toByte()
    val third = value.shr(40).toByte()
    val fourth = value.shr(32).toByte()
    val fifth = value.shr(24).toByte()
    val sixth = value.shr(16).toByte()
    val seventh = value.shr(8).toByte()
    val eighth = value.toByte()

    this[index] = first
    this[index + 1] = second
    this[index + 2] = third
    this[index + 3] = fourth
    this[index + 4] = fifth
    this[index + 5] = sixth
    this[index + 6] = seventh
    this[index + 7] = eighth
}

internal fun checkCanRead(index: Int, count: Int, capacity: Int) {
    if (index + count > capacity) {
        throw IndexOutOfBoundsException("Can't read $count bytes at index $index from array of size $capacity")
    }
}

internal fun checkCanWrite(index: Int, count: Int, capacity: Int) {
    if (index + count > capacity) {
        throw IndexOutOfBoundsException("Can't write $count bytes at index $index to array of size $capacity")
    }
}

private fun ByteArray.rawByte(index: Int): Int = get(index).asInt()
private fun Byte.asInt(): Int = toInt() and 0xFF