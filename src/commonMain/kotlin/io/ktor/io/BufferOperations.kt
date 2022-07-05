package io.ktor.io

public val Buffer.canRead: Boolean get() = readIndex < writeIndex
public val Buffer.canWrite: Boolean get() = writeIndex < capacity

public val Buffer.availableForRead: Int get() = writeIndex - readIndex
public val Buffer.availableForWrite: Int get() = capacity - writeIndex

public fun Buffer.reset() {
    readIndex = 0
    writeIndex = 0
}

public operator fun Buffer.get(index: Int): Byte = loadByteAt(index)

public operator fun Buffer.set(index: Int, value: Byte) {
    storeByteAt(index, value)
}

/**
 * Check if the Buffer has [count] bytes to read.
 *
 * @throws IndexOutOfBoundsException if the [count] is greater [availableForRead].
 */
public fun Buffer.checkCanRead(count: Int) {
    if (availableForRead < count) {
        throw IndexOutOfBoundsException("Can't read $count bytes. Available: $availableForRead.")
    }
}

/**
 * Check if the Buffer has space to write [count] bytes.
 *
 * @throws IndexOutOfBoundsException if the [count] is greater [availableForWrite].
 */
public fun Buffer.checkCanWrite(count: Int) {
    if (availableForWrite < count) {
        throw IndexOutOfBoundsException("Can't write $count bytes. Available space: $availableForWrite.")
    }
}