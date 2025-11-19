package com.helarras.bufferchain;

import java.util.Optional;

public class BufferCursor {
    private int chunkPos;
    private int offset;
    private final BufferChain chain;

    public BufferCursor(BufferChain chain) {
        this(chain, 0, 0);
    }

    public BufferCursor(BufferChain chain, int position) {
        this(chain, position / chain.getChunkCapacity(), position % chain.getChunkCapacity());
        if (position >= chain.getSize())
            throw new IllegalArgumentException("Position " + position + " exceeds buffer chain size " + chain.getSize());
        if (position < 0)
            throw new IllegalArgumentException("Position cannot be negative: " + position);

    }

    public BufferCursor(BufferChain chain, int chunkPos, int offset) {
        this.chunkPos = chunkPos;
        this.offset = offset;
        this.chain = chain;
    }

    public byte peek() {
        if (chain.isEmpty())
            throw new RuntimeException("BufferChain is empty");
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

    /**
     *  Advances the cursor to the next available byte in the buffer chain
     * @return true if the cursor was successfully moved to the next byte,
     * false if the end of the buffer chain was already reached.
     */

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


    public int advance(int n) { // 3
        if (n < 0) throw new IllegalArgumentException("Negative advance not allowed");
        BufferCursor tail = chain.tail();

        int oldPos = position();
        int newPos = Math.min(oldPos + n, tail.position());
        this.chunkPos = newPos / chain.getChunkCapacity();
        this.offset = newPos % chain.getChunkCapacity();
        return newPos - oldPos;
    }

    public int rewind(int n) {
        if (n < 0) throw new IllegalArgumentException("Negative rewind not allowed");
        BufferCursor head = chain.head();

        int oldPos = position();
        int newPos = Math.max(oldPos - n, head.position());
        this.chunkPos = newPos / chain.getChunkCapacity();
        this.offset = newPos % chain.getChunkCapacity();
        return oldPos - newPos;
    }


    public int position() {
        return chain.getChunkCapacity() * chunkPos + offset;
    }


    public BufferCursor clone() {
        return new BufferCursor(this.chain, chunkPos, offset);
    }
}
