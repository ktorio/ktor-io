package io.ktor.io

class CompositeBufferDefaultTest : BufferTest() {
    override fun createBuffer(): CompositeBuffer = CompositeBuffer().also {
        it.appendBuffer(ByteArrayBuffer())
    }
}