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

package com.github.lapesd.rdfit.source.fixer;

import com.github.lapesd.rdfit.source.fixer.impl.TurtleFixerParsers;
import com.github.lapesd.rdfit.util.GrowableByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Transforming {@link InputStream} that makes invalid NT/Turtle/TriG look valid.
 */
public class TurtleFamilyFixerStream extends InputStream {
    private static final int PREFERRED_BUFFER_SIZE = 8192;

    /* --- --- --- Output state --- --- --- */

    private int nextOverride = 0;
    private final @Nonnull GrowableByteBuffer override = new GrowableByteBuffer();

    /* --- --- --- Input state --- --- --- */

    private final @Nonnull InputStream delegate;
    private @Nullable byte[] input;
    private int inputPos = 0, inputSize = 0;

    /* --- --- --- Parser state --- --- --- */

    private @Nonnull FixerParser currentParser;

    /* --- --- --- Constructors --- --- --- */

    /**
     * Create a new {@link InputStream} that returns the bytes from delegate, unless there is
     * a NT/Turtle/TriG syntax error on the bytes read from delegate. If such an error is found,
     * this {@link TurtleFamilyFixerStream} will try to change the returned bytes to make
     * a NT/Turtle/TriG parser accept the input.
     *  @param delegate the NT/Turtle/TriG input source
     * 
     */
    public TurtleFamilyFixerStream(@Nonnull InputStream delegate) {
        this(delegate, delegate.toString());
    }

    /**
     * Create a new {@link InputStream} that returns the bytes from delegate, unless there is
     * a NT/Turtle/TriG syntax error on the bytes read from delegate. If such an error is found,
     * this {@link TurtleFamilyFixerStream} will try to change the returned bytes to make
     * a NT/Turtle/TriG parser accept the input.
     *
     * @param delegate the NT/Turtle/TriG input source
     * @param context if an error is found/fixed, report this string as the source of the input
     */
    public TurtleFamilyFixerStream(@Nonnull InputStream delegate,
                                   @Nullable String context) {
        this.delegate = delegate;
        currentParser = new TurtleFixerParsers.Start(override, context);
    }

    /* --- --- --- Internal utilities --- --- --- */

    private int nextInputByte(int maxBytesToRead) throws IOException {
        if (input == null)
            input = new byte[Math.max(maxBytesToRead, PREFERRED_BUFFER_SIZE)];

        if (inputPos == inputSize) {
            inputSize = delegate.read(input, 0, Math.min(input.length, maxBytesToRead));
            inputPos = 0;
        }
        return inputSize == 0 ? delegate.read() : (inputSize < 0 ? -1 : input[inputPos++] & 0xFF);
    }

    /* --- --- --- Public interface: behave just as a normal InputStream --- --- --- */

    @Override public int available() throws IOException {
        return Math.max(0, override.size()-nextOverride) + delegate.available();
    }

    @Override public int read() throws IOException {
        while (true) {
            if (nextOverride < override.size())
                return override.get(nextOverride++) & 0xFF;
            nextOverride = override.clear().size();
            int val = delegate.read();
            if (val == -1) {
                currentParser.flush();
                if (!override.isEmpty())
                    return override.get(nextOverride++) & 0xFF;
                return -1; // EOF reached
            } else {
                currentParser = currentParser.feedByte(val);
            }
        }
    }

    @Override public int read(@Nonnull byte[] out, int off, int len) throws IOException {
        assert off+len <= out.length;
        int i = off, end = off+len, val = 0;
        while (i < end && val > -1) {
            int overrideLen = Math.min(end - i, override.size()-nextOverride);
            if (overrideLen > 0) {
                System.arraycopy(override.getArray(), nextOverride, out, i, overrideLen);
                if ((nextOverride += overrideLen) == override.size())
                    nextOverride = override.clear().size(); //exhausted override
                if ((i += overrideLen) == end)
                    break;                                  // exhausted out
            }
            val = nextInputByte(len);
            if (val >= 0) {
                currentParser = currentParser.feedByte(val);
            } else {
                currentParser.flush();
                if (!override.isEmpty()) val = 0; //do not break from loop
            }
        }

        int count = i - off;
        return count > 0 ? count : -1;
    }

    @Override public void close() throws IOException {
        delegate.close();
        super.close();
    }
}
