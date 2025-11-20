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
    private final int chunksCount;

    public BufferChain(int chunkCapacity, int extraChunksCount) {
        this.writeChunk = 0;
        this.chunkCapacity = chunkCapacity;
        this.chunksCount = extraChunksCount + 1;
        this.chunks = new ArrayList<>();
        chunks.add(new BufferChunk(chunkCapacity));
    }

    private void advance() throws IOException {
        ++writeChunk;
        if (writeChunk >= chunksCount)
            throw new IOException("Buffer chain over flow");
        chunks.add(new BufferChunk(chunkCapacity));
    }

    /**
     * Appends a segment of a byte array to the buffer chain.
     * Automatically allocates new chunks if the current one fills up.
     *
     * @param bytes The source byte array.
     * @param start The starting index in the source array.
     * @param length   The ending index (exclusive limit) in the source array.
     * @throws IOException If the chain exceeds the maximum defined chunk count.
     */
    public void append(byte[] bytes, int start, int length) throws IOException {
        if (start < 0 || length < 0 || start + length > bytes.length)
            throw new IndexOutOfBoundsException("Range [" + start + ", " + (start + length) + ") out of bounds for length " + bytes.length);

        BufferChunk currentChunk = chunks.get(writeChunk);
        int written = currentChunk.write(bytes, start, length);
        if (written == length) return;
        advance(); // advances to the next chunk if possible
        append(bytes, start + written, length - written); // written works here as the starting byte
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
     * Creates a cursor positioned at a specific position of the chain.
     *
     * @param position that the cursor should point to.
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

    public Optional<BufferChunk> getChunk(int index) {
        if (index >= chunksCount)
            throw new IndexOutOfBoundsException();
        if (index >= chunks.size())
            return Optional.empty();
        return Optional.of(chunks.get(index));
    }

    public boolean isEmpty() {
        return writeChunk == 0 && chunks.get(writeChunk).empty();
    }

    public int getSize() {
        return (writeChunk * chunkCapacity) + chunks.get(writeChunk).getSize();
    }

    public int getChunkCapacity() {
        return chunkCapacity;
    }

    public int getChunksCount() {
        return chunksCount;
    }


}
