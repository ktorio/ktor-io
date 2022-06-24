package io.ktor.io.internal

import io.ktor.io.*

internal expect fun platformRead(byteArrayBuffer: ByteArrayBuffer, buffer: Buffer): Int

internal expect fun platformWrite(byteArrayBuffer: ByteArrayBuffer, buffer: Buffer): Int
