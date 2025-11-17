package com.helarras.bufferchain;

public class BufferChunk {
    private final byte [] chunk;
    private int writePos;

    public BufferChunk(int capacity) {
        this.writePos = 0;
        this.chunk = new byte[capacity];
    }

    public int fill(byte [] bytes, int start,  int len) {
        int i = start;
        while (i < len && writePos < chunk.length)
            chunk[writePos++] = bytes[i++];
        return i;
    }

    public byte [] getBytes() {
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
