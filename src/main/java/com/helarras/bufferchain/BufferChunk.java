package com.helarras.bufferchain;

import java.util.Arrays;

public class BufferChunk {
    private final byte [] chunk;
    private int cursor;

    public BufferChunk(int capacity) {
        this.cursor = 0;
        this.chunk = new byte[capacity];
    }

    public int fill(byte [] bytes, int start,  int len) {
        int i = start;
        while (i < len && cursor < chunk.length)
            chunk[cursor++] = bytes[i++];
        return i;
    }

    public byte [] getBytes() {
        return chunk.clone();
    }

    public boolean hasSpace() {
        return chunk.length > cursor;
    }

    public int getSize() {
        return cursor;
    }

}
