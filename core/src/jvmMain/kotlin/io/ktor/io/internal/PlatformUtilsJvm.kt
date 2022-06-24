package io.ktor.io.internal

import io.ktor.io.*

internal actual fun platformRead(
    byteArrayBuffer: ByteArrayBuffer,
    buffer: Buffer
): Int {
    if (buffer !is DefaultBuffer) return Int.MIN_VALUE

    return buffer.write(byteArrayBuffer)
}

internal actual fun platformWrite(
    byteArrayBuffer: ByteArrayBuffer,
    buffer: Buffer
): Int {
    if (buffer !is DefaultBuffer) return -1

    return buffer.read(byteArrayBuffer)
}
