package com.helarras.bufferchain;

public class BufferChunk {
    private final byte[] chunk;
    private int writePos;

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


    public byte[] getBytes() {
        return chunk.clone();
    }

    public byte get(int index) throws IndexOutOfBoundsException {
        if (index >= chunk.length || index < 0)
            throw new IndexOutOfBoundsException("Chunk length is [" + chunk.length + "] but the index was [" + index + "]");
        return chunk[index];
    }

    public boolean hasSpace() {
        return chunk.length > writePos;
    }

    public int getSize() {
        return writePos;
    }

    public boolean empty() {
        return writePos == 0;
    }

}
