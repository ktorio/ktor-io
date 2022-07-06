package io.ktor.io

public class ByteArrayBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
) : DefaultPool<ByteArrayBuffer>(capacity) {

    override fun produceInstance(): ByteArrayBuffer = ByteArrayBuffer(ByteArrayPool.Default)

    override fun clearInstance(instance: ByteArrayBuffer): ByteArrayBuffer {
        instance.resetForWrite()
        return instance
    }

    public companion object {
        public val Default: ObjectPool<ByteArrayBuffer> = ByteArrayBufferPool()

        public val Empty: ObjectPool<ByteArrayBuffer> = ByteArrayBufferPool(capacity = 0)
    }
}

public class ByteArrayPool(
    private val size: Int = DEFAULT_BUFFER_SIZE,
    capacity: Int = DEFAULT_POOL_CAPACITY,
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