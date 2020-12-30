package com.github.lapesd.rdfit.components.parsers.impl.listener;

import com.github.lapesd.rdfit.components.parsers.BaseListenerParser;
import com.github.lapesd.rdfit.components.parsers.ListenerFeeder;
import com.github.lapesd.rdfit.errors.InterruptParsingException;
import com.github.lapesd.rdfit.errors.RDFItException;
import com.github.lapesd.rdfit.listener.RDFListener;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;

import static com.github.lapesd.rdfit.util.Utils.compactClass;

public abstract class BaseJavaListenerParser extends BaseListenerParser {
    public static final int CAN_PARSE_MAX = 128;

    public BaseJavaListenerParser(@Nonnull Class<?> acceptedClass,
                                  @Nullable Class<?> tripleClass, @Nullable Class<?> quadClass) {
        super(Collections.singletonList(acceptedClass), tripleClass, quadClass);
    }

    protected abstract @Nonnull Iterator<?> createIterator(@Nonnull Object source);

    @Override public boolean canParse(@Nonnull Object source) {
        if (!super.canParse(source)) return false;
        Iterator<?> it = createIterator(source);
        for (int i = 0; i < CAN_PARSE_MAX && it.hasNext(); ++i) {
            Object next = it.next();
            if ((tripleClass == null || !tripleClass.isInstance(next))
                    && (quadClass == null || !quadClass.isInstance(next)) ) {
                return false;
            }
        }
        return true;
    }

    protected boolean feed(@Nonnull ListenerFeeder feeder, @Nonnull Object element) {
        return feeder.feed(element);
    }

    @Override public void parse(@Nonnull Object source, @Nonnull RDFListener<?,?> listener) {
        try (ListenerFeeder feeder = createListenerFeeder(listener, source)) {
            try {
                for (Iterator<?> it = createIterator(source); it.hasNext(); ) {
                    if (!feed(feeder, it.next()))
                        break; // stop parsing this source
                }
            } catch (InterruptParsingException e) {
                throw e;
            } catch (Throwable t) {
                if (!listener.notifySourceError(RDFItException.wrap(source, t)))
                    throw new InterruptParsingException();
            }
        }
    }

    @Override public @Nonnull String toString() {
        return String.format("%s{triple=%s, quad=%s}", Utils.toString(this),
                             compactClass(tripleClass), compactClass(quadClass));
    }
}
