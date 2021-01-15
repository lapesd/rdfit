package com.github.lapesd.rdfit.util.impl;

import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.util.URLCache;
import com.github.lapesd.rdfit.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class WeighedURLCache implements URLCache {
    private static final double DEF_MAX_USED_MEMORY_RATE = 0.25;
    private static final int DEF_MIN_BYTES = 20*1024*1024, DEF_MAX_BYTES = Integer.MAX_VALUE;
    private static final WeighedURLCache INSTANCE = new WeighedURLCache(EternalCache.getDefault(),
            0.2, 50*1024*1024, 500*1024*1024);

    private final @Nonnull Map<String, RDFBlob> strongMap = new LinkedHashMap<>();
    private final @Nonnull Map<String, SoftReference<RDFBlob>> softMap = new HashMap<>();
    private @Nonnull URLCache parent;
    private double maxUsedMemoryRate;
    private int lowestBytesUsage, highestBytesUsage, bytesUsed = 0;


    public static @Nonnull WeighedURLCache getDefault() {
        return INSTANCE;
    }

    public WeighedURLCache() {
        this(EternalCache.getDefault());
    }

    public WeighedURLCache(@Nullable URLCache parent) {
        this(parent, DEF_MAX_USED_MEMORY_RATE, DEF_MIN_BYTES, DEF_MAX_BYTES);
    }

    public WeighedURLCache(@Nullable URLCache parent, double maxUsedMemoryRate,
                           int lowestBytesUsage, int highestBytesUsage) {
        this.parent = parent == null ? new EternalCache() : parent;
        this.maxUsedMemoryRate = maxUsedMemoryRate;
        this.lowestBytesUsage = lowestBytesUsage;
        this.highestBytesUsage = highestBytesUsage;
    }

    public @Nonnull URLCache getParent() {
        return parent;
    }

    public void setParent(@Nonnull URLCache parent) {
        this.parent = parent;
    }

    public double getMaxUsedMemoryRate() {
        return maxUsedMemoryRate;
    }

    public @Nonnull WeighedURLCache setMaxUsedMemoryRate(double maxUsedMemoryRate) {
        this.maxUsedMemoryRate = maxUsedMemoryRate;
        return this;
    }

    public int getLowestBytesUsage() {
        return lowestBytesUsage;
    }

    public @Nonnull WeighedURLCache setLowestBytesUsage(int lowestBytesUsage) {
        this.lowestBytesUsage = lowestBytesUsage;
        return this;
    }

    public int getHighestBytesUsage() {
        return highestBytesUsage;
    }

    public @Nonnull WeighedURLCache setHighestBytesUsage(int highestBytesUsage) {
        this.highestBytesUsage = highestBytesUsage;
        return this;
    }

    public int getStronglyReferencedBytes() {
        return bytesUsed;
    }

    /**
     * Evicts oldest entries from strongMap until bytesToEvict bytes have been evicted into softMap
     * @param bytesToEvict how many bytes to evict from strongMap
     */
    protected void evict(int bytesToEvict) {
        Iterator<Map.Entry<String, RDFBlob>> i;
        for (i = strongMap.entrySet().iterator(); bytesToEvict > 0 && i.hasNext();) {
            Map.Entry<String, RDFBlob> e = i.next();
            bytesToEvict -= e.getValue().getLength();
            softMap.put(e.getKey(), new SoftReference<>(e.getValue()));
            bytesUsed -= e.getValue().getLength();
        }
    }

    protected int getJVMUsedMemory() {
        Runtime r = Runtime.getRuntime();
        long used = r.totalMemory() - r.freeMemory();
        return used > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)used;
    }

    protected int allowedStrongSize() {
        int used = getJVMUsedMemory();
        return Math.min(Math.max((int)(maxUsedMemoryRate * used), lowestBytesUsage), highestBytesUsage);
    }

    protected void evict() {
        int bytesToEvict = bytesUsed - allowedStrongSize();
        if (bytesToEvict > 0)
            evict(bytesToEvict);
    }

    @Override
    public synchronized @Nullable Supplier<RDFInputStream>
    put(@Nonnull URL url, @Nonnull Supplier<RDFInputStream> supplier) {
        String key = Utils.toCacheKey(url);
        RDFBlob blob;
        try {
            blob = RDFBlob.fromSupplier(supplier);
        } catch (IOException e) {
            throw new RuntimeException("IOException on "+this+".put("+url+", supplier)", e);
        }
        Supplier<RDFInputStream> old = getLocal(url);
        if (old != null) {
            softMap.remove(key);
            bytesUsed -= ((RDFBlob)old).getLength();
        }
        strongMap.put(key, blob);
        bytesUsed += blob.getLength();
        evict();
        return old;
    }

    @Override
    public synchronized boolean putIfAbsent(@Nonnull URL url,
                                            @Nonnull Supplier<RDFInputStream> supplier) {
        if (getLocal(url) == null) {
            put(url, supplier);
            return true;
        }
        return false;
    }

    protected synchronized @Nullable Supplier<RDFInputStream> getLocal(@Nonnull URL url) {
        String key = Utils.toCacheKey(url);
        RDFBlob blob = strongMap.getOrDefault(key, null);

        if (blob == null) {
            SoftReference<RDFBlob> reference = softMap.getOrDefault(key, null);
            if (reference != null) {
                blob = reference.get();
                if (blob != null)
                    softMap.remove(key);
            }
        } else {
            strongMap.remove(key);
            bytesUsed -= blob.getLength();
        }
        if (blob != null) {
            strongMap.put(key, blob);
            bytesUsed += blob.getLength();
            evict();
        }
        return blob;
    }

    @Override public synchronized @Nullable Supplier<RDFInputStream> get(@Nonnull URL url) {
        Supplier<RDFInputStream> supplier = getLocal(url);
        return supplier == null ? parent.get(url) : supplier;
    }
}
