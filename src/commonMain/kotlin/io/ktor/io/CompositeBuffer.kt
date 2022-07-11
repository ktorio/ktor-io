package io.ktor.io

/**
 * The [CompositeBuffer] is a [Buffer] which is build on top of multiple [Buffer]s. In the result it has a flexible
 * [capacity].
 *
 * In advance to the default [Buffer] methods, [CompositeBuffer] provides an [appendBuffer].
 */
public open class CompositeBuffer(
    buffers: List<Buffer> = emptyList(),
    override var readIndex: Int = 0,
    override var writeIndex: Int = buffers.sumOf { it.availableForRead }
) : Buffer {
    private val _buffers: MutableList<Buffer> = mutableListOf<Buffer>().apply {
        addAll(buffers)
    }

    /**
     * Buffers inside the [CompositeBuffer].
     */
    public val buffers: List<Buffer> get() = _buffers

    override var capacity: Int = buffers.sumOf { it.availableForRead } + (buffers.lastOrNull()?.availableForWrite ?: 0)
        protected set

    override fun getByteAt(index: Int): Byte {
        ensureCanRead(index, 1)
        val buffer = bufferForIndex(index)
        val bufferIndex = indexInBuffer(index)
        return buffer.getByteAt(bufferIndex)
    }

    override fun setByteAt(index: Int, value: Byte) {
        ensureCanWrite(index, 1)

        val buffer = bufferForIndex(index)
        val indexInBuffer = indexInBuffer(index)
        buffer.setByteAt(indexInBuffer, value)
    }

    override fun setShortAt(index: Int, value: Short) {
        val buffer = bufferForIndex(index)
        if (currentCapacity() >= 2) {
            buffer.setShortAt(indexInBuffer(index), value)
            return
        }

        setByteAt(index, value.highByte)
        setByteAt(index + 1, value.lowByte)
    }

    override fun getShortAt(index: Int): Short {
        val buffer = bufferForIndex(index)
        if (currentCapacity() >= 2) {
            return buffer.getShortAt(indexInBuffer(index))
        }

        val highByte = getByteAt(index)
        val lowByte = getByteAt(index + 1)
        return Short(highByte, lowByte)
    }

    override fun steal(): Buffer = CompositeBuffer(
        buffers.map { it.steal() },
        readIndex,
        writeIndex
    )

    /**
     * Appends a [Buffer] to the end of the [CompositeBuffer].
     */
    public open fun appendBuffer(buffer: Buffer) {
        if (buffer.availableForRead == 0) {
            _buffers.add(buffer)
            capacity += buffer.capacity
            return
        }

        val unusedCapacity = _buffers.lastOrNull()?.availableForWrite ?: 0
        val newBufferCapacity = buffer.capacity - buffer.readIndex
        val capacityChange = newBufferCapacity - unusedCapacity

        _buffers.add(buffer)
        capacity += capacityChange
    }

    private var bufferIndex = 0
    private var bufferOffset = 0

    private fun bufferForIndex(index: Int): Buffer {
        while (index >= bufferOffset + currentCapacity()) {
            nextBuffer()
        }

        while (index < bufferOffset) {
            previousBuffer()
        }

        return _buffers[bufferIndex]
    }

    private fun indexInBuffer(index: Int): Int = index - bufferOffset + buffers[bufferIndex].readIndex

    private fun nextBuffer() {
        if (bufferIndex == buffers.lastIndex) return

        bufferOffset += buffers[bufferIndex].availableForRead
        bufferIndex++
    }

    private fun previousBuffer() {
        if (bufferIndex == 0) return

        bufferIndex--
        bufferOffset -= buffers[bufferIndex].availableForRead
    }

    private fun currentCapacity(): Int {
        val buffer = buffers[bufferIndex]

        return if (bufferIndex == buffers.lastIndex) {
            buffer.capacity - buffer.readIndex
        } else {
            buffer.availableForRead
        }
    }

    override fun close() {
        buffers.forEach { it.close() }
    }
}
