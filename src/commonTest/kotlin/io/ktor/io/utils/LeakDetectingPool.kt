package io.ktor.io.utils

import io.ktor.io.*
import kotlinx.atomicfu.atomic

class LeakDetectingPool : ObjectPool<ByteArrayBuffer> {
    private val allocations = mutableListOf<Allocation<ByteArrayBuffer>>()
    override val capacity: Int = DEFAULT_POOL_CAPACITY

    override fun borrow(): ByteArrayBuffer {
        val array = ByteArray(DEFAULT_BUFFER_SIZE)
        val allocation = Allocation(ByteArrayBuffer(array, readIndex = 0, writeIndex = 0, pool = this))
        allocations += allocation
        return allocation.instance
    }

    override fun dispose() {
        checkLeaks()
    }

    override fun recycle(instance: ByteArrayBuffer) {
        val allocation = allocations.find { it.instance === instance }
            ?: error("Instance $instance is not allocated by this pool")

        allocation.dispose()
    }

    private fun checkLeaks() {
        val leaks = allocations.filter { !it.isDisposed }
        if (leaks.isEmpty()) return

        val stacks = leaks.joinToString("\n") { it.stack }
        throw IllegalStateException("Leaked ${allocations.size} objects: $stacks")
    }
}

private class Allocation<T>(val instance: T) {
    var stack: String = Exception().stackTraceToString()
    private val disposed = atomic(false)

    val isDisposed: Boolean get() = disposed.value

    fun dispose() {
        if (!disposed.compareAndSet(false, true)) {
            throw IllegalStateException("Instance $instance is already disposed")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Allocation<*>) return false

        if (instance !== other.instance) return false
        return true
    }
}
