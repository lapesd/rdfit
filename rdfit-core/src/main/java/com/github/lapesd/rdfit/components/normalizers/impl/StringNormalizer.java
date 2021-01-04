package com.github.lapesd.rdfit.components.normalizers.impl;

import com.github.lapesd.rdfit.components.annotations.Accepts;
import com.github.lapesd.rdfit.components.normalizers.BaseSourceNormalizer;
import com.github.lapesd.rdfit.source.RDFBytesInputStream;
import com.github.lapesd.rdfit.source.syntax.RDFLangs;
import com.github.lapesd.rdfit.source.syntax.impl.RDFLang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

@Accepts({String.class, CharSequence.class})
public class StringNormalizer extends BaseSourceNormalizer {
    private static final Logger logger = LoggerFactory.getLogger(StringNormalizer.class);
    private static final Pattern URI_RX = Pattern.compile("(?i)^\\w+:");

     @Override public @Nonnull Object normalize(@Nonnull Object source) {
         if (!(source instanceof CharSequence))
             return source;
         String string = source.toString();
         if (URI_RX.matcher(string).find()) {
             try {
                 return new URL(string);
             } catch (MalformedURLException e) {
                 File file = new File(string);
                 if (file.exists())
                     return file;
                 logger.warn("Source {} looks like an URL, but is neither an URL, nor a file path",
                             source);
                 return source;
             }
         }
         File file = new File(string);
         if (file.exists())
             return file;
         byte[] bytes = string.getBytes(UTF_8);
         try (InputStream is = new ByteArrayInputStream(bytes)) {
             RDFLang lang = RDFLangs.guess(is, Integer.MAX_VALUE);
             if (RDFLangs.isKnown(lang))
                 return new RDFBytesInputStream(bytes, lang);
         } catch (IOException e) {
             logger.error("Ignoring unexpected IOException reading a ByteArrayInputStream", e);
         }
         return source;
     }
}