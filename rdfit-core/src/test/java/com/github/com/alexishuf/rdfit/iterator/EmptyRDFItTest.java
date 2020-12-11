package com.github.com.alexishuf.rdfit.iterator;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class EmptyRDFItTest {
    @Test
    public void isEmpty() {
        try (EmptyRDFIt<String> it = new EmptyRDFIt<>(String.class)) {
            assertEquals(it.valueClass(), String.class);
            assertFalse(it.hasNext());
        } //nothing thrown
    }
}