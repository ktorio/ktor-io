package io.ktor.io


public class JvmBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
) : DefaultPool<JvmBuffer>(capacity) {

    override fun produceInstance(): JvmBuffer = JvmBuffer(DirectByteBufferPool.Default)

    public companion object {
        public val Default: ObjectPool<JvmBuffer> = JvmBufferPool()

        public val NoPool: ObjectPool<JvmBuffer> = object : DefaultPool<JvmBuffer>(capacity = 0) {
            override fun produceInstance(): JvmBuffer = JvmBuffer(DirectByteBufferPool.NoPool)
        }
    }
}
