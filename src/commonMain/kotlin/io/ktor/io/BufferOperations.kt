package io.ktor.io

import io.ktor.io.*

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
