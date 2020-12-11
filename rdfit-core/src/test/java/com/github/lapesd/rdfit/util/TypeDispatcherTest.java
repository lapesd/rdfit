package com.github.lapesd.rdfit.util;

import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.testng.Assert.*;

public class TypeDispatcherTest {
    private static class Handler {
        private final @Nonnull Collection<Object> blacklist;

        public Handler() {
            this(Collections.emptySet());
        }

        public Handler(@Nonnull Collection<Object> blacklist) {
            this.blacklist = blacklist;
        }
    }

    private static class TestDispatcher extends TypeDispatcher<Handler> {
        @Override protected boolean accepts(@Nonnull Handler handler, @Nonnull Object instance) {
            return !handler.blacklist.contains(instance);
        }
    }

    @Test
    public void testGetExactClass() {
        TestDispatcher dispatcher = new TestDispatcher();
        Handler h1 = new Handler();
        dispatcher.add(String.class, h1);
        Iterator<Handler> it = dispatcher.get("test");
        assertTrue(it.hasNext());
        assertEquals(it.next(), h1);
        assertFalse(it.hasNext());
    }

    @Test
    public void testRejectByInstance() {
        TestDispatcher dispatcher = new TestDispatcher();
        Handler h1 = new Handler(singleton("BAD"));
        dispatcher.add(String.class, h1);

        Iterator<Handler> it = dispatcher.get("BAD");
        assertFalse(it.hasNext());

        it = dispatcher.get("OK");
        assertTrue(it.hasNext());
        assertSame(it.next(), h1);
    }

    @Test
    public void testRejectByInstanceGetSuperclass() {
        TestDispatcher dispatcher = new TestDispatcher();
        Handler dHandler = new Handler(   asList(3.14, 5.2));
        Handler nHandler = new Handler(singleton(3.14));
        dispatcher.add(Double.class, dHandler);
        dispatcher.add(Number.class, nHandler);

        Iterator<Handler> it = dispatcher.get(7.8);
        assertTrue(it.hasNext());
        assertSame(it.next(), dHandler);
        assertTrue(it.hasNext());
        assertSame(it.next(), nHandler);
        assertFalse(it.hasNext());

        it = dispatcher.get(5.2);
        assertTrue(it.hasNext());
        assertSame(it.next(), nHandler);
        assertFalse(it.hasNext());

        it = dispatcher.get(3.14);
        assertFalse(it.hasNext());
    }
}