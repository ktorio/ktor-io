package io.ktor.io.internal

import io.ktor.io.*

internal actual fun platformRead(
    byteArrayBuffer: ByteArrayBuffer,
    buffer: Buffer
): Boolean {
    if (buffer !is DefaultBuffer) return false

    buffer.write(byteArrayBuffer)
    return true
}

internal actual fun platformWrite(
    byteArrayBuffer: ByteArrayBuffer,
    buffer: Buffer
): Boolean {
    if (buffer !is DefaultBuffer) return false

    buffer.read(byteArrayBuffer)
    return true
}
