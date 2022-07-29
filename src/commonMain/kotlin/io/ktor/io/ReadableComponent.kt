package io.ktor.io

import kotlin.contracts.*

// allow to access:
//  - ByteArray if available
//  - ByteBuffer if available
//  - NativeAddress if available (direct byte buffer or netty Unsafe buffer)
//  - CPointer if available ?
//  - etc.

//TODO: do the same for Writable

//TODO: should it be readable? - overall looks like we need var index and val limit
public interface ReadableComponent : Readable {
    //when reading relatively to underlying buffer, we need to move the position forward correctly using readIndex
}

public interface ReadableComponentIterator : Closeable {
    public operator fun next(): ReadableComponent
    public operator fun hasNext(): Boolean
}

//example of implementation

//TODO: array vs byteArray properties naming
public interface ByteArrayReadableComponent : ReadableComponent {
    public val array: ByteArray
    public val arrayOffset: Int
    public val arrayLength: Int
}

//contract is needed to be more convenient to use
@OptIn(ExperimentalContracts::class)
public fun ReadableComponent.hasByteArray(): Boolean {
    contract {
        returns(true) implies (this@hasByteArray is ByteArrayReadableComponent)
    }
    return this is ByteArrayReadableComponent
}
