package io.ktor.io.internals

import kotlinx.atomicfu.atomic

internal interface ReferenceCounter {
    fun retain()
    fun release(): Boolean
}

internal object EmptyReferenceCounter : ReferenceCounter {
    override fun retain() {}
    override fun release(): Boolean = false
}

internal class AtomicReferenceCounter : ReferenceCounter {
    private val count = atomic(0)

    override fun retain() {
        count.incrementAndGet()
    }

    override fun release(): Boolean = count.decrementAndGet() == 0
}
