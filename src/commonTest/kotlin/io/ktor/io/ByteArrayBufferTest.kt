package io.ktor.io

import kotlin.test.Test
import kotlin.test.assertEquals

class ByteArrayWithDefaultPoolTest : BufferTest() {
    override fun createBuffer(): Buffer = ByteArrayBufferPool.Default.borrow()
}

class ByteArrayWithEmptyPoolTest : BufferTest() {
    override fun createBuffer(): Buffer = ByteArrayBufferPool.Empty.borrow()
}

class ByteArrayTest {
    @Test
    fun testConstructorFromArray() {
        val array = ByteArray(10)
        val buffer = ByteArrayBuffer(array)

        assertEquals(0, buffer.readIndex)
        assertEquals(array.size, buffer.writeIndex)
    }

    @Test
    fun testConstructorFromPool() {
        val buffer = ByteArrayBuffer()
        assertEquals(0, buffer.readIndex)
        assertEquals(0, buffer.writeIndex)
    }
}
