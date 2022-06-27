package io.ktor.io

public val Buffer.availableForRead: Int get() = writeIndex - readIndex

public val Buffer.availableForWrite: Int get() = capacity - writeIndex

public val Buffer.isEmpty: Boolean get() = availableForRead == 0

public val Buffer.isNotEmpty: Boolean get() = availableForRead > 0

public val Buffer.isFull: Boolean get() = availableForWrite == 0

public val Buffer.isNotFull: Boolean get() = availableForWrite > 0

public fun Buffer.reset() {
    readIndex = 0
    writeIndex = 0
}
