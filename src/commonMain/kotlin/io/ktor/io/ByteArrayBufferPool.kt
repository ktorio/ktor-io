package io.ktor.io

public class ByteArrayBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
) : DefaultPool<ByteArrayBuffer>(capacity) {

    override fun produceInstance(): ByteArrayBuffer = ByteArrayBuffer(DEFAULT_BUFFER_SIZE)

    override fun clearInstance(instance: ByteArrayBuffer): ByteArrayBuffer {
        instance.reset()
        return instance
    }

    public companion object {
        public val Default: ObjectPool<ByteArrayBuffer> = ByteArrayBufferPool()

        public val NoPool: NoPoolImpl<ByteArrayBuffer> = object : NoPoolImpl<ByteArrayBuffer>() {
            override fun borrow(): ByteArrayBuffer {
                throw NotImplementedError()
            }
        }
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

        public val NoPool: NoPoolImpl<ByteArray> = object : NoPoolImpl<ByteArray>() {
            override fun borrow(): ByteArray {
                throw NotImplementedError()
            }
        }
    }
}