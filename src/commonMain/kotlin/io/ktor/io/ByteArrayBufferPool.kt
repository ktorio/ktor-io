package io.ktor.io

public class ByteArrayBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
) : DefaultPool<ByteArrayBuffer>(capacity) {

    override fun produceInstance(): ByteArrayBuffer = ByteArrayBuffer(DEFAULT_BUFFER_SIZE, this)

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
