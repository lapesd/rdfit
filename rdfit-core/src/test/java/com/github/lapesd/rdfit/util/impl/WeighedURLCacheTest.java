package com.github.lapesd.rdfit.util.impl;

import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.util.Utils;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static org.testng.Assert.*;

public class WeighedURLCacheTest {
    private static final String EX = "http://example.org/";
    private static final String SUPPLIER_FORMAT = "@prefix <"+EX+">.\n ex:S%1$d ex:P%1$d ex:O%1$d.\n";
    private static final URL ex1, ex2;

    static {
        try {
            ex1 = new URL(EX + "1.ttl");
            ex2 = new URL(EX + "2.ttl");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static @Nonnull Supplier<RDFInputStream> createSupplier(int id) {
        return () -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (Writer w = new OutputStreamWriter(out)) {
                w.write(String.format(SUPPLIER_FORMAT, id));
            } catch (IOException e) {
                throw new RuntimeException("Unexpected IOException", e);
            }
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            return new RDFInputStream(in);
        };
    }

    private static int supplierLength(int id) {
        return String.format(SUPPLIER_FORMAT, id).length();
    }

    private static void assertSameSupplier(@Nullable Supplier<RDFInputStream> actual,
                                           int expectedId) {
        assertTrue(expectedId >= 0);
        assertNotNull(actual);
        try {
            byte[] acBytes = Utils.toBytes(actual.get().getInputStream());
            String exString = String.format(SUPPLIER_FORMAT, expectedId);
            ByteArrayOutputStream baOut = new ByteArrayOutputStream();
            try (Writer writer = new OutputStreamWriter(baOut)) {
                writer.write(exString);
            }
            assertEquals(acBytes, baOut.toByteArray());
        } catch (IOException e) {
            throw new AssertionError("Unexpected IOException", e);
        }
    }

    @Test
    public void testGetFromParent() {
        EternalCache parent = new EternalCache();
        WeighedURLCache cache = new WeighedURLCache(parent);
        Supplier<RDFInputStream> supplier = createSupplier(2);
        parent.put(ex2, supplier);
        assertNull(cache.get(ex1));
        assertSame(cache.get(ex2), supplier);
    }

    @Test
    public void testEnforceMinimumSize() throws Exception {
        double maxUsedMemoryRate = 0.0000000000001;
        int minBytesUsage = 1024 * 1024;
        WeighedURLCache cache = new WeighedURLCache(null, maxUsedMemoryRate,
                                                    minBytesUsage, Integer.MAX_VALUE);
        int bytesUsedSetup = 0, minUnits = 0;
        for (; bytesUsedSetup < minBytesUsage; ++minUnits) {
            cache.put(new URL(EX+minUnits), createSupplier(minUnits));
            bytesUsedSetup += supplierLength(minUnits);
        }
        if (bytesUsedSetup > minBytesUsage)
            --minUnits; // last one should have caused an eviction from strong
        assertTrue(cache.getStronglyReferencedBytes() <= minBytesUsage);

        // check this test premise: if this does not hold, a false PASS may occur
        Runtime runtime = Runtime.getRuntime();
        double allowedByRate = (runtime.totalMemory() - runtime.freeMemory()) * maxUsedMemoryRate;
        assertTrue(allowedByRate < minBytesUsage);

        // all entries still cached
        for (int i = 0; i < minUnits; i++)
            assertSameSupplier(cache.get(new URL(EX + i)), i);

        // try to make SoftReference<>s be collected
        for (int i = 0; i < 10; i++) {
            runtime.gc();
            Thread.sleep(10);
        }

        // all entries still cached (everything was really on strong refs)
        for (int i = 0; i < minUnits; i++)
            assertSameSupplier(cache.get(new URL(EX + i)), i);

        // nevertheless, we should be above the limit set by the used memory rate
        assertTrue(cache.getStronglyReferencedBytes() > allowedByRate);

        // add more entries to tip over the limit
        for (int i = 0; i < minUnits; i++)
            cache.put(new URL(EX+"2/"+i), createSupplier(i));
        // check strong byte usage remains valid
        assertTrue(cache.getStronglyReferencedBytes() > allowedByRate);
        assertTrue(cache.getStronglyReferencedBytes() <= minBytesUsage);


        // add yet more entries, but keep track of instances
        for (int i = 0; i < minUnits; i++) {
            cache.put(new URL(EX+"3/"+i), createSupplier(i));
        }
        // verify that the entire last batch is accessible
        for (int i = 0; i < minUnits; i++)
            assertSameSupplier(cache.getLocal(new URL(EX+"3/"+i)), i);
        // check strong byte usage remains valid
        assertTrue(cache.getStronglyReferencedBytes() > allowedByRate);
        assertTrue(cache.getStronglyReferencedBytes() <= minBytesUsage);

        // try to make SoftReference<>s be collected
        for (int i = 0; i < 10; i++) {
            runtime.gc();
            Thread.sleep(10);
        }

        // verify that SoftReference collections did not affect the last batch
        for (int i = 0; i < minUnits; i++)
            assertSameSupplier(cache.getLocal(new URL(EX+"3/"+i)), i);
        // check strong byte usage remains valid
        assertTrue(cache.getStronglyReferencedBytes() > allowedByRate);
        assertTrue(cache.getStronglyReferencedBytes() <= minBytesUsage);
    }

    @Test
    public void testKeepSoftReferences() throws Exception {
        int lowestCapacity = 1024;
        WeighedURLCache cache = new WeighedURLCache(null, 0.00000001,
                                                    lowestCapacity, Integer.MAX_VALUE);
        int usedOnSetup = 0, nEntries = 0;
        for (; usedOnSetup < lowestCapacity; ++nEntries) {
            cache.put(new URL(EX+nEntries), createSupplier(nEntries));
            usedOnSetup += supplierLength(nEntries);
        }
        if (usedOnSetup > lowestCapacity)
            --nEntries;
        int[] dummy = new int[nEntries*4];
        for (int i = 0; i < nEntries; i++) {
            Supplier<RDFInputStream> supplier = cache.get(new URL(EX + i));
            assertSameSupplier(supplier, i);
            dummy[i] = Objects.requireNonNull(supplier).hashCode();
        }
        System.out.println(Arrays.hashCode(dummy)); // prevent the compiler from skipping allocation

        // Run the GC
        Runtime runtime = Runtime.getRuntime();
        for (int i = 0; i < 7; i++) {
            runtime.gc();
            Thread.sleep(50);
        }
        //noinspection UnusedAssignment
        dummy = null; // allow this to be collected before the soft refs to be created

        //send all suppliers to soft storage
        for (int i = 0; i < nEntries; i++)
            assertNull(cache.put(new URL(EX+"2/"+i), createSupplier(i)));
        // should be in soft reference, but still alive
        assertSameSupplier(cache.get(new URL(EX+(nEntries-2))), nEntries-2);
    }

    @Test
    public void testObeyHighestBytesUsage() throws MalformedURLException {
        int lowerLimit = 1024, upperLimit = 2048;
        double maxRate = 1.0;
        WeighedURLCache cache = new WeighedURLCache(null, maxRate, lowerLimit, upperLimit);

        int bytesAdded = 0;
        for (int i = 0; bytesAdded < upperLimit*4; ++i) {
            assertNull(cache.put(new URL(EX + i), createSupplier(i)));
            bytesAdded += supplierLength(i);
            assertTrue(cache.getStronglyReferencedBytes() <= upperLimit);
        }
        assertTrue(cache.getStronglyReferencedBytes() <= upperLimit);
    }

    private static int usedMemBytes() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        return used < Integer.MAX_VALUE ? (int) used : Integer.MAX_VALUE;
    }

    @Test
    public void testObeyMemoryRateLimit() throws MalformedURLException {
        int lowerLimit = 128, upperLimit = Integer.MAX_VALUE, bytesToAdd = 40 * 1024 * 1024;
        double maxRate = 0.25 ;

        List<Supplier<RDFInputStream>> suppliers = new ArrayList<>();
        WeighedURLCache cache = new WeighedURLCache(null, maxRate, lowerLimit, upperLimit);
        for (int i = 0, bytesAdded = 0; bytesAdded < bytesToAdd; ++i) {
            Supplier<RDFInputStream> supplier = createSupplier(i);
            suppliers.add(supplier);
            assertNull(cache.put(new URL(EX+i), supplier));
            bytesAdded += supplierLength(i);

            double maxAllowed = (maxRate + 0.05) * usedMemBytes() + 1024;
            assertTrue(cache.getStronglyReferencedBytes() < maxAllowed);
        }

        System.out.println(suppliers.hashCode()); //prevent the compiler from discard the List
    }
}