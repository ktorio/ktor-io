package io.ktor.io.internal

import io.ktor.io.*

internal actual fun platformRead(
    byteArrayBuffer: ByteArrayBuffer,
    buffer: Buffer
): Int = Int.MIN_VALUE

internal actual fun platformWrite(
    byteArrayBuffer: ByteArrayBuffer,
    buffer: Buffer
): Int = Int.MIN_VALUE
