package com.github.lapesd.rdfit.iterator;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class EmptyRDFItTest {
    @DataProvider public static Object[][] isEmptyData() {
        return Stream.of(
                asList(String.class, IterationElement.TRIPLE),
                asList(String.class, IterationElement.QUAD)
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "isEmptyData")
    public void testIsEmpty(@Nonnull Class<?> valueClass, @Nonnull IterationElement itEl) {
        Object source = new Object();
        //noinspection unchecked
        try (EmptyRDFIt<String> it = new EmptyRDFIt<>((Class<String>) valueClass, itEl, source)) {
            assertEquals(it.valueClass(), valueClass);
            assertEquals(it.itElement(), itEl);
            assertFalse(it.hasNext());
            assertSame(it.getSource(), source);
        } //nothing thrown
    }
}