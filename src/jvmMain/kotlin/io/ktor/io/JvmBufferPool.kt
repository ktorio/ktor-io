package io.ktor.io

import java.nio.ByteBuffer

public class JvmBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
    public val byteBufferPool: ObjectPool<ByteBuffer> = ByteBufferPool.Default
) : DefaultPool<JvmBuffer>(capacity) {

    override fun produceInstance(): JvmBuffer {
        val buffer = byteBufferPool.borrow()
        val resource = resource(buffer, byteBufferPool::recycle)
        return JvmBuffer(resource).apply {
            readIndex = 0
            writeIndex = 0
        }
    }

    override fun clearInstance(instance: JvmBuffer): JvmBuffer {
        instance.readIndex = 0
        instance.writeIndex = 0
        return instance
    }

    public companion object {
        public val Default: ObjectPool<JvmBuffer> = JvmBufferPool()

        public val Empty: ObjectPool<JvmBuffer> = JvmBufferPool(
            capacity = 0,
            byteBufferPool = ByteBufferPool.Empty
        )
    }
}
