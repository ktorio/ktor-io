package io.ktor.io

public class CompositeBuffer internal constructor(
    buffers: MutableList<Buffer> = arrayListOf(),
    readIndex: Int = 0,
    writeIndex: Int = buffers.sumOf { it.availableForRead }
) : Buffer {
    private var buffers: MutableList<Buffer> = buffers

    override var capacity: Int = buffers.sumOf { it.availableForRead }
        private set

    override var readIndex: Int = readIndex

    override var writeIndex: Int = writeIndex

    override fun getByteAt(index: Int): Byte = bufferForIndex(index) { buffer, bufferIndex ->
        buffer.getByteAt(bufferIndex)
    }

    override fun setByteAt(index: Int, value: Byte) {
        bufferForIndex(index) { buffer, bufferIndex ->
            buffer.setByteAt(bufferIndex, value)
        }
    }

    override fun writeBufferAt(index: Int, value: Buffer): Int {
        var currentIndex = index

        while (value.isNotEmpty) {
            val bufferIndex = bufferIndex(currentIndex)
            if (bufferIndex < 0) break

            val buffer = buffers[bufferIndex]
            val written = buffer.writeBufferAt(bufferOffset(currentIndex), value)
            currentIndex += written
        }

        return currentIndex - index
    }

    override fun writeBuffer(value: Buffer): Int {
        val buffer = value.steal()
        val result = buffer.availableForRead
        buffers.add(buffer)
        capacity += buffer.availableForRead

        return result
    }

    override fun readToByteArrayAt(index: Int, destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        var currentIndex = index
        var destinationIndex = startIndex

        while (destinationIndex < endIndex) {
            val bufferIndex = bufferIndex(currentIndex)
            val bufferOffset = bufferOffset(currentIndex)
            if (bufferIndex < 0) break

            val buffer = buffers[bufferIndex]
            val written = buffer.writeByteArrayAt(bufferOffset, destination, destinationIndex, endIndex)

            destinationIndex += written
            currentIndex += written
        }

        return currentIndex - index
    }

    override fun readToByteArray(destination: ByteArray, startIndex: Int, endIndex: Int): Int {
        val count = readToByteArrayAt(readIndex, destination, startIndex, endIndex)
        readIndex += count
        return count
    }

    override fun steal(): Buffer {
        val buffer = CompositeBuffer(buffers, readIndex, writeIndex)

        buffers = arrayListOf()
        readIndex = 0
        writeIndex = 0
        return buffer
    }

    override fun close() {
        buffers.forEach {
            it.close()
        }
    }

    private fun bufferIndex(index: Int): Int = TODO()
    private fun bufferOffset(index: Int): Int = TODO()

    private inline fun <T> bufferForIndex(index: Int, block: (buffer: Buffer, bufferIndex: Int) -> T): T {
        val buffer = buffers[bufferIndex(index)]
        val offset = bufferOffset(index)
        return block(buffer, offset)
    }

}