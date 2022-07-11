package io.ktor.io

import java.nio.ByteBuffer

public class JvmBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
    public val byteBufferPool: ObjectPool<ByteBuffer> = ByteBufferPool.Default
) : DefaultPool<JvmBuffer>(capacity) {

    override fun produceInstance(): JvmBuffer = JvmBuffer(byteBufferPool.borrow(), this).apply {
        reset()
    }

    override fun clearInstance(instance: JvmBuffer): JvmBuffer {
        instance.reset()
        return instance
    }

    override fun disposeInstance(instance: JvmBuffer) {
        byteBufferPool.recycle(instance.buffer)
    }

    public companion object {
        public val Default: ObjectPool<JvmBuffer> = JvmBufferPool()

        public val Empty: ObjectPool<JvmBuffer> = JvmBufferPool(
            capacity = 0,
            byteBufferPool = ByteBufferPool.Empty
        )
    }
}
