package io.ktor.io

import java.nio.ByteBuffer

public class JvmBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
    public val byteBufferPool: ObjectPool<ByteBuffer> = ByteBufferPool.Default
) : DefaultPool<Buffer>(capacity) {

    override fun produceInstance(): Buffer = JvmBuffer(byteBufferPool.borrow(), this).apply {
        reset()
    }

    override fun clearInstance(instance: Buffer): Buffer {
        instance.reset()
        return instance
    }

    override fun disposeInstance(instance: Buffer) {
        check(instance is JvmBuffer)

        byteBufferPool.recycle(instance.buffer)
    }

    public companion object {
        public val Default: ObjectPool<Buffer> = JvmBufferPool()

        public val Empty: ObjectPool<Buffer> = JvmBufferPool(
            capacity = 0,
            byteBufferPool = ByteBufferPool.Empty
        )
    }
}
