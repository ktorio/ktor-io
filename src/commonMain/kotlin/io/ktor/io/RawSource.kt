package io.ktor.io

/**
 * Interface is used unify different IO sources.
 */
public interface RawSource {

    /**
     * Receive next [Buffer] from source.
     *
     * @return next [Buffer] or `null` if source is exhausted.
     * @throws IOException if the source was cancelled. A new exception created every time. The exception.cause contains
     * the original exception from the [cancel] call.
     */
    public fun receive(): Buffer?

    /**
     * Try to receive a next [Buffer] from the source. This method should try avoiding blocking operations and return
     * null instead.
     *
     * @return next [Buffer] or `null` if source is exhausted or buffer is not available at the moment.
     * @throws IOException if the source was cancelled. A new exception created every time. The exception.cause contains
     * the original exception from the [cancel] call.
     */
    public fun tryReceive(): Buffer? = receive()

    /**
     * Suspends until the next [Buffer] is available
     *
     * @return true if the next [Buffer] is available or false if the source is exhausted.
     * @throws IOException if the source was cancelled. A new exception created every time. The exception.cause contains
     * the original exception from the [cancel] call.
     */
    public suspend fun awaitBuffer(): Boolean

    /**
     * Cancels the source and release all resources. Does nothing if the source is already cancelled.
     */
    public fun cancel(cause: Throwable)
}
