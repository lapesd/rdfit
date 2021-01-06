package com.github.lapesd.rdfit;

import com.github.lapesd.rdfit.generators.SourceGenerator;
import com.github.lapesd.rdfit.util.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.PrefixMappingMem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestData {
    public final @Nonnull TripleSet set;
    public final @Nonnull SourceGenerator generator;
    public final @Nullable Class<?> tripleClass, quadClass;
    private final @Nonnull List<File> tempDirs = new ArrayList<>();

    public TestData(@Nonnull TripleSet tripleSet, @Nonnull SourceGenerator generator,
                    @Nullable Class<?> tripleClass, @Nullable Class<?> quadClass) {
        if (tripleClass == null && quadClass == null)
            throw new IllegalArgumentException("triple and quad class cannot be both null");
        this.set = tripleSet;
        this.generator = generator;
        this.tripleClass = tripleClass;
        this.quadClass = quadClass;
    }

    public boolean isTripleOnly() {
        return quadClass == null;
    }
    public boolean isQuadOnly() {
        return tripleClass == null;
    }
    public boolean hasBoth() {
        return tripleClass != null && quadClass != null;
    }

    public @Nonnull List<?> generateInputs() {
        try {
            File dir = Files.createTempDirectory("rdfit").toFile();
            tempDirs.add(dir);
            return generator.generate(set, dir);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public void cleanUp() {
        for (File dir : tempDirs) {
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        tempDirs.clear();
    }

    public @Nonnull List<?> expectedTriples() {
        if (tripleClass == null)
            return Collections.emptyList();
        return quadClass == null ? set.export(tripleClass) : set.onlyTriples(tripleClass);
    }

    public @Nonnull List<?> expectedQuads() {
        if (quadClass == null)
            return Collections.emptyList();
        return tripleClass == null ? set.export(quadClass) : set.onlyQuads(quadClass);
    }

    public @Nonnull TripleSet getTripleSet() {
        return set;
    }

    public @Nonnull SourceGenerator getGenerator() {
        return generator;
    }

    public @Nonnull Class<?> getTripleClass() {
        if (tripleClass == null) throw new NullPointerException();
        return tripleClass;
    }

    public @Nonnull Class<?> getQuadClass() {
        if (quadClass == null) throw new NullPointerException();
        return quadClass;
    }

    private void toString(@Nonnull StringBuilder b, @Nonnull Node node) {
        if (node.isBlank()) {
            b.append("_:").append(node.getBlankNodeLabel());
        } else if (node.isURI()) {
            b.append('<').append(node.getURI().replace("http://example.org/", "ex:")).append('>');
        } else if (node.isLiteral()) {
            PrefixMapping pm = new PrefixMappingMem();
            pm.setNsPrefix("xsd", XSD.getURI());
            pm.setNsPrefix("rdf", RDF.getURI());
            b.append(node.toString(pm, true));
        } else {
            b.append(node.toString());
        }
        b.append(' ');
    }

    @Override public @Nonnull String toString() {
        StringBuilder b = new StringBuilder();
        b.append(Utils.compactClass(tripleClass)).append(", ");
        b.append(Utils.compactClass(quadClass));
        b.append(", ").append(Utils.toString(generator)).append(" :: ");
        for (Quad q : set.export(Quad.class)) {
            if (!Quad.defaultGraphIRI.equals(q.getGraph()))
                toString(b, q.getGraph());
            toString(b, q.getSubject());
            toString(b, q.getPredicate());
            toString(b, q.getObject());
            b.append(".\n");
        }
        b.setLength(b.length()-1);
        return b.toString();
    }
}
