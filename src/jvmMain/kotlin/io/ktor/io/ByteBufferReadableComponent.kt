package io.ktor.io

import java.nio.*
import kotlin.contracts.*

public interface ByteBufferReadableComponent : ReadableComponent {
    public val byteBuffer: ByteBuffer
}

@OptIn(ExperimentalContracts::class)
public fun ReadableComponent.hasByteBuffer(): Boolean {
    contract {
        returns(true) implies (this@hasByteBuffer is ByteBufferReadableComponent)
    }
    return this is ByteBufferReadableComponent
}

//example of what we can do
private fun Buffer.example() {
    iterateReadableComponents { component, last ->
        when {
            //here we can also do component is ByteBufferReadableComponent - but it's super verbose
            component.hasByteBuffer() -> {
                component.byteBuffer //can be used
                //do something with it
                component.readIndex += 12 //TODO?
            }

            component.hasByteArray()  -> {
                component.array
            }
            //hasNettyBuffer
            //hasOkioBuffer
        }
        true
    }
}
