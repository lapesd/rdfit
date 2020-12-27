package com.github.lapesd.rdfit.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class Literal {
    public enum QuotationStyle {
        SINGLE_SINGLE,
        MULTI_SINGLE,
        SINGLE_DOUBLE,
        MULTI_DOUBLE,
        NONE;

        public boolean isSingleQuote() {
            return this == SINGLE_SINGLE || this == MULTI_SINGLE;
        }

        public boolean isDoubleQuote() {
            return this == SINGLE_DOUBLE || this == MULTI_DOUBLE;
        }

        public boolean isMultiline() {
            return this == MULTI_SINGLE || this == MULTI_DOUBLE;
        }

        public void appendEscaped(@Nonnull StringBuilder b, char c) {
            boolean escape = (isMultiline() && (c == '\r' || c == '\n'))
                    || (isSingleQuote() && c == '\'')
                    || (isDoubleQuote() && c == '"')
                    || c == '\\';
            if (escape)
                b.append('\\').append(c);
        }

        public static @Nonnull QuotationStyle fromChar(char c, int num) {
            if (c == '\0' || num == 0)
                return NONE;
            else if (c != '\'' && c != '"')
                throw new IllegalArgumentException("Bad quote char: '"+c+"'");
            else if (num == 1)
                return c == '\'' ? SINGLE_SINGLE : SINGLE_DOUBLE;
            else
                return c == '\'' ? MULTI_SINGLE : MULTI_DOUBLE;
        }

        public static @Nonnull QuotationStyle fromQuotation(@Nonnull CharSequence seq) {
            char first = '\0';
            int i = 0;
            for (int size = seq.length(); i < size; i++) {
                char c = seq.charAt(i);
                if (first == '\0') {
                    if (c == '\'' || c == '"')
                        first = c;
                    else
                        return NONE;
                } else if (c != first) {
                    if (i == 1)
                        return c == '\'' ? MULTI_SINGLE : MULTI_DOUBLE;
                    else
                        return c == '\'' ? SINGLE_SINGLE : SINGLE_DOUBLE;
                }
            }
            return first == '\0' ? NONE : (first == '\'' ? SINGLE_SINGLE : SINGLE_DOUBLE);
        }

        public @Nonnull String getString() {
            switch (this) {
                case SINGLE_SINGLE: return "'";
                case  MULTI_SINGLE: return "'''";
                case SINGLE_DOUBLE: return "\"";
                case  MULTI_DOUBLE: return "\"\"\"";
                case          NONE: return "";
            }
            throw new UnsupportedOperationException();
        }
    }

    private final @Nonnull QuotationStyle quotation;
    private final @Nonnull String lexicalForm;
    private final @Nullable String langTag, typeIRI;
    private final @Nullable String typePrefixName, typeLocalName;

    protected Literal(@Nonnull QuotationStyle quotation, @Nonnull String lexicalForm,
                      @Nullable String langTag, @Nullable String typeIRI,
                      @Nullable String typePrefixName, @Nullable String typeLocalName) {
        this.quotation = quotation;
        this.lexicalForm = lexicalForm;
        this.langTag = langTag;
        this.typeIRI = typeIRI;
        this.typePrefixName = typePrefixName;
        this.typeLocalName = typeLocalName;
    }
    public static @Nonnull Literal unquoted(@Nonnull String lexicalForm) {
        return new Literal(QuotationStyle.NONE, lexicalForm, null, null, null, null);
    }
    public static @Nonnull Literal plain(@Nonnull QuotationStyle quotation,
                                         @Nonnull String lexicalForm) {
        return new Literal(quotation, lexicalForm, null, null, null, null);
    }
    public static @Nonnull Literal lang(@Nonnull QuotationStyle quotation,
                                        @Nonnull String lexicalForm,
                                        @Nonnull String langTag) {
        return new Literal(quotation, lexicalForm, langTag, null, null, null);
    }
    public static @Nonnull Literal iriTyped(@Nonnull QuotationStyle quotation,
                                            @Nonnull String lexicalForm, @Nonnull String typeIRI) {
        return new Literal(quotation, lexicalForm, null, typeIRI, null, null);
    }

    public static @Nonnull Literal prefixTyped(@Nonnull QuotationStyle quotation,
                                               @Nonnull String lexicalForm,
                                               @Nonnull String typePrefixName,
                                               @Nonnull String typeLocalName) {
        return new Literal(quotation, lexicalForm, null, null, typePrefixName, typeLocalName);
    }

    public boolean isQuoted() {
        return quotation != QuotationStyle.NONE;
    }

    public @Nonnull QuotationStyle getQuotation() {
        return quotation;
    }

    public @Nonnull String getLexicalForm() {
        return lexicalForm;
    }
    public @Nullable String getLangTag() {
        return langTag;
    }

    public boolean isLang() {
        return langTag != null;
    }

    public @Nullable String getTypeIRI() {
        return typeIRI;
    }

    public boolean isTyped() {
        return isIRITyped() || isPrefixTyped();
    }

    public boolean isIRITyped() {
        return typeIRI != null;
    }

    public boolean isPrefixTyped() {
        return typePrefixName != null || typeLocalName != null;
    }

    public @Nullable String getTypePrefixName() {
        return typePrefixName;
    }

    public @Nullable String getTypeLocalName() {
        return typeLocalName;
    }

    public @Nullable String getPrefixedType() {
        String prefix = typePrefixName == null ? "" : typePrefixName;
        String local  = typeLocalName  == null ? "" : typeLocalName;
        return prefix.replaceAll(":$", "") + ":" + local.replaceAll("^:", "");
    }

    public @Nonnull String toNT() {
        if (quotation == QuotationStyle.NONE)
            return lexicalForm;
        StringBuilder b = new StringBuilder();
        b.append(quotation.getString()).append(getLexicalForm()).append(quotation.getString());
        if (isLang())
            b.append('@').append(getLangTag());
        else if (getTypeIRI() != null)
            b.append("^^").append(typeIRI);
        else if (typePrefixName != null || typeLocalName != null)
            b.append("^^").append(getPrefixedType());
        return b.toString();
    }

    @Override public @Nonnull String toString() {
        return toNT();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Literal)) return false;
        Literal literal = (Literal) o;
        return getQuotation() == literal.getQuotation()
                && getLexicalForm().equals(literal.getLexicalForm())
                && Objects.equals(getLangTag(), literal.getLangTag())
                && Objects.equals(getTypeIRI(), literal.getTypeIRI())
                && Objects.equals(getTypePrefixName(), literal.getTypePrefixName())
                && Objects.equals(getTypeLocalName(), literal.getTypeLocalName());
    }

    @Override public int hashCode() {
        return Objects.hash(getQuotation(), getLexicalForm(), getLangTag(), getTypeIRI(),
                            getTypePrefixName(), getTypeLocalName());
    }
}
