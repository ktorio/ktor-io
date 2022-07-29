package io.ktor.io

//it's safe to create shared slices ONLY over readOnly buffer
//TODO: naming - buffer vs slice vs view?
public interface ReadOnlyBuffer : Readable {
    public fun getBufferAt(index: Int, size: Int): ReadOnlyBuffer

    public fun getBuffer(startIndex: Int, endIndex: Int): ReadOnlyBuffer {
        return getBufferAt(startIndex, endIndex - startIndex)
    }

    public fun readBuffer(size: Int = availableForRead): ReadOnlyBuffer {
        return getBufferAt(readIndex, size)
    }

    //zero bytes copy
    //TODO: naming - duplicate?
    public fun copy(): ReadOnlyBuffer = getBufferAt(0, readLimit)
}
