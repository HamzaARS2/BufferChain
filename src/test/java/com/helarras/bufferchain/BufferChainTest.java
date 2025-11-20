package com.helarras.bufferchain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BufferChainTest {

    private BufferChain chain;

    @BeforeEach
    void setUp() {
        // Max 10 chunks (total capacity 50 bytes)
        chain = new BufferChain(5, 10);
    }

    @Test
    void testSimpleAppendAndRead() throws IOException {
        String data = "Hello";
        chain.append(data.getBytes(), 0, data.length());

        assertEquals(5, chain.getSize());

        BufferCursor cursor = chain.head();
        assertEquals('H', cursor.peek());
        assertTrue(cursor.next());
        assertEquals('e', cursor.peek());
    }

    @Test
    void testCrossingChunkBoundaries() throws IOException {
        // We append 12 bytes: "12345" (Chunk 1) "67890" (Chunk 2) "AB" (Chunk 3)
        String data = "1234567890AB";
        chain.append(data.getBytes(), 0, data.length());

        assertEquals(12, chain.getSize());

        BufferCursor cursor = chain.head();

        // Advance 4 times to hit end of first chunk
        cursor.advance(4);
        assertEquals('5', cursor.peek()); // End of Chunk 1

        assertTrue(cursor.next());
        assertEquals('6', cursor.peek()); // Start of Chunk 2
    }

    @Test
    void testAdvanceAndRewind() throws IOException {
        String data = "ABCDEFGHIJKLMNOP";
        chain.append(data.getBytes(), 0, data.length());

        BufferCursor cursor = chain.head();

        // Jump forward 10 bytes (should cross 2 chunks)
        int moved = cursor.advance(10);
        assertEquals(10, moved);
        assertEquals('K', cursor.peek()); // 0-based index 10 is 'K'

        // Jump backward 5 bytes
        int rewound = cursor.rewind(5);
        assertEquals(5, rewound);
        assertEquals('F', cursor.peek()); // 10 - 5 = 5 ('F')
    }

    @Test
    void testFindPatternSpanningChunks() throws IOException {
        // Chunk 1: "ABCDE"
        // Chunk 2: "FGHIJ"
        chain.append("ABCDEFGHIJ".getBytes(), 0, 10);

        // Pattern "EF" crosses the boundary between Chunk 1 and 2
        byte[] pattern = "EF".getBytes();
        Optional<BufferCursor> result = chain.find(pattern);

        assertTrue(result.isPresent(), "Should find pattern crossing chunks");
        assertEquals(4, result.get().position(), "Pattern starts at index 4 ('E')");
        assertEquals('E', result.get().peek());
    }

    @Test
    void testFindPatternNotFound() throws IOException {
        chain.append("Hello World".getBytes(), 0, 11);
        Optional<BufferCursor> result = chain.find("XYZ".getBytes());

        assertFalse(result.isPresent());
    }

    @Test
    void testDuplicateCursorIndependent() throws IOException {
        chain.append("12345".getBytes(), 0, 5);

        BufferCursor c1 = chain.head();
        BufferCursor c2 = c1.duplicate();

        c1.next(); // c1 is at '2'

        assertEquals('2', c1.peek());
        assertEquals('1', c2.peek(), "c2 should not have moved");
    }

    @Test
    void testAppendValidation() {
        byte[] empty = new byte[10];

        // Should throw exception if we ask for more data than exists in source
        assertThrows(IndexOutOfBoundsException.class, () -> {
            chain.append(empty, 0, 20);
        });

        // Should throw exception if start is negative
        assertThrows(IndexOutOfBoundsException.class, () -> {
            chain.append(empty, -1, 5);
        });
    }

    @Test
    void testChainOverflow() throws IOException {
        // Chain capacity is 50 bytes (5 bytes * 10 chunks)
        byte[] data = new byte[50];
        chain.append(data, 0, 50); // This should fill it perfectly

        // Trying to add one more byte should fail
        assertThrows(IOException.class, () -> {
            chain.append(new byte[]{1}, 0, 1);
        });
    }
}