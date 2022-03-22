package io.ktor.io

import io.ktor.io.impl.*
import io.ktor.io.utils.*
import kotlin.test.Test
import kotlin.test.assertEquals

class BufferedBytesDestinationTest {

    @Test
    fun testWriteDoesNotFlush() {
        val destination = TestBytesDestination()
        val buffered = BufferedBytesDestination(destination, 1024)

        val buffer1 = ByteArrayBuffer(512)
        buffer1.write(ByteArray(512) { it.toByte() })

        val buffer2 = ByteArrayBuffer(1024)
        buffer2.write(ByteArray(1024) { it.toByte() })

        buffered.write(buffer1)
        buffered.write(buffer2)

        assertEquals(512, buffer1.readIndex)
        assertEquals(512, buffer2.readIndex)
        assertEquals(0, destination.writeCount)
    }

    @Test
    fun testAwaitFreeSpaceDoesNotWriteIfBufferIsNotFull() = testSuspend {
        val destination = TestBytesDestination()
        val buffered = BufferedBytesDestination(destination, 1024)

        val buffer1 = ByteArrayBuffer(1023)
        buffer1.write(ByteArray(1023) { it.toByte() })

        buffered.write(buffer1)
        buffered.awaitFreeSpace()

        assertEquals(0, destination.writeCount)
    }

    @Test
    fun testAwaitFreeSpaceWritesIfBufferIsFull() = testSuspend {
        val destination = TestBytesDestination()
        val buffered = BufferedBytesDestination(destination, 1024)

        val buffer1 = ByteArrayBuffer(1024)
        buffer1.write(ByteArray(1024) { it.toByte() })

        buffered.write(buffer1)
        buffered.awaitFreeSpace()

        assertEquals(1, destination.writeCount)
    }

    @Test
    fun testWriteByteFlushesIfBufferIsFull() = testSuspend {
        val destination = TestBytesDestination()
        val buffered = BufferedBytesDestination(destination, 1024)

        val buffer1 = ByteArrayBuffer(1023)
        buffer1.write(ByteArray(1023) { it.toByte() })

        buffered.write(buffer1)
        assertEquals(0, destination.writeCount)

        buffered.writeByte(1)
        assertEquals(1, destination.writeCount)
    }

    @Test
    fun testWriteByteDoesNotFlushIfBufferIsNotFull() = testSuspend {
        val destination = TestBytesDestination()
        val buffered = BufferedBytesDestination(destination, 1024)

        val buffer1 = ByteArrayBuffer(1022)
        buffer1.write(ByteArray(1022) { it.toByte() })

        buffered.write(buffer1)
        buffered.writeByte(1)
        assertEquals(0, destination.writeCount)
    }

    @Test
    fun testWriteShort() = testSuspend {
        val destination = TestBytesDestination()
        val buffered = BufferedBytesDestination(destination, 2)


        buffered.writeByte(1)
        buffered.writeShort(999)
        buffered.flush()

        assertEquals(2, destination.writeCount)

        val buffer1 = destination.buffers[0]
        val buffer2 = destination.buffers[1]
        buffer1.readByte()
        val value = Short(buffer1.readByte(), buffer2.readByte())
        assertEquals(999, value)
    }

    @Test
    fun testWriteInt() = testSuspend {
        val destination = TestBytesDestination()
        val buffered = BufferedBytesDestination(destination, 3)

        buffered.writeByte(1)
        buffered.writeInt(999999)
        buffered.flush()

        assertEquals(2, destination.writeCount)

        val buffer1 = destination.buffers[0]
        val buffer2 = destination.buffers[1]
        buffer1.readByte()
        val value = Int(buffer1.readShort(), buffer2.readShort())
        assertEquals(999999, value)
    }

    @Test
    fun testWriteLong() = testSuspend {
        val destination = TestBytesDestination()
        val buffered = BufferedBytesDestination(destination, 5)

        buffered.writeByte(1)
        buffered.writeLong(999999999)
        buffered.flush()

        assertEquals(2, destination.writeCount)

        val buffer1 = destination.buffers[0]
        val buffer2 = destination.buffers[1]
        buffer1.readByte()
        val value = Long(buffer1.readInt(), buffer2.readInt())
        assertEquals(999999999, value)
    }
}

class TestBytesDestination : BytesDestination() {
    internal val buffers = mutableListOf<Buffer>()
    internal val writeCount: Int get() = buffers.size

    override var closedCause: Throwable? = null

    override fun canWrite(): Boolean = closedCause != null

    override fun write(buffer: Buffer) {
        val copy = ByteArrayBuffer(buffer.readCapacity())
        val array = ByteArray(buffer.readCapacity())
        buffer.read(array)
        copy.write(array)
        buffers.add(copy)
        buffer.readIndex = buffer.writeIndex
    }

    override suspend fun flush() = Unit
    override suspend fun awaitFreeSpace() = Unit

    override fun close(cause: Throwable?) {
        closedCause = cause ?: ClosedDestinationException()
    }

    override fun close() {
        close(null)
    }

    class ClosedDestinationException() : IOException("closed")
}