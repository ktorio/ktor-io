package io.ktor.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CompositeBufferTest {

    @Test
    fun testEmpty() {
        val empty = CompositeBuffer()

        assertEquals(0, empty.capacity)
        assertEquals(0, empty.availableForWrite)
        assertEquals(0, empty.availableForRead)

        assertFailsWith<IndexOutOfBoundsException> { empty[0] }
        assertFailsWith<IndexOutOfBoundsException> { empty.readByte() }
        assertFailsWith<IndexOutOfBoundsException> { empty.writeByte(42) }
    }

    @Test
    fun testCompositeBufferWithArray() {
        val buffer = CompositeBuffer().apply {
            appendBuffer(ByteArrayBuffer())
            appendBuffer(ByteArrayBuffer())
        }

        assertEquals(0, buffer.readIndex)
        assertEquals(0, buffer.writeIndex)
        assertEquals(DEFAULT_BUFFER_SIZE, buffer.capacity)
    }

    @Test
    fun testBufferWithArrayWriteAndReadByte() {
        val buffer = CompositeBuffer().apply {
            appendBuffer(ByteArrayBuffer())
        }

        buffer.writeByte(42)
        assertEquals(42, buffer.readByte())
    }

    @Test
    fun testWriteShortOnBorder() {
        val buffer = CompositeBuffer().apply {
            appendBuffer(ByteArrayBuffer(1))
            writeByte(0)
            appendBuffer(ByteArrayBuffer(1))
            writeByte(0)

            writeIndex = 0
            writeShort(42)
        }

        assertEquals(2, buffer.buffers.size)
        assertEquals(42, buffer.readShort())
    }

    @Test
    fun testSetShortOnBorder() {
        val buffer = CompositeBuffer().apply {
            appendBuffer(ByteArrayBuffer(1))
            writeByte(42)
            appendBuffer(ByteArrayBuffer(1))
            writeByte(42)

            setShortAt(0, -42)
        }

        assertEquals(2, buffer.buffers.size)
        assertEquals(-42, buffer.readShort())
    }
}