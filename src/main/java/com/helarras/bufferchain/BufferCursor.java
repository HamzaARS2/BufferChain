package com.helarras.bufferchain;

import java.util.Optional;


/**
 * Allows traversal over the BufferChain as if it were a single continuous stream.
 * Handles the logic of jumping between chunks automatically.
 */
public class BufferCursor {
    private int chunkPos;
    private int offset;
    private final BufferChain chain;

    /**
     * Creates a cursor starting at the beginning of the chain.
     * @param chain The buffer chain to navigate.
     */
    public BufferCursor(BufferChain chain) {
        this(chain, 0, 0);
    }

    /**
     * Creates a cursor at a specific absolute position.
     *
     * @param chain    The buffer chain to navigate.
     * @param position The absolute byte index.
     * @throws IllegalArgumentException If the position is negative or exceeds the chain size.
     */
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

    /**
     * Returns the byte at the current cursor position without moving the cursor.
     *
     * @return The current byte.
     * @throws RuntimeException If the chain is empty.
     */
    public byte peek() {
        if (chain.isEmpty())
            throw new RuntimeException("BufferChain is empty");
        return chain.getChunk(chunkPos).orElseThrow().get(offset);
    }


    /**
     * Checks whether a next byte is available
     *
     * @return true if there is a next byte
     */
    public boolean hasNext() {
        int nextPos = position() + 1;
        int nextChunkPos = nextPos / chain.getChunkCapacity();
        int nextOffset = nextPos % chain.getChunkCapacity();
        if (nextChunkPos >= chain.getMaxChunks())
            return false; // In case nextChunkPost exceeded the chain chunks count
        Optional<BufferChunk> oChunk = chain.getChunk(nextChunkPos);
        return nextChunkPos < chain.getMaxChunks() && oChunk.isPresent()
                && nextOffset < oChunk.get().getSize();
    }

    /**
     * Advances the cursor to the next available byte in the buffer chain
     *
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

    /**
     * Moves the cursor forward by a specific number of bytes.
     *
     * @param n The number of steps to advance.
     * @return The actual number of steps advanced (capped by the tail of the chain).
     * @throws IllegalArgumentException if n is negative.
     */
    public int advance(int n) { // 3
        if (n < 0) throw new IllegalArgumentException("Negative advance not allowed");
        BufferCursor tail = chain.tail();

        int oldPos = position();
        int newPos = Math.min(oldPos + n, tail.position());
        this.chunkPos = newPos / chain.getChunkCapacity();
        this.offset = newPos % chain.getChunkCapacity();
        return newPos - oldPos;
    }

    /**
     * Moves the cursor backward by a specific number of bytes.
     *
     * @param n The number of steps to rewind.
     * @return The actual number of steps rewound (capped by the head of the chain).
     * @throws IllegalArgumentException if n is negative.
     */
    public int rewind(int n) {
        if (n < 0) throw new IllegalArgumentException("Negative rewind not allowed");
        BufferCursor head = chain.head();

        int oldPos = position();
        int newPos = Math.max(oldPos - n, head.position());
        this.chunkPos = newPos / chain.getChunkCapacity();
        this.offset = newPos % chain.getChunkCapacity();
        return oldPos - newPos;
    }

    /**
     * Calculates the current absolute position of the cursor in the chain.
     *
     * @return The 0-based index relative to the start of the entire chain.
     */
    public int position() {
        return chain.getChunkCapacity() * chunkPos + offset;
    }

    /**
     * Creates a new Cursor with the exact same position as this one.
     * The two cursors move independently.
     *
     * @return A new BufferCursor instance.
     */
    public BufferCursor duplicate() {
        return new BufferCursor(this.chain, chunkPos, offset);
    }
}
