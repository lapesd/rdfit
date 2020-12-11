package com.github.com.alexishuf.rdfit.errors;

import com.github.com.alexishuf.rdfit.iterator.RDFIt;
import com.github.com.alexishuf.rdfit.components.CallbackParser;

/**
 * A exception that is thrown to force an early return of a {@link CallbackParser#parse()} call.
 *
 * This does not represent an error. It is used to make stop callback-based parsers to
 * stop processing if the end consumer closes a {@link RDFIt} being fed by a thread running
 * that parser.
 */
public class InterruptParsingException extends RuntimeException {

}
