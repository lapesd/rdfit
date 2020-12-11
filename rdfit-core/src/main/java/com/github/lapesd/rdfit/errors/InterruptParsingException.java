package com.github.lapesd.rdfit.errors;

import com.github.lapesd.rdfit.iterator.RDFIt;

/**
 * A exception that is thrown to force an early stop of parsing.
 *
 * This does not represent an error. It is used to make stop callback-based parsers to
 * stop processing if the end consumer closes a {@link RDFIt} being fed by a thread running
 * that parser.
 */
public class InterruptParsingException extends RuntimeException {

}
