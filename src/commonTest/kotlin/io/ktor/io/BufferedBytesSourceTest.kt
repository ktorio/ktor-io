package io.ktor.io

import io.ktor.io.utils.*
import kotlin.test.*

class BufferedBytesSourceTest {

    @Test
    fun testCanReadReadReturnsTrueWhenBufferHasContent() = testSuspend {
        val buffer1 = ByteArrayBuffer(1024)
        buffer1.writeByte(1)

        val source = TestBytesSource(buffer1)
        val buffered = BufferedBytesSource(source)

        assertTrue(buffered.canRead())
        buffered.read()
        assertTrue(buffered.canRead())
        buffered.readByte()
        assertFalse(buffered.canRead())
    }

    @Test
    fun testReadReturnsSameBufferIfHasContent() {
        val buffer1 = ByteArrayBuffer(1024)
        buffer1.copyFromByteArray(ByteArray(123) { it.toByte() })

        val buffer2 = ByteArrayBuffer(1024)
        buffer2.copyFromByteArray(ByteArray(123) { it.toByte() })

        val source = TestBytesSource(buffer1, buffer2)
        val buffered = BufferedBytesSource(source)

        val read1 = buffered.read()
        repeat(buffer1.writeIndex - 1) {
            buffer1.readByte()
        }
        val read2 = buffered.read()
        assertSame(read1, read2)

        read2.readByte()
        val read3 = buffered.read()
        assertNotSame(read2, read3)
    }

    @Test
    fun testReadByteWaitsUntilHasContent() = testSuspend {
        val buffer1 = ByteArrayBuffer(1024)
        val buffer2 = ByteArrayBuffer(1024)
        buffer2.writeByte(1)

        val source = TestBytesSource(buffer1, buffer2)
        val buffered = BufferedBytesSource(source)

        val value = buffered.readByte()
        assertEquals(1, value)
        assertEquals(2, source.readCount)
    }

    @Test
    fun testReadShort() = testSuspend {
        val buffer1 = ByteArrayBuffer(1)
        val buffer2 = ByteArrayBuffer(1)
        val value: Short = 999
        buffer1.writeByte(value.highByte)
        buffer2.writeByte(value.lowByte)

        val source = TestBytesSource(buffer1, buffer2)
        val buffered = BufferedBytesSource(source)

        assertEquals(999, buffered.readShort())
        assertEquals(2, source.readCount)
    }

    @Test
    fun testReadInt() = testSuspend {
        val buffer1 = ByteArrayBuffer(2)
        val buffer2 = ByteArrayBuffer(2)
        val value = 999
        buffer1.writeShort(value.highShort)
        buffer2.writeShort(value.lowShort)

        val source = TestBytesSource(buffer1, buffer2)
        val buffered = BufferedBytesSource(source)

        assertEquals(999, buffered.readInt())
        assertEquals(2, source.readCount)
    }

    @Test
    fun testReadLong() = testSuspend {
        val buffer1 = ByteArrayBuffer(4)
        val buffer2 = ByteArrayBuffer(4)
        val value = 999L
        buffer1.writeInt(value.highInt)
        buffer2.writeInt(value.lowInt)

        val source = TestBytesSource(buffer1, buffer2)
        val buffered = BufferedBytesSource(source)

        assertEquals(999, buffered.readLong())
        assertEquals(2, source.readCount)
    }

    @Test
    fun testReadLongAtOnce() = testSuspend {
        val buffer = ByteArrayBuffer(8)
        buffer.writeInt(Int.MAX_VALUE)
        buffer.writeInt(Int.MAX_VALUE)

        val source = TestBytesSource(buffer)
        val buffered = BufferedBytesSource(source)

        assertEquals(9223372034707292159, buffered.readLong())
    }
}
