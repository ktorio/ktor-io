package io.ktor.io

import java.nio.ByteBuffer
import java.nio.ByteOrder

public class ByteBufferPool(
    capacity: Int = DEFAULT_POOL_CAPACITY,
    public val direct: Boolean = true,
    public val bufferSize: Int = DEFAULT_BUFFER_SIZE
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
        public val Default: ObjectPool<ByteBuffer> get() = Direct

        public val Direct: ObjectPool<ByteBuffer> = ByteBufferPool(direct = true)

        public val Heap: ObjectPool<ByteBuffer> = ByteBufferPool(direct = false)

        public val NoPool: ObjectPool<ByteBuffer> = ByteBufferPool(capacity = 0)
    }
}
