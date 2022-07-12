package io.ktor.io

public class ByteArrayBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
    public val arrayPool: ObjectPool<ByteArray> = ByteArrayPool.Default
) : DefaultPool<Buffer>(capacity) {

    override fun produceInstance(): Buffer = ByteArrayBuffer(arrayPool.borrow(), pool = this).apply {
        readIndex = 0
        writeIndex = 0
    }

    override fun disposeInstance(instance: Buffer) {
        check(instance is ByteArrayBuffer)
        arrayPool.recycle(instance.array)
    }

    override fun clearInstance(instance: Buffer): Buffer {
        instance.reset()
        return instance
    }

    public companion object {
        public val Default: ObjectPool<Buffer> = ByteArrayBufferPool()

        public val Empty: ObjectPool<Buffer> = ByteArrayBufferPool(
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