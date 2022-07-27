package io.ktor.io

import kotlinx.atomicfu.*

public sealed interface Resource<C> : Closeable {
    //value will be considered closed if:
    // - called Resource.close
    // - Resource object becomes unreachable
    // - all created links are considered closed
    public val value: C

    //cannot be called on closed resource
    public fun link(): Link

    //Link will be considered closed if:
    // - called Link.close
    // - Link object becomes unreachable
    public sealed interface Link : Closeable
}

//`close` will be called exactly once
public inline fun <T> resource(value: T, crossinline close: (T) -> Unit): Resource<T> =
    resource(value, Runnable { close(value) })

@PublishedApi
internal fun <T> resource(value: T, cleanup: Runnable): Resource<T> = ResourceImpl(value, cleanup)

public expect fun interface Runnable {
    public fun run()
}

//TODO: 2 atomics vs 1 syncrhonized
private class ResourceImpl<C>(
    value: C,
    valueCleanup: Runnable
) : Resource<C>, RefCountable(valueCleanup) {
    private val _value = atomic<C?>(value)
    override val value: C get() = checkNotNull(_value.value) { "Resource is closed" }

    override fun link(): Resource.Link {
        value //check if resource is still active
        increment()
        return LinkImpl(decrement)
    }

    private class LinkImpl(cleanup: Runnable) : Resource.Link, AutoCloseable(cleanup)

    override fun beforeManualClose() {
        _value.value = null
    }
}

internal expect abstract class AutoCloseable(cleanup: Runnable) : Closeable {
    final override fun close()
    open fun beforeManualClose()
}

internal abstract class RefCountable(cleanup: Runnable) : AutoCloseable(cleanup) {
    private val refCount = atomic(0)

    protected val decrement: Runnable = Runnable {
        if (refCount.decrementAndGet() == 0) close()
    }

    protected fun increment() {
        refCount.incrementAndGet()
    }
}
