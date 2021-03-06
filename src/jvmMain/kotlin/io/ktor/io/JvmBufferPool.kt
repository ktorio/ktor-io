package io.ktor.io

import java.nio.ByteBuffer

public class JvmBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
    public val byteBufferPool: ObjectPool<ByteBuffer> = ByteBufferPool.Default
) : DefaultPool<JvmBuffer>(capacity) {

    override fun produceInstance(): JvmBuffer = JvmBuffer(byteBufferPool)

    public companion object {
        public val Default: ObjectPool<JvmBuffer> = JvmBufferPool()

        public val Empty: ObjectPool<JvmBuffer> = JvmBufferPool(
            capacity = 0,
            byteBufferPool = ByteBufferPool.Empty
        )
    }
}
