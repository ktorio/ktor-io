package io.ktor.io

import java.nio.ByteBuffer
import java.nio.ByteOrder


public class DirectByteBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE
) : DefaultPool<ByteBuffer>(capacity) {

    override fun produceInstance(): ByteBuffer = ByteBuffer.allocateDirect(bufferSize)!!

    override fun clearInstance(instance: ByteBuffer): ByteBuffer = instance.apply {
        clear()
        order(ByteOrder.BIG_ENDIAN)
    }

    override fun validateInstance(instance: ByteBuffer) {
        check(instance.capacity() == bufferSize)
        check(instance.isDirect)
    }

    public companion object {
        public val Default: ObjectPool<ByteBuffer> = DirectByteBufferPool()

        public val NoPool: ObjectPool<ByteBuffer> = DirectByteBufferPool(capacity = 0)
    }
}
