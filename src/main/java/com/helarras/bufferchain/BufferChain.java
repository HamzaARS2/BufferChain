package com.helarras.bufferchain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public void append(byte [] bytes, int start, int len) throws IOException {
        if (start < 0 || len < start)
            throw new IllegalArgumentException("Cannot append: start must be non-negative and length must not be less than start");
        if (len - start > bytes.length)
            throw new IllegalArgumentException("Cannot append: requested length goes beyond the end of the array. " + "len=" + len + ", start=" + start + ", array length=" + bytes.length);

        BufferChunk currentChunk = chunks.get(writeChunk);
        int bytesFilled = currentChunk.fill(bytes, start, len);
        if (bytesFilled == len) return;
        advance(); // advances to the next chunk if possible
        append(bytes, bytesFilled, len); // bytesFilled works here as the starting byte
    }

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

    public BufferCursor head() {
        return new BufferCursor(this);
    }

    public BufferCursor cursorAt(int position) {
        return new BufferCursor(this, position);
    }

    public BufferCursor tail() {
        int chunkSize = chunks.get(writeChunk).getSize();
        int lastBytePos = chunkSize == 0 ? 0: chunkSize - 1;
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
