# BufferChain
# Overview

**BufferChain** is a memory-efficient, chunk-based byte buffer implementation for Java.


When building high-performance applications (like Web Servers), allocating large contiguous `byte[]` arrays can lead to `OutOfMemoryErrors` and heap fragmentation.

**BufferChain** solves this by breaking data into small, fixed-size "Chunks" linked together. It provides a `BufferCursor` to traverse these chunks seamlessly, as if they were one continuous array.

##  Features
* **Dynamic Growth:** Automatically adds chunks as you append data.
* **Memory Safety:** Enforces a strict `maxChunks` limit to prevent infinite memory usage.
* **Cursor Navigation:** Read, seek, and peek through data without worrying about chunk boundaries.
* **Pattern Matching:** Built-in support for finding byte patterns (useful for parsing HTTP headers like `\r\n\r\n`).

##  Installation

### Maven
Add the JitPack repository to your `pom.xml`:
```xml
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>
```

Add the dependency:
```xml
<dependency>
    <groupId>com.github.helarras</groupId>
    <artifactId>BufferChain</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

1. Create a chain
```java
// Create a chain with:
// - 1KB (1024 bytes) per chunk
// - Max 8 chunks total
// Total Capacity = 8KB
BufferChain chain = new BufferChain(1024, 8);
```
2. Append data
```java
byte[] data = "Hello world from BufferChain!".getBytes();
// The chain automatically handles splitting data across chunks.
chain.append(data, 0, data.length);
```
3. Read with Cursor

```java
BufferCursor cursor = chain.head();
do {
    System.out.print((char)cursor.peek());
} while(cursor.next());
```