/*
 *    Copyright 2021 Alexis Armin Huf
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.lapesd.rdfit.util;


import javax.annotation.Nonnull;

import static java.lang.Character.isWhitespace;

public class LiteralParser {
    private final @Nonnull StringBuilder lexicalForm = new StringBuilder();
    private final @Nonnull StringBuilder lang        = new StringBuilder();
    private final @Nonnull StringBuilder typeIRI     = new StringBuilder();
    private byte[] utf8buf = new byte[4];
    private char[] utf8decoded = new char[2];
    private int utf8total = 0, utf8next = 0;
    private char quoteChar = '\0', iriBegin = '\0';
    private int openQuotes, closedQuotes, hats;
    private boolean escaped = false;
    private Symbol symbol = Symbol.BEGIN;

    private enum Symbol {
        BEGIN, OPEN, LEX, SEP, LANG, TYPE
    }

    public void reset() {
        lexicalForm.setLength(0);
        lang.setLength(0);
        typeIRI.setLength(0);
        quoteChar = iriBegin = '\0';
        openQuotes = closedQuotes = hats = 0;
        escaped = false;
        symbol = Symbol.BEGIN;
        utf8total = utf8next = 0;
    }

    private boolean feed(@Nonnull Symbol symbol, char c) {
        this.symbol = symbol;
        return feed(c);
    }


    public @Nonnull Literal parse(@Nonnull CharSequence sequence) {
        reset();
        try {
            for (int i = 0, size = sequence.length(); i < size; i++)
                feed(sequence.charAt(i));
            return end();
        } finally {
            reset();
        }
    }

    public @Nonnull Literal parseFirst(@Nonnull CharSequence sequence) {
        reset();
        try {
            for (int i = 0, size = sequence.length(); i < size; i++) {
                if (feed(sequence.charAt(i)))
                    return end();
            }
            return end();
        } finally {
            reset();
        }
    }

    public boolean feedByte(byte value) {
        int bits = value & 0xff;
        if (utf8total == 0) {
            int i = value >>> 7;
            if (bits >>> 7 == 0) {
                return feed((char)value);
            } else if ((bits >>> 5) == 0x6) {
                utf8total = 2;
            } else if ((bits >>> 4) == 0xe) {
                utf8total = 3;
            } else if ((bits >>> 3) == 0x1e) {
                utf8total = 4;
            }
            assert utf8total > 0;
            utf8next = 0;
        } else if (bits >> 6 != 0x02) {
            // invalid UTF-8
            boolean last = false;
            for (int i = 0; i < utf8total; i++)
                last = feed((char)utf8buf[i]);
            utf8total = 0;
            return last;
        }

        utf8buf[utf8next++] = value;
        if (utf8next != utf8total)
            return false;
        int code;
        if (utf8total == 2) {
            code = (utf8buf[1] & 0x3f) | ((utf8buf[0] & 0x1f) << 6);
        } else if (utf8total == 3) {
            code = (utf8buf[2] & 0x3f) | ((utf8buf[1] & 0x3f) << 6) | ((utf8buf[0] & 0x0f) << 12);
        } else {
            assert utf8total == 4;
            code = (utf8buf[3] & 0x3f) | ((utf8buf[2] & 0x3f) << 6)
                    | ((utf8buf[1] & 0x3f) << 12) | ((utf8buf[0] & 0x07) << 18);
        }

        boolean last = false;
        for (int i = 0, n = Character.toChars(code, utf8decoded, 0); i < n; ++i)
            last = feed(utf8decoded[i]);
        utf8total = 0;
        return last;
    }

    public boolean feed(char c) {
        if (symbol == Symbol.BEGIN) {
            if (Character.isWhitespace(c) || c == '.' || c == ',' || c == ';')
                return false;
            else if (c != '\'' && c != '"')
                return feed(Symbol.LEX, c);
            else
                return feed(Symbol.OPEN, c);
        } else if (symbol == Symbol.OPEN) {
            return feedOpen(c);
        } else if (symbol == Symbol.LEX) {
            return feedLex(c);
        } else if (symbol == Symbol.SEP) {
            return feedSep(c);
        } else if (symbol == Symbol.LANG) {
            return feedLang(c);
        } else if (symbol == Symbol.TYPE) {
            return feedType(c);
        }
        return false;
    }

    private boolean feedLang(char c) {
        if (c == '_') c = '-';
        if (c == '-' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                     || (c >= '0' && c <= '9')) {
            lang.append(c);
        } else {
            return true;
        }
        return false;
    }

    private boolean feedType(char c) {
        if (iriBegin == '\0') {
            iriBegin = c;
            if (c != '<') typeIRI.append(c);
        } else if (iriBegin == '<') {
            if (c == '>') return true;
            typeIRI.append(c);
            return false;
        } else if (c == ',' || c == ';' || isWhitespace(c)) {
            end();
            return true;
        } else {
            typeIRI.append(c);
        }
        return false;
    }

    private boolean feedSep(char c) {
        if (c == '@') {
            symbol = Symbol.LANG;
        } else if (c == '^' && hats == 0) {
            ++hats;
        } else if (c == '^' && hats == 1) {
            symbol = Symbol.TYPE;
        } else if (Character.isWhitespace(c) || c == ',' || c == ';' || c == '.') {
            return true; //finished
        } else { //bad input, go back to LEX state
            for (int i = 0; i < openQuotes; i++) lexicalForm.append('\\').append(quoteChar);
            for (int i = 0; i < hats;       i++) lexicalForm.append('^');
            closedQuotes = hats = 0;
            feed(Symbol.LEX, c);
        }
        return false;
    }

    private boolean feedOpen(char c) {
        if (quoteChar == '\0') {
            if (c == '\'' || c == '"') {
                quoteChar = c;
                openQuotes = 1;
            } else {
                return feed(Symbol.LEX, c);
            }
        } else if (c == quoteChar) {
            ++openQuotes;
            if (openQuotes > 3) {
                lexicalForm.append('\\').append(c);
                symbol = Symbol.LEX;
            }
        } else {
            return feed(Symbol.LEX, c);
        }
        return false;
    }

    private boolean feedLex(char c) {
        if (quoteChar == '\0' && Character.isWhitespace(c))
            return true;
        if (openQuotes == 2) {
            openQuotes = 1;
            lexicalForm.append('\\').append(quoteChar);
        }
        if (escaped) {
            escaped = false;
            lexicalForm.append(c);
        } else if (c == '\\') {
            escaped = true;
            lexicalForm.append(c);
        } else if (c == quoteChar) {
            ++closedQuotes;
            if (closedQuotes == openQuotes) symbol = Symbol.SEP;
        } else {
            for (; closedQuotes > 0; --closedQuotes)
                lexicalForm.append(quoteChar);
            lexicalForm.append(c);
            closedQuotes = 0;
        }
        return false;
    }

    public boolean endsInDot() {
        return typeIRI.length() > 0 && typeIRI.charAt(typeIRI.length()-1) == '.';
    }

    public @Nonnull Literal end() {
        if (endsInDot())
            typeIRI.setLength(typeIRI.length()-1);
        Literal.QuotationStyle q = Literal.QuotationStyle.fromChar(quoteChar, openQuotes);
        if (q == Literal.QuotationStyle.NONE)
            return Literal.unquoted(lexicalForm.toString());
        if (lang.length() > 0)
            return Literal.lang(q, lexicalForm.toString(), lang.toString());
        if (typeIRI.length() == 0)
            return Literal.plain(q, lexicalForm.toString());
        if (iriBegin == '<')
            return Literal.iriTyped(q, lexicalForm.toString(), typeIRI.toString());

        String type = typeIRI.toString();
        int idx = type.indexOf(':');
        if (idx < 1)
            return Literal.iriTyped(q, lexicalForm.toString(), type);
        String prefixName = type.substring(0, idx);
        String localName = type.substring(idx + 1);
        return Literal.prefixTyped(q, lexicalForm.toString(), prefixName, localName);
    }

    public @Nonnull Literal endAndReset() {
        Literal lit = end();
        reset();
        return lit;
    }
}
