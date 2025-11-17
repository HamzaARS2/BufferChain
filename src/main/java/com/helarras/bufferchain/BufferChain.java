package com.helarras.bufferchain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BufferChain {
    private List<BufferChunk> chunks;
    private int current;
    private final int chunkCapacity;
    private final int chunksCount;
    private final boolean lazyInit = true;

    public BufferChain(int chunkCapacity, int chunksCount) {
        this.current = 0;
        this.chunkCapacity = chunkCapacity;
        this.chunksCount = chunksCount;
        this.chunks = new ArrayList<>();
        chunks.add(new BufferChunk(chunkCapacity));
    }

    private void advance() throws IOException {
        ++current;
        if (current >= chunksCount)
            throw new IOException("Buffer chain over flow");
        if (lazyInit)
            chunks.add(new BufferChunk(chunkCapacity));
    }

    public void append(byte [] bytes, int start, int len) throws IOException {
        if (len - start > bytes.length)
            throw new IllegalArgumentException("Cannot append: requested length goes beyond the end of the array. " + "len=" + len + ", start=" + start + ", array length=" + bytes.length);

        BufferChunk currentChunk = chunks.get(current);
        int bytesFilled = currentChunk.fill(bytes, start, len);
        if (bytesFilled == len) return;
        advance(); // advances to the next chunk if possible
        append(bytes, bytesFilled, len); // bytesFilled works here as the starting byte
    }

    public BufferCursor tail() {
        int chunkSize = chunks.get(current).getSize();
        int lastBytePos = chunkSize == 0 ? 0: chunkSize - 1;
        return new BufferCursor(this, current, lastBytePos);
    }

    public Optional<BufferChunk> getChunk(int index) {
        if (index >= chunksCount)
            throw new IndexOutOfBoundsException();
        if (index >= chunks.size())
            return Optional.empty();
        return Optional.of(chunks.get(index));
    }

    public boolean isEmpty() {
        return current == 0 && chunks.get(current).empty();
    }

    public int getChunkCapacity() {
        return chunkCapacity;
    }

    public int getChunksCount() {
        return chunksCount;
    }

    public void debugPrint() {
        System.out.println("BufferChain debug: total chunks = " + chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            BufferChunk chunk = chunks.get(i);
            System.out.print("Chunk " + i + " [filled=" + chunk.getSize() + "]: ");
            byte[] data = chunk.getBytes();
            for (int j = 0; j < chunk.getSize(); j++) {
                // print as hex for clarity
                System.out.print(String.format("%02X ", data[j]));
            }
            System.out.println();
        }
    }


}
