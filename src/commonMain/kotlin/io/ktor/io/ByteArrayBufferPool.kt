package io.ktor.io

public class ByteArrayBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
    public val arrayPool: ObjectPool<ByteArray> = ByteArrayPool.Default
) : DefaultPool<ByteArrayBuffer>(capacity) {

    override fun produceInstance(): ByteArrayBuffer = ByteArrayBuffer(arrayPool.borrow(), pool = this).apply {
        readIndex = 0
        writeIndex = 0
    }

    override fun disposeInstance(instance: ByteArrayBuffer) {
        arrayPool.recycle(instance.array)
    }

    override fun clearInstance(instance: ByteArrayBuffer): ByteArrayBuffer {
        instance.reset()
        return instance
    }

    public companion object {
        public val Default: ObjectPool<ByteArrayBuffer> = ByteArrayBufferPool()

        public val Empty: ObjectPool<ByteArrayBuffer> = ByteArrayBufferPool(
            capacity = 0,
            arrayPool = ByteArrayPool.Empty
        )
    }
}

public class ByteArrayPool(
    private val size: Int = DEFAULT_BUFFER_SIZE,
    capacity: Int = DEFAULT_POOL_CAPACITY
) : DefaultPool<ByteArray>(capacity) {

    override fun produceInstance(): ByteArray = ByteArray(size)

    override fun clearInstance(instance: ByteArray): ByteArray {
        instance.fill(0)
        return instance
    }

    public companion object {
        public val Default: ObjectPool<ByteArray> = ByteArrayPool()

        public val Empty: ObjectPool<ByteArray> = ByteArrayPool(capacity = 0)
    }
}