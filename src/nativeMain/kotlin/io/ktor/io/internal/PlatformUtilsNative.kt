package io.ktor.io.internal

import io.ktor.io.*

internal actual fun platformRead(
    byteArrayBuffer: ByteArrayBuffer,
    buffer: Buffer
): Boolean = false

internal actual fun platformWrite(
    byteArrayBuffer: ByteArrayBuffer,
    buffer: Buffer
): Boolean = false