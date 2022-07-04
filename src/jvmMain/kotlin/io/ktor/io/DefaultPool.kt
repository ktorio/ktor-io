package io.ktor.io

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.atomicArrayOfNulls
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

public actual abstract class DefaultPool<T : Any> actual constructor(
    actual final override val capacity: Int
) : ObjectPool<T> {
    protected val lock: SynchronizedObject = SynchronizedObject()

    private val instances = atomicArrayOfNulls<Any?>(capacity)
    private var size by atomic(0)

    protected actual abstract fun produceInstance(): T
    protected actual open fun disposeInstance(instance: T) {}

    protected actual open fun clearInstance(instance: T): T = instance
    protected actual open fun validateInstance(instance: T) {}

    public actual final override fun borrow(): T = synchronized(lock) {
        if (size == 0) return produceInstance()
        val idx = --size

        @Suppress("UNCHECKED_CAST")
        val instance = instances[idx].value as T
        instances[idx].value = null

        return clearInstance(instance)
    }

    public actual final override fun recycle(instance: T) {
        synchronized(lock) {
            validateInstance(instance)
            if (size == capacity) {
                disposeInstance(instance)
            } else {
                instances[size++].value = instance
            }
        }
    }

    public actual final override fun dispose() {
        synchronized(lock) {
            for (i in 0 until size) {
                @Suppress("UNCHECKED_CAST")
                val instance = instances[i].value as T
                instances[i].value = null
                disposeInstance(instance)
            }
            size = 0
        }
    }
}
