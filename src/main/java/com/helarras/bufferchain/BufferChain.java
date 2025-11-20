package com.helarras.bufferchain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A dynamic buffer implementation that manages data in linked chunks.
 * This avoids large contiguous memory allocation and allows for efficient
 * growing of the buffer.
 */

public class BufferChain {
    private final List<BufferChunk> chunks;
    private int writeChunk;
    private final int chunkCapacity;
    private final int maxChunks;

    /**
     * Initializes a new BufferChain.
     *
     * @param chunkCapacity    The size (in bytes) of each individual chunk.
     * @param maxChunks  The total number of chunks allowed (must be >= 1).
     * @throws IllegalArgumentException if the maxChunks is less than 1.
     */
    public BufferChain(int chunkCapacity, int maxChunks) {
        if (maxChunks < 1) throw new IllegalArgumentException("maxChunks must be at least 1");
        this.writeChunk = 0;
        this.chunkCapacity = chunkCapacity;
        this.maxChunks = maxChunks;
        this.chunks = new ArrayList<>();
        chunks.add(new BufferChunk(chunkCapacity));
    }

    private void advance() throws IOException {
        ++writeChunk;
        if (writeChunk >= maxChunks)
            throw new IOException("Buffer chain over flow: Max chunks (" + maxChunks + ") reached.");
        chunks.add(new BufferChunk(chunkCapacity));
    }

    /**
     * Appends data to the buffer chain.
     * Automatically handles chunk boundaries by filling the current chunk
     * and creating new ones if necessary.
     *
     * @param bytes  The source byte array.
     * @param start  The starting index in the source array.
     * @param length The number of bytes to append.
     * @throws IOException               If the buffer runs out of chunks (Overflow).
     * @throws IndexOutOfBoundsException If the start/length parameters are invalid.
     */
    public void append(byte[] bytes, int start, int length) throws IOException {
        if (start < 0 || length < 0 || start + length > bytes.length)
            throw new IndexOutOfBoundsException("Range [" + start + ", " + (start + length) + ") out of bounds for length " + bytes.length);

        BufferChunk currentChunk = chunks.get(writeChunk);
        int written = currentChunk.write(bytes, start, length);
        if (written == length) return;
        advance();
        append(bytes, start + written, length - written);
    }

    /**
     * Searches for a specific byte pattern within the buffer chain.
     *
     * @param pattern The byte array pattern to search for.
     * @return An Optional containing a BufferCursor positioned at the start
     * of the found pattern, or empty if not found.
     */
    public Optional<BufferCursor> find(byte[] pattern) {
        if (pattern.length == 0)
            return Optional.empty();
        BufferCursor cursor = new BufferCursor(this);
        int matched = 0;
        do {
            byte current = cursor.peek();
            if (current == pattern[matched]) ++matched;
            else {
                matched = 0;
                if (current == pattern[matched])
                    ++matched;
            }
        } while (matched < pattern.length && cursor.next());
        if (matched != pattern.length)
            return Optional.empty();
        cursor.rewind(matched - 1);
        return Optional.of(cursor);
    }

    /**
     * Creates a cursor positioned at the very beginning of the chain.
     */
    public BufferCursor head() {
        return new BufferCursor(this);
    }

    /**
     * Creates a cursor at a specific absolute position.
     *
     * @param position The absolute byte index to position the cursor at.
     * @return A new BufferCursor.
     */
    public BufferCursor cursorAt(int position) {
        return new BufferCursor(this, position);
    }

    /**
     * Creates a cursor positioned at the last byte of the chain.
     */
    public BufferCursor tail() {
        int chunkSize = chunks.get(writeChunk).getSize();
        int lastBytePos = chunkSize == 0 ? 0 : chunkSize - 1;
        return new BufferCursor(this, writeChunk, lastBytePos);
    }

    /**
     * Retrieves a specific chunk by index.
     *
     * @param index The chunk index.
     * @return An Optional containing the chunk, or empty if it doesn't exist yet.
     */
    public Optional<BufferChunk> getChunk(int index) {
        if (index >= maxChunks)
            throw new IndexOutOfBoundsException();
        if (index >= chunks.size())
            return Optional.empty();
        return Optional.of(chunks.get(index));
    }

    /**
     * @return true if the buffer contains no data.
     */
    public boolean isEmpty() {
        return writeChunk == 0 && chunks.get(writeChunk).empty();
    }

    /**
     * Calculates the total number of bytes stored in the entire chain.
     *
     * @return Total size in bytes.
     */
    public int getSize() {
        return (writeChunk * chunkCapacity) + chunks.get(writeChunk).getSize();
    }

    public int getChunkCapacity() {
        return chunkCapacity;
    }

    public int getMaxChunks() {
        return maxChunks;
    }


}
