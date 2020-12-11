package com.github.lapesd.rdfit.source.syntax;

import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface LangDetector {
    interface State {
        /**
         * Feed the next input byte to the detector, which may reach a conclusion or return null
         *
         * @param value next byte value read from input
         * @return null if detection is inconclusive, {@link RDFLangs#UNKNOWN} if the detection
         * is negative (further feeding will never produce a different result) or another
         * {@link RDFLang} if a syntax has been conclusively identified.
         */
        @Nullable RDFLang feedByte(byte value);

        /**
         * Some syntaxes have ambiguous document starts. This occurs for superset languages
         * (e.g., TTL and NT), where a detection of "[", ",", ";" or "a" may occur only after
         * several triples. Due to this feedByte() may return null for the last byte read from
         * input after reaching a maximum allowed byte reads.
         *
         * This method informs the detector that feedByte() will not be called anymore and
         * the detector should output its best (and safest) guess, if any.
         *
         * @return null if detection remains inconclusive, {@link RDFLangs#UNKNOWN} if the
         * input corresponds to no known syntax or any other {@link RDFLang} value if the
         * syntax could be determined or can be safely guessed.
         */
        @Nullable RDFLang end();
    }

    @Nonnull State createState();
}
