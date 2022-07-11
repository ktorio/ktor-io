package io.ktor.io

import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import kotlin.test.assertEquals

class JvmBufferWithDefaultPoolTest : BufferTest() {
    override fun createBuffer(): Buffer = JvmBufferPool.Default.borrow()
}

class JvmBufferWithEmptyPoolTest : BufferTest() {
    override fun createBuffer(): Buffer = JvmBufferPool.Empty.borrow()
}

class JvmBufferTest {

    @Test
    fun testConstructorFromByteBuffer() {
        val data = ByteBuffer.allocateDirect(1024)
        data.position(21)
        data.limit(42)

        val buffer = JvmBuffer(data)
        assertEquals(21, buffer.readIndex)
        assertEquals(42, buffer.writeIndex)
    }

    @Test
    fun testConstructorFromPool() {
        val buffer = JvmBufferPool.Default.borrow()
        assertEquals(0, buffer.readIndex)
        assertEquals(0, buffer.writeIndex)
    }
}