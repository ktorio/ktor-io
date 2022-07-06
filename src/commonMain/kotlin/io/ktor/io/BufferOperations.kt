package io.ktor.io

public val Buffer.isEmpty: Boolean get() = availableForRead == 0
public val Buffer.isNotEmpty: Boolean get() = !isEmpty

public val Buffer.isFull: Boolean get() = availableForWrite == 0
public val Buffer.isNotFull: Boolean get() = !isFull

public val Buffer.availableForRead: Int get() = writeIndex - readIndex
public val Buffer.availableForWrite: Int get() = capacity - writeIndex

public fun Buffer.resetForWrite() {
    readIndex = 0
    writeIndex = 0
}

public operator fun Buffer.get(index: Int): Byte = getByteAt(index)

public operator fun Buffer.set(index: Int, value: Byte) {
    setByteAt(index, value)
}

/**
 * Check if the Buffer has [count] bytes to read.
 *
 * @throws IndexOutOfBoundsException if the [count] is greater [availableForRead].
 */
public fun Buffer.ensureCanRead(count: Int) {
    if (availableForRead < count) {
        throw IndexOutOfBoundsException("Can't read $count bytes. Available: $availableForRead.")
    }
}

/**
 * Check if the Buffer has space to write [count] bytes.
 *
 * @throws IndexOutOfBoundsException if the [count] is greater [availableForWrite].
 */
public fun Buffer.ensureCanWrite(count: Int) {
    if (availableForWrite < count) {
        throw IndexOutOfBoundsException("Can't write $count bytes. Available space: $availableForWrite.")
    }
}