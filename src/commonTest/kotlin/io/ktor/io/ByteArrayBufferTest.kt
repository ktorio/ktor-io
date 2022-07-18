@file:Suppress("UNCHECKED_CAST")

package io.ktor.io

import io.ktor.io.utils.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ByteArrayBufferWithDefaultPoolTest : BufferTest() {
    override val pool: ObjectPool<Buffer> = ByteArrayBufferPool.Default as ObjectPool<Buffer>
}

class ByteArrayBufferWithEmptyPoolTest : BufferTest() {
    override val pool: ObjectPool<Buffer> = ByteArrayBufferPool.Empty as ObjectPool<Buffer>
}

class ByteArrayBufferWithHeadTest : BufferTest() {
    override val pool: ObjectPool<Buffer> = ByteArrayBufferPool.Empty as ObjectPool<Buffer>

    override fun createBuffer(): Buffer = pool.borrow().takeHead(1024)
}

class ByteArrayBufferWithTailTest : BufferTest() {
    override val pool: ObjectPool<Buffer> = ByteArrayBufferPool.Empty as ObjectPool<Buffer>

    override fun createBuffer(): Buffer {
        val buffer = pool.borrow()
        buffer.takeHead(1024)
        return buffer
    }
}

class LeakCheckBufferTest : BufferTest() {
    override val pool: ObjectPool<Buffer> = LeakDetectingPool() as ObjectPool<Buffer>
}

class ByteArrayBufferTest {
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
