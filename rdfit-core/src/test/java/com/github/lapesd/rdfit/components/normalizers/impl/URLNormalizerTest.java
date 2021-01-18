/*
 *    Copyright 2021 Alexis Armin Huf
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.lapesd.rdfit.components.normalizers.impl;

import com.github.lapesd.rdfit.source.RDFInputStream;
import com.github.lapesd.rdfit.source.RDFInputStreamSupplier;
import com.github.lapesd.rdfit.util.Utils;
import com.google.common.base.Stopwatch;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class URLNormalizerTest {

    @Test
    public void testGetSKOS() throws Exception {
        URLNormalizer normalizer = new URLNormalizer();
        URL https = new URL("https://www.w3.org/2004/02/skos/core#");

        Stopwatch sw = Stopwatch.createStarted();
        Object normalized = normalizer.normalize(https);
        assertFalse(normalized instanceof RDFInputStreamSupplier);
        assertTrue(sw.elapsed(TimeUnit.MILLISECONDS) < 900);

        sw.reset().start();
        byte[] bytes = Utils.toBytes(((RDFInputStream) normalized).getInputStream());
        assertTrue(bytes.length > 15000);
        assertTrue(sw.elapsed(TimeUnit.MILLISECONDS) < 900);
    }

}