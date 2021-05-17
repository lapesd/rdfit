/*
 * Copyright 2021 Alexis Armin Huf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.lapesd.rdfit.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("UnusedReturnValue")
public class GrowableByteBuffer  {
    private static final @Nonnull Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    private @Nonnull byte[] buf;
    private int size = 0;

    public GrowableByteBuffer() { this(32); }
    public GrowableByteBuffer(int capacity) { buf = new byte[capacity]; }

    /**
     * Grow the capacity of this buffer to <code>size()+additional</code>.
     *
     * @param additional additional minimum number of bytes to add to capacity
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer grow(int additional) {
        return reserve(size+additional);
    }

    /**
     * Ensure this buffer array can hold at least <code>minimumSize</code> bytes.
     *
     * @param minimumSize minimum value for {@link GrowableByteBuffer#getArray()}.length
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer reserve(int minimumSize) {
        if (buf.length < minimumSize)
            buf = Arrays.copyOf(buf, (minimumSize & ~0x1f) + 32);
        return this;
    }

    public static byte toByte(int value) {
        assert (value & ~0xFF) == 0 || (value & ~0xFF) == ~0xFF : "value overflows as byte";
        return (byte)value;
    }

    /**
     * Set this buffer size to zero, softly erasing all bytes.
     *
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer clear() {
        size = 0;
        return this;
    }

    /**
     * Performs an <code>assert isEmpty()</code> and returns the same instance.
     *
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer assertEmpty() {
        assert isEmpty();
        return this;
    }

    /**
     * Removes last byte, i.e., decrements size.
     *
     * @throws IllegalStateException if {@link GrowableByteBuffer#isEmpty()}
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer removeLast() {
        if (size > 0)
            --size;
        else
            throw new IllegalStateException("buffer is empty, cannot remove last");
        return this;
    }

    /**
     * Append a byte to this buffer.
     * @param value the value to add
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer add(int value) {
        return add(toByte(value));
    }

    /**
     * Append a byte to this buffer.
     * @param value the value to add
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer add(byte value) {
        reserve(size+1);
        buf[size++] = value;
        return this;
    }

    /**
     * Add the bytes in src[0:src.length) to the end of this buffer.
     *
     * @param src origin of byte values
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer add(byte[] src) {
        return add(src, 0, src.length);
    }

    /**
     * Add the bytes in src[from:from+len) to the end of this buffer.
     *
     * @param src origin of byte values
     * @param from index of the first byte to add
     * @param len number of bytes to add
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer add(byte[] src, int from, int len) {
        if (len < 0)
            throw new NegativeArraySizeException("len="+len+" is negative");
        reserve(size+len);
        System.arraycopy(src, from, buf, size, len);
        size += len;
        return this;
    }

    /**
     * Append all bytes in other to this buffer's end.
     *
     * @param other Another {@link GrowableByteBuffer} to read all bytes from
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer add(@Nonnull GrowableByteBuffer other) {
        return add(other.getArray(), 0, other.size());
    }

    /**
     * Append all bytes in bb, from the position (inclusive) until the limit (exclusive) to the
     * end of this buffer.
     *
     * @param byteBuffer bytes source. Will read starting at {@link ByteBuffer#position()} and will
     *           update it until it reaches {@link ByteBuffer#limit()}
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer add(@Nonnull Buffer byteBuffer) {
        ByteBuffer bb = (ByteBuffer) byteBuffer;
        grow(bb.limit()-bb.position());
        while (bb.hasRemaining())
            buf[size++] = bb.get();
        return this;
    }

    /**
     * Insert the byte value at position pos, shifting the range [pos:size) one byte to the right.
     *
     * @param pos where to insert the byte
     * @param value byte value to insert
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer insert(int pos, int value) {
        return insert(pos, toByte(value));
    }

    /**
     * Insert the byte value at position pos, shifting the range [pos:size) one byte to the right.
     *
     * @param pos where to insert the byte
     * @param value byte value to insert
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer insert(int pos, byte value) {
        reserve(size+1);
        System.arraycopy(buf, pos, buf, pos+1, size-pos);
        buf[pos] = value;
        ++size;
        return this;
    }

    /**
     * Insert bytes data[from:from+len) into position pos, shifting bytes in [pos:size)
     * len bytes to the right.
     *
     * @param pos left-most index where to insert the new bytes
     * @param data array where to read byte values from
     * @param from index of the first byte to be inserted
     * @param len number of bytes to insert
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer insert(int pos, byte[] data, int from, int len) {
        checkPosAndLen(pos, len);
        grow(len);
        System.arraycopy(buf, pos, buf, pos+len, size-pos);
        System.arraycopy(data, from, buf, pos, len);
        size += len;
        return this;
    }

    /**
     * Add all readable bytes from <code>bb</code> to position <code>pos</code>,
     * shifting existing bytes in [pos:size()) to <code>pos+bb.remaining()</code>.
     *
     * @param pos where to insert bytes from bb
     * @param bb Source of bytes to add. can be empty ({@link ByteBuffer#remaining()} == 0.
     *           The {@link ByteBuffer#position()} will be updated until it
     *           reaches {@link ByteBuffer#limit()}.
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer insert(int pos, @Nonnull ByteBuffer bb) {
        if (pos < 0 || pos > size)
            throw new IndexOutOfBoundsException("Bad position "+pos+" on buffer of size "+size);
        int len = bb.remaining();
        grow(len);
        System.arraycopy(buf, pos, buf, pos+len, size-pos); // shift to right
        for (int i = pos, end = pos+len; i < end; i++) // copy data to insertion space
            buf[i] = bb.get();
        return this;
    }

    /**
     * Set a specific byte to the given value.
     *
     * @param pos position of the byte to be set. Must be <code>>=0</code> and <code><=size</code>
     * @param value value to set
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer set(int pos, int value) {
        return set(pos, toByte(value));
    }

    /**
     * Set a specific byte to the given value.
     *
     * @param pos position of the byte to be set. Must be <code>>=0</code> and <code><=size</code>
     * @param value value to set
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer set(int pos, byte value) {
        if (pos == size) {
            add(value);
        } else if (pos > size) {
            throw new IndexOutOfBoundsException("Bad byte index "+pos+", size="+size);
        } else {
            buf[pos] = value;
        }
        return this;
    }

    /**
     * Set bytes in [pos:pos+len) to the bytes in data[from:from+len).
     *
     * @param pos Where to start writing the bytes. Must be <code>>= 0</code>
     *            and <code><= size</code>
     * @param data Source of byte values
     * @param from index of the first byte in data to read from
     * @param len how many bytes to read from data.
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer set(int pos, byte[] data, int from, int len) {
        checkPosAndLen(pos, len);
        reserve(pos+len);
        System.arraycopy(buf, pos, data, from, len);
        return this;
    }

    /**
     * Overwrite bytes in [pos:pos+bb.remaining()) with the readable bytes in <code>bb</code>.
     *
     * This buffer may grow to accommodate bytes writen past the previous
     * {@link GrowableByteBuffer#size}.
     *
     * @param pos where to start writing bytes
     * @param bb bytes to write. Will update {@link ByteBuffer#position()} until it
     *           reaches {@link ByteBuffer#limit()}
     * @return <code>this</code>
     */
    public @Nonnull GrowableByteBuffer set(int pos, @Nonnull ByteBuffer bb) {
        if (pos < 0 || pos > size)
            throw new IndexOutOfBoundsException("Bad pos="+pos+" for buffer of size "+size());
        reserve(pos+bb.remaining());
        for (int i = pos; bb.hasRemaining(); i++)
            buf[i] = bb.get();
        return this;
    }

    private void checkPosAndLen(int pos, int len) {
        if (len < 0)
            throw new NegativeArraySizeException("len=" + len + " is negative");
        if (pos < 0 || pos > size)
            throw new IndexOutOfBoundsException("Bad pos=" + pos + " for buffer of size " + size);
    }

    /**
     * Shifts all bytes from pos onwards len bytes to the right: <code>buf[i+len] = buf[i]</code>.
     *
     * This will not set the gap bytes in [pos:pos+len) to zero. To shift bytes to the right,
     * provide a negative len.
     *
     * @param pos left-most position in the shifted region [pos:size)
     * @param len how many bytes to shift the shifted region to the right. If negative,
     *            shifts to the left.
     * @return <code>this</code>.
     */
    public @Nonnull GrowableByteBuffer shift(int pos, int len) {
        if (pos < 0 || pos >= size)
            throw new IndexOutOfBoundsException("Bad pos="+pos+" for a buffer of size "+size);
        if (len < 0) {
            System.arraycopy(buf, pos+len, buf, pos, size-pos);
        } else if (len > 0) {
            reserve(size+len);
            System.arraycopy(buf, pos, buf, pos+len, size-pos);
        }
        return this;
    }

    public byte get(int idx) {
        if (idx < 0 || idx >= size)
            throw new IndexOutOfBoundsException("Bad index="+idx+" on buffer with size "+size);
        return buf[idx];
    }

    public @Nonnull byte[] getArray() { return buf; }
    public @Nonnull byte[] toArray() { return Arrays.copyOf(buf, size); }
    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
    public @Nonnull ByteBuffer asByteBuffer() { return ByteBuffer.wrap(buf, 0, size); }
    public @Nonnull ByteArrayInputStream asInputStream() { return new ByteArrayInputStream(buf); }

    public @Nonnull String asString(int from, int to) {
        return new String(buf, from, to-from, UTF_8);
    }

    public @Nonnull String asString() {
        return asString(UTF_8);
    }

    public @Nonnull String asString(@Nonnull Charset charset) {
        return new String(buf, 0, size, charset);
    }

    public @Nonnull String asString(@Nonnull Charset charset, int from, int to) {
        return new String(buf, from, to-from, charset);
    }

    public boolean has(int value, int index) {
        if (index < 0)
            throw new IndexOutOfBoundsException("Negative index "+index);
        return index < size() && buf[index] == value;
    }

    public int indexOf(int value) {
        for (int i = 0; i < size; i++) {
            if (buf[i] == value) return i;
        }
        return -1;
    }

    public boolean startsWith(@Nonnull byte[] rhs) {
        return rangeEquals(0, rhs);
    }
    public boolean startsWith(@Nonnull GrowableByteBuffer rhs) {
        return rangeEquals(0, rhs);
    }
    public boolean endsWith(@Nonnull byte[] rhs) {
        int from = size() - rhs.length;
        return from >= 0 && rangeEquals(from, rhs);
    }
    public boolean endsWith(@Nonnull GrowableByteBuffer rhs) {
        int from = size() - rhs.size();
        return from >= 0 && rangeEquals(from, rhs);
    }
    public boolean rangeEquals(int from, @Nonnull GrowableByteBuffer rhs) {
        return rangeEquals(from, from+rhs.size(), rhs);
    }
    public boolean rangeEquals(int from, @Nonnull byte[] rhs) {
        return rangeEquals(from, from+rhs.length, rhs);
    }

    public boolean rangeEquals(int from, int to, @Nonnull byte[] rhs) {
        if (to-from > rhs.length || to > size())
            return false;
        for (int i = from; i < to; i++) {
            if (buf[i] != rhs[i-from]) return false;
        }
        return true;
    }

    public boolean rangeEquals(int from, int to, @Nonnull GrowableByteBuffer rhs) {
        if (to - from > rhs.size() || to > size())
            return false;
        for (int i = from; i < to; i++) {
            if (buf[i] != rhs.get(i-from)) return false;
        }
        return true;
    }
    
    @Override public boolean equals(@Nullable Object rhs) {
        if (this == rhs) return true;
        if (!(rhs instanceof GrowableByteBuffer)) return false;

        GrowableByteBuffer o = (GrowableByteBuffer) rhs;
        if (size != o.size)
            return false;
        for (int i = 0; i < size; i++) {
            if (buf[i] != o.buf[i]) return false;
        }
        return true;
    }

    @Override public int hashCode() {
        int hash = 1;
        for (byte b : buf)
            hash = 31*hash + b;
        return hash;
    }

    @Override public String toString() {
        final ByteBuffer wrap = ByteBuffer.wrap(buf, 0, size);
        return new String(BASE64_ENCODER.encode(wrap).array(), UTF_8);
    }
}
