package io.ktor.io

class ByteArrayBufferTest : BufferTest() {
    override fun createBuffer(capacity: Int): Buffer = ByteArrayBuffer(capacity)
}
