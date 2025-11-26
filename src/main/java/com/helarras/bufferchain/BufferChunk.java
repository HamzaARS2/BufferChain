package com.helarras.bufferchain;

/**
 * A wrapper around a fixed-size byte array.
 * Represents a single node in the {@link BufferChain}.
 * It tracks the current write position to prevent overwriting data.
 */
public class BufferChunk {
    private final byte[] chunk;
    private int writePos;

    /**
     * Creates a new empty chunk.
     *
     * @param capacity The maximum number of bytes this chunk can hold.
     */
    public BufferChunk(int capacity) {
        this.writePos = 0;
        this.chunk = new byte[capacity];
    }

    /**
     * Writes bytes into this chunk from a source array.
     *
     * @param bytes  The source byte array.
     * @param start  The starting index in the source array.
     * @param length The number of bytes to attempt to write.
     * @return The number of bytes actually written. This may be less than length
     * if the chunk runs out of space.
     */
    public int write(byte[] bytes, int start, int length) {
        int available = chunk.length - writePos;
        int toCopy = Math.min(length, available);
        System.arraycopy(bytes, start, chunk, writePos, toCopy);

        writePos += toCopy;
        return toCopy;
    }

    public byte[] getBytes(int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(chunk, start, bytes, 0, length);
        return bytes;
    }

    public void getBytes(byte [] dst, int start, int length, int chunkOffset) {
        System.arraycopy(chunk, chunkOffset, dst, start, length);
    }

    /**
     * Creates a copy of the underlying byte array.
     *
     * @return A clone of the internal byte array.
     */
    public byte[] getBytes() {
        return chunk.clone();
    }

    /**
     * Retrieves a single byte at a specific index within this chunk.
     *
     * @param index The index to retrieve.
     * @return The byte at the specified index.
     * @throws IndexOutOfBoundsException If the index is negative or outside the chunk bounds.
     */
    public byte get(int index) throws IndexOutOfBoundsException {
        if (index >= chunk.length || index < 0)
            throw new IndexOutOfBoundsException("Chunk length is [" + chunk.length + "] but the index was [" + index + "]");
        return chunk[index];
    }
    /**
     * Checks if there is any space left in this chunk.
     *
     * @return true if the chunk is not full yet.
     */
    public boolean hasSpace() {
        return chunk.length > writePos;
    }

    /**
     * Returns the number of bytes currently written to this chunk.
     *
     * @return The current write position (size).
     */
    public int getSize() {
        return writePos;
    }

    /**
     * Checks if the chunk is completely empty.
     *
     * @return true if no bytes have been written.
     */
    public boolean empty() {
        return writePos == 0;
    }

}
