@file:Suppress("UNCHECKED_CAST")

package io.ktor.io

import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import kotlin.test.assertEquals

class JvmBufferWithDefaultPoolTest : BufferTest() {
    override val pool: ObjectPool<Buffer> = JvmBufferPool.Default as ObjectPool<Buffer>
}

class JvmBufferWithEmptyPoolTest : BufferTest() {
    override val pool: ObjectPool<Buffer> = JvmBufferPool.Empty as ObjectPool<Buffer>
}

class JvmBufferWithHeadTest : BufferTest() {
    override val pool: ObjectPool<Buffer> = JvmBufferPool.Empty as ObjectPool<Buffer>
    override fun createBuffer(): Buffer = pool.borrow().takeHead(1024)
}

class JvmBufferWithTailTest : BufferTest() {
    override val pool: ObjectPool<Buffer> = JvmBufferPool.Empty as ObjectPool<Buffer>

    override fun createBuffer(): Buffer = pool.borrow().apply {
        takeHead(1024)
    }
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
        val buffer = JvmBuffer()
        assertEquals(0, buffer.readIndex)
        assertEquals(0, buffer.writeIndex)
    }
}
