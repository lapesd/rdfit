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

package com.github.lapesd.rdfit.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class UtilsTest {
    private static final String EX = "http://example.org";

    @DataProvider public @Nonnull Object[][] createURIOrFixData() {
        return Stream.of(
                asList(EX, URI.create(EX)),
                asList(EX+"/a-_,X?query[]=1#fragment", URI.create(EX+"/a-_,X?query[]=1#fragment")),
                asList(EX+"/a b", URI.create(EX+"/a%20b")),
                asList(EX+"/a b?query=`", URI.create(EX+"/a%20b?query=%60")),
                asList(EX+"/a|b", URI.create(EX+"/a%7Cb")),
                asList(EX+"/a%20|b", URI.create(EX+"/a%20%7Cb")),
                asList("", URI.create("")),
                asList(":", null)
        ).map(List::toArray).toArray(Object[][]::new);
    }

    @Test(dataProvider = "createURIOrFixData")
    public void testCreateURIOrFix(@Nonnull String in, @Nullable URI expected) throws URISyntaxException {
        try {
            URI uri = Utils.createURIOrFix(in);
            if (expected == null)
                fail("Expected an URISyntaxException to be thrown for in="+in);
            assertEquals(uri, expected);
        } catch (URISyntaxException e) {
            if (expected != null)
                throw e;
        }
    }

}