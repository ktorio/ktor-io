package io.ktor.io

import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DefaultBuffetTest {

    private val noPool = object : NoPoolImpl<ByteBuffer>() {
        override fun borrow(): ByteBuffer {
            throw NotImplementedError()
        }
    }

    @Test
    fun testWriteCanRead() {
        val buffer = DefaultBuffer(ByteBuffer.allocate(1024).clear(), noPool)
        buffer.writeIndex = 0

        buffer.writeByte(99)
        buffer.writeShort(999)
        buffer.writeInt(999_999)
        buffer.writeLong(9_999_999_999_999)

        val array = ByteArray(123) { it.toByte() }
        buffer.write(array)

        assertEquals(0, buffer.readIndex)
        assertEquals(138, buffer.writeIndex)

        assertEquals(99, buffer.readByte())
        assertEquals(999, buffer.readShort())
        assertEquals(999_999, buffer.readInt())
        assertEquals(9_999_999_999_999, buffer.readLong())
        val newArray = ByteArray(123).also { buffer.read(it) }
        assertContentEquals(array, newArray)

        assertEquals(138, buffer.readIndex)
        assertEquals(138, buffer.writeIndex)
    }
}