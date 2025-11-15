package com.helarras.bufferchain;

import java.util.Optional;

public class BufferCursor {
    int chunkPos;
    int offset;
    BufferChain chain;

    public BufferCursor(BufferChain chain) {
        this(chain, 0, 0);
    }

    public BufferCursor(BufferChain chain, int chunkPos, int offset) {
        this.chunkPos = chunkPos;
        this.offset = offset;
        this.chain = chain;
    }

    public byte peek() {
        return chain.getChunk(chunkPos).orElseThrow().get(offset);
    }

    /**
     * Checks whether a next byte is available
     * @return true if there is a next byte
     */
    public boolean hasNext() {
        int nextPos = position() + 1;
        int nextChunkPos = nextPos / chain.getChunkCapacity();
        int nextOffset = nextPos % chain.getChunkCapacity();
        Optional<BufferChunk> oChunk = chain.getChunk(nextChunkPos);
        return nextChunkPos < chain.getChunksCount() && oChunk.isPresent()
            && nextOffset < oChunk.get().getSize();
    }

    public byte next() {
        if (!hasNext()) return peek();
        if (offset + 1 < chain.getChunkCapacity())
            ++offset;
        else {
            offset = 0;
            ++chunkPos;
        }
        return peek();
    }


    public void advance() {
    }
    public void advance(int n) { // 3
        int newPos = position() + n;
        int chunkPos = (newPos + 1 / chain.getChunksCount()) - 1; // 2
        int offset = (newPos + 1 % chain.getChunkCapacity()); // 1

    }

    public int position() {
        return chain.getChunkCapacity() * chunkPos + offset;
    }


    public BufferCursor clone() {
        return new BufferCursor(this.chain, chunkPos, offset);
    }
}
