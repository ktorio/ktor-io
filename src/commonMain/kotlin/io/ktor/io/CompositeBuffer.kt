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
    override var writeIndex: Int = buffers.sumOf { it.availableForRead },
    public val pool: ObjectPool<out Buffer> = ByteArrayBufferPool.Default
) : Buffer {
    private val _buffers: ArrayList<Buffer> = ArrayList(buffers.size)

    override var capacity: Int = 0
        protected set

    init {
        buffers.forEach {
            val buffer = it.steal()
            capacity += buffer.availableForRead
            _buffers.add(buffer)
        }
    }

    /**
     * Buffers inside the [CompositeBuffer].
     */
    public val buffers: List<Buffer> get() = _buffers

    override fun getByteAt(index: Int): Byte {
        checkCanRead(index, 1)

        return locate(index) { offset, _ ->
            getByteAt(offset)
        }
    }

    override fun setByteAt(index: Int, value: Byte) {
        checkCanWrite(index, 1)

        locate(index) { offset, _ ->
            setByteAt(offset, value)
        }
    }

    override fun getShortAt(index: Int): Short {
        checkCanRead(index, 2)

        locate(index) { offset, capacity ->
            if (offset + 2 < capacity) return getShortAt(offset)
        }

        return Short(getByteAt(index), getByteAt(index + 1))
    }

    override fun setShortAt(index: Int, value: Short) {
        checkCanWrite(index, 2)

        locate(index) { offset, capacity ->
            if (offset + 2 < capacity) {
                return setShortAt(offset, value)
            }
        }

        setByteAt(index, value.highByte)
        setByteAt(index + 1, value.lowByte)
    }

    override fun getIntAt(index: Int): Int {
        checkCanRead(index, 4)

        locate(index) { offset, capacity ->
            if (offset + 4 < capacity) return getIntAt(offset)
        }

        return Int(getShortAt(index), getShortAt(index + 2))
    }

    override fun setIntAt(index: Int, value: Int) {
        checkCanWrite(index, 4)

        locate(index) { offset, capacity ->
            if (offset + 4 < capacity) return setIntAt(offset, value)
        }

        setShortAt(index, value.highShort)
        setShortAt(index + 2, value.lowShort)
    }

    override fun getLongAt(index: Int): Long {
        checkCanRead(index, 8)

        locate(index) { offset, capacity ->
            if (offset + 8 < capacity) return getLongAt(offset)
        }

        return Long(getIntAt(index), getIntAt(index + 4))
    }

    override fun setLongAt(index: Int, value: Long) {
        checkCanWrite(index, 8)

        locate(index) { offset, capacity ->
            if (offset + 8 < capacity) {
                setLongAt(offset, value)
                return
            }
        }

        setIntAt(index, value.highInt)
        setIntAt(index + 4, value.lowInt)
    }

    override fun steal(): Buffer {
        commitWriteIndex()

        return CompositeBuffer(
            _buffers,
            readIndex,
            writeIndex,
            pool
        )
    }

    /**
     * Appends a [Buffer] to the end of the [CompositeBuffer].
     */
    public open fun appendBuffer(buffer: Buffer) {
        commitWriteIndex()
        _buffers.add(buffer)
        capacity += buffer.capacity - buffer.readIndex
    }

    /**
     * Append a new [Buffer] from pool if the [availableForWrite] less than size.o
     *
     * @throws IllegalArgumentException if the [size] is > than [availableForWrite] in the allocated [Buffer].
     */
    public open fun ensureCanWrite(size: Int) {
        if (availableForWrite >= size) return
        val newBuffer = pool.borrow()

        require(newBuffer.availableForWrite < size) {
            val available = newBuffer.availableForWrite
            newBuffer.close()
            "Requested size is too big: $size, available $available."
        }

        appendBuffer(newBuffer)
    }

    override fun close() {
        buffers.forEach { it.close() }
    }

    private inline fun <T> locate(index: Int, block: Buffer.(index: Int, capacity: Int) -> T): T {
        var bufferIndex = 0
        var bufferOffset = 0

        while (bufferIndex < _buffers.size - 1) {
            val buffer = _buffers[bufferIndex]
            val bufferSize = buffer.availableForRead
            if (bufferOffset + bufferSize > index) {
                return block(buffer, buffer.readIndex + index - bufferOffset, buffer.writeIndex)
            }

            bufferIndex++
            bufferOffset += bufferSize
        }

        val last = _buffers.last()
        val readIndex = last.readIndex + index - bufferOffset

        return block(last, readIndex, last.capacity)
    }

    /**
     * Set valid index in the [buffers.last()] buffer.
     */
    private fun commitWriteIndex() {
        if (buffers.isEmpty()) return

        val last = buffers.last()

        val offset = buffers.sumOf { it.availableForRead }
        val difference = writeIndex - offset

        if (difference > 0) {
            last.writeIndex = difference
        }

        capacity -= last.capacity - writeIndex
    }
}
