package com.helarras.bufferchain;

import java.util.Optional;

public class BufferCursor {
    private int chunkPos;
    private int offset;
    private BufferChain chain;

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
        if (nextChunkPos >= chain.getChunksCount())
            return false; // In case nextChunkPost exceeded the chain chunks count
        Optional<BufferChunk> oChunk = chain.getChunk(nextChunkPos);
        return nextChunkPos < chain.getChunksCount() && oChunk.isPresent()
            && nextOffset < oChunk.get().getSize();
    }

    public boolean next() {
        if (!hasNext()) return false;
        if (offset + 1 < chain.getChunkCapacity())
            ++offset;
        else {
            offset = 0;
            ++chunkPos;
        }
        return true;
    }


    public void advance(int n) { // 3
        if (n < 0) throw new IllegalArgumentException("Negative advance not allowed");
        BufferCursor cursor = chain.tail();

        int newPos = Math.min(position() + n, cursor.position());
        this.chunkPos = newPos / chain.getChunkCapacity();
        this.offset = newPos % chain.getChunkCapacity();
    }



    public int position() {
        return chain.getChunkCapacity() * chunkPos + offset;
    }


    public BufferCursor clone() {
        return new BufferCursor(this.chain, chunkPos, offset);
    }
}
