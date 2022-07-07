package io.ktor.io

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

abstract class BufferTest {

    abstract fun createBuffer(): Buffer

    @Test
    fun testWriteCanReadByte() {
        val buffer = createBuffer()
        buffer.writeIndex = 0

        buffer.writeByte(99)
        buffer.writeByte(-99)

        assertEquals(0, buffer.readIndex)
        assertEquals(2, buffer.writeIndex)

        assertEquals(99, buffer.readByte())
        assertEquals(-99, buffer.readByte())

        assertEquals(2, buffer.readIndex)
        assertEquals(2, buffer.writeIndex)
    }

    @Test
    fun testWriteCanReadShort() {
        val buffer = createBuffer()
        buffer.writeIndex = 0

        buffer.writeShort(999)
        buffer.writeShort(-999)

        assertEquals(0, buffer.readIndex)
        assertEquals(4, buffer.writeIndex)

        assertEquals(999, buffer.readShort())
        assertEquals(-999, buffer.readShort())

        assertEquals(4, buffer.readIndex)
        assertEquals(4, buffer.writeIndex)
    }

    @Test
    fun testWriteCanReadInt() {
        val buffer = createBuffer()
        buffer.writeIndex = 0

        buffer.writeInt(999_999)
        buffer.writeInt(-999_999)

        assertEquals(0, buffer.readIndex)
        assertEquals(8, buffer.writeIndex)

        assertEquals(999_999, buffer.readInt())
        assertEquals(-999_999, buffer.readInt())

        assertEquals(8, buffer.readIndex)
        assertEquals(8, buffer.writeIndex)
    }

    @Test
    fun testWriteCanReadLong() {
        val buffer = createBuffer()
        buffer.writeIndex = 0

        buffer.writeLong(9_999_999_999_999)
        buffer.writeLong(-9_999_999_999_999)

        assertEquals(0, buffer.readIndex)
        assertEquals(16, buffer.writeIndex)

        assertEquals(9_999_999_999_999, buffer.readLong())
        assertEquals(-9_999_999_999_999, buffer.readLong())

        assertEquals(16, buffer.readIndex)
        assertEquals(16, buffer.writeIndex)
    }

    @Test
    fun testWriteCanReadArray() {
        val buffer = createBuffer()
        buffer.writeIndex = 0

        val array = ByteArray(123) { it.toByte() }
        buffer.copyFromByteArray(array)

        assertEquals(0, buffer.readIndex)
        assertEquals(123, buffer.writeIndex)

        val newArray = ByteArray(123).also { buffer.copyToByteArray(it) }
        assertContentEquals(array, newArray)

        assertEquals(123, buffer.readIndex)
        assertEquals(123, buffer.writeIndex)
    }

    @Test
    fun testBoundsByte() {
        val buffer = createBuffer()

        assertFailsWith<IndexOutOfBoundsException> { buffer.readByte() }
        buffer.writeIndex = buffer.capacity - 1
        buffer.readIndex = buffer.writeIndex
        buffer.writeByte(1)
        assertEquals(1, buffer.readByte())
        assertFailsWith<IndexOutOfBoundsException> { buffer.writeByte(2) }
    }

    @Test
    fun testBoundsShort() {
        val buffer = createBuffer()

        assertFailsWith<IndexOutOfBoundsException> { buffer.readShort() }
        buffer.writeIndex = buffer.capacity - 2
        buffer.readIndex = buffer.writeIndex
        buffer.writeShort(1)
        assertEquals(1, buffer.readShort())

        buffer.writeIndex = buffer.capacity - 1
        assertFailsWith<IndexOutOfBoundsException> { buffer.writeShort(2) }
    }

    @Test
    fun testBoundsInt() {
        val buffer = createBuffer()

        assertFailsWith<IndexOutOfBoundsException> { buffer.readInt() }
        buffer.writeIndex = buffer.capacity - 4
        buffer.readIndex = buffer.writeIndex
        buffer.writeInt(1)
        assertEquals(1, buffer.readInt())
        buffer.writeIndex = buffer.capacity - 3
        assertFailsWith<IndexOutOfBoundsException> { buffer.writeInt(2) }
    }

    @Test
    fun testBoundsLong() {
        val buffer = createBuffer()

        assertFailsWith<IndexOutOfBoundsException> { buffer.readLong() }
        buffer.writeIndex = buffer.capacity - 8
        buffer.readIndex = buffer.writeIndex
        buffer.writeLong(1)
        assertEquals(1, buffer.readLong())
        buffer.writeIndex = buffer.capacity - 7
        assertFailsWith<IndexOutOfBoundsException> { buffer.writeLong(2) }
    }

    @Test
    fun testBoundsArray() {
        val buffer = createBuffer()

        val array = ByteArray(123)
        var count = buffer.copyToByteArray(array)
        assertEquals(0, count)

        buffer.writeIndex = buffer.capacity - 123
        buffer.readIndex = buffer.writeIndex
        buffer.copyFromByteArray(array)

        val newArray = ByteArray(123)
        assertContentEquals(array, newArray)
        buffer.writeIndex = buffer.capacity - 122
        count = buffer.copyFromByteArray(array)
        assertEquals(122, count)
    }


    @Test
    fun testWriteCanRead() {
        val buffer = createBuffer()
        buffer.writeIndex = 0

        buffer.writeByte(99)
        buffer.writeShort(999)
        buffer.writeInt(999_999)
        buffer.writeLong(9_999_999_999_999)

        val array = ByteArray(123) { it.toByte() }
        buffer.copyFromByteArray(array)

        assertEquals(0, buffer.readIndex)
        assertEquals(138, buffer.writeIndex)

        assertEquals(99, buffer.readByte())
        assertEquals(999, buffer.readShort())
        assertEquals(999_999, buffer.readInt())
        assertEquals(9_999_999_999_999, buffer.readLong())
        val newArray = ByteArray(123).also { buffer.copyToByteArray(it) }
        assertContentEquals(array, newArray)

        assertEquals(138, buffer.readIndex)
        assertEquals(138, buffer.writeIndex)
    }
}