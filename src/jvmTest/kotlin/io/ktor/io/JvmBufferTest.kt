package io.ktor.io

import io.ktor.io.*

class JvmBufferTest : BufferTest() {
    override fun createBuffer(capacity: Int): Buffer = JvmBuffer(capacity)
}