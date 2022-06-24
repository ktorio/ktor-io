# Source & Destination

## Source

There are lots of different places we need to read from (like files, sockets, different inputs from other libraries). We
need to have some abstraction about these places. Here the `Source` comes.

### Reading Bytes

The most basic operation is `receive`. It gives you a new `Buffer` with data from the source:

```kotlin
val source = mySocket.source
val buffer = source.receive()
```

The `Buffer` will have the data stored between `readPosition` and `writePosition`.
All `receive` calls are independent. Each call new buffer is returned and the old buffer is still possible to use.

```kotlin
val first = source.receive()
val second = source.receive()

assertTrue(first !== second)
```

If there are no bytes available at the moment, the `receive` operation returns an empty `Buffer`:

```kotlin
fun Source.readAvailable(): List<Buffer> {
    val chunks = mutableListOf<Buffer>()
    while (true) {
        val buffer = source.receive()
        if (buffer.isEmpty()) return chunks

        chunks.add(buffer)
    }
}
```

### Waiting for Bytes

What should I do if I want to read a specific amount of bytes or the whole `Source` to the `ByteArray`? If there are no
bytes available you have to wait. Waiting could be expensive operation. It can require system calls or context switch.
Spinning on the `receive()` call also seems not a good idea. For these reasons, there is a separate `awaitBytes()`
operation. It suspends until the source has some data available, or it will be closed:

```kotlin
abstract class Source {
    ...
    suspend fun awaitBytes(): Boolean
}
```

`awaitBytes` returns `true` if there is some data available. It returns `false` if the source is closed.

### What should I do if I don't need `Source` any more?

Imagine that you have a method to read a message from `Source`:

```kotlin
fun Source.parseMessage(): Message = TODO()
```

Of course, it can fail:

```kotlin
val message = try {
    socketSource.parseMessage()
} catch (cause: ParseException) {
    log.error("Failed to parse message", cause)
    throw cause
}
```

The source doesn't know about your message format, protocols and other stuff and of course it will continue reading data
from socket. If you want to be a nice citizen, you should not waste time or memory on reading bytes and release all
resources. You can do this, by cancelling the `Source`:

```kotlin
val message = try {
    socketSource.parseMessage()
} catch (cause: ParseException) {
    log.error("Failed to parse message", cause)
    throw cause
} catch (cause: Throwable) {
    socketSource.cancel(cause)
    throw cause
}
```

This is a common pattern of using `Source`s. If you don't need logging here, you can use the `use` shortcut:

```kotlin
val message = socketSource.use {
    it.parseMessage()
}
```

If the `Source` is cancelled with an exception, it will be stored to the `cancelCause` property.

```kotlin
abstract class Source {
    abstract val cancelCause: Throwable?

    abstract fun receive(): Buffer

    abstract suspend fun awaitBytes(): Boolean

    abstract fun cancel(cause: Throwable = CancellationException("Source has been cancelled"))
}
```

## Destination

To handle different outputs there is another abstraction: `Destination`.
You can write `Buffer` using the `write` method:

```kotlin
val destination = mySocket.destination

val count = destination.write(buffer)
println("$count bytes written")
```

The `write` call writes as much data (between `buffer.readPosition` and `buffer.writePosition`) as it can to the
destination. It modifies `buffer.readPosition` and returns amount of written bytes.

While the `Source` is responsible for creating `Buffer`, `Destination` is responsible for releasing it. If the `buffer`
has no bytes available after write, it will be automatically released.

### Flush

It's hard to write human-readable justification of having flush method without introducing a new abstractions, so I will
keep it as TODO("Rewrite this").

If you have chained transformations on the `Destination` like:

```kotlin
val destination = mySocket.destination
    .buffered()
    .compressed()
```

and a single `message`. There is a common pattern when you want to send this message and make sure that all data is
actually written to file or socket(for instance it can be a http message header). Some transformations could keep an
internal buffer because of efficiency reasons.

To make sure that all data is written to the destination, you can call `flush()`. The `flush()` call suspends until all
data is actually written to the final destination:

```kotlin
destination.writeFully(message)
destination.flush()
// At this point message should be compressed and written to the socket.
```

## Writing Full Buffer

How could you write a whole `Buffer` to the `Destination`? The spin on the `write` method is not a good idea, so there
is a method `awaitFreeSpace()` that suspends until the destination has some free space to write:

```kotlin
suspend fun Destination.writeBuffer(buffer: Buffer) {
    while (!buffer.isEmpty()) {
        awaitFreeSpace()
        write(buffer)
    }
}
```

If `Destination` has some buffered bytes, they should be flushed in `awaitFreeSpace`.

### How to end the conversation?

After writing The `close()` method terminates `Destination`. If there are some pending bytes they will be sent:

```kotlin
suspend fun Destination.writeTrailer(trailer: ByteArray) {
    writeFully(trailer)
    close()
}
```

You can pass close cause as exception to the close method. It will be stored to the `closedCause` and available for
tracing.

## Final Listing

```kotlin
abstract class Destination {
    abstract val closeCause: Throwable?

    abstract fun write(buffer: Buffer): Int

    abstract suspend fun awaitFreeSpace()

    abstract suspend fun flush()

    abstract fun close(cause: Throwable? = null)
}
```
