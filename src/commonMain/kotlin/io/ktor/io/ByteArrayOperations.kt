package io.ktor.io

/**
 * Loads [Short] from the byte array at the specified [index].
 *
 * @throws IndexOutOfBoundsException if there are not enough bytes in the array.
 */
public fun ByteArray.getShortAt(index: Int): Short {
    ensureCanRead(index, 2, size)
    return Short(this[index], this[index + 1])
}

/**
 * Loads [Int] from the byte array at the specified [index].
 *
 * @throws IndexOutOfBoundsException if there are not enough bytes in the array.
 */
public fun ByteArray.getIntAt(index: Int): Int {
    ensureCanRead(index, 4, size)

    return Int(getShortAt(index), getShortAt(index + 2))
}

/**
 * Loads [Long] from the byte array at the specified [index].
 *
 * @throws IndexOutOfBoundsException if there are not enough bytes in the array.
 */
public fun ByteArray.getLongAt(index: Int): Long {
    ensureCanRead(index, 8, size)
    return Long(getIntAt(index), getIntAt(index + 4))
}

/**
 * Stores [value] to the byte array at the specified [index].
 *
 * @throws IndexOutOfBoundsException if there are not enough bytes in the array.
 */
public fun ByteArray.setShortAt(index: Int, value: Short): ByteArray {
    ensureCanWrite(index, 2, size)

    this[index] = value.highByte
    this[index + 1] = value.lowByte

    return this
}

/**
 * Stores [value] to the byte array at the specified [index].
 *
 * @throws IndexOutOfBoundsException if there are not enough bytes in the array.
 */
public fun ByteArray.setIntAt(index: Int, value: Int): ByteArray {
    ensureCanWrite(index, 4, size)

    setShortAt(index, value.highShort)
    setShortAt(index + 2, value.lowShort)

    return this
}

/**
 * Stores [value] to the byte array at the specified [index].
 *
 * @throws IndexOutOfBoundsException if there are not enough bytes in the array.
 */
public fun ByteArray.setLongAt(index: Int, value: Long): ByteArray {
    ensureCanWrite(index, 8, size)

    setIntAt(index, value.highInt)
    setIntAt(index + 4, value.lowInt)

    return this
}

internal fun ensureCanRead(index: Int, count: Int, capacity: Int) {
    if (index + count > capacity) {
        throw IndexOutOfBoundsException("Can't read $count bytes at index $index from array of size $capacity")
    }
}

internal fun ensureCanWrite(index: Int, count: Int, capacity: Int) {
    if (index + count > capacity) {
        throw IndexOutOfBoundsException("Can't write $count bytes at index $index to array of size $capacity")
    }
}

private fun Byte.asInt(): Int = toInt() and 0xFF