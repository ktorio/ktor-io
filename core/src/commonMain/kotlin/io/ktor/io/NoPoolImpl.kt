package io.ktor.io

/**
 * A pool implementation of zero capacity that always creates new instances
 */
public abstract class NoPoolImpl<T : Any> : ObjectPool<T> {
    override val capacity: Int
        get() = 0

    override fun recycle(instance: T) {
    }

    override fun dispose() {
    }
}
