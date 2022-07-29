package io.ktor.io

//high-level replacement for pools (pool still can be used under the hood) but such allocator is more flexible
// f.e. we can implement some Arena allocator, to not allocate all time BIG buffers for small operations
public interface BufferAllocator : Closeable {
    //returned underlying memory segment can be bigger than expected, but capacity will be equal to size
    public fun allocate(size: Int): Buffer
}
