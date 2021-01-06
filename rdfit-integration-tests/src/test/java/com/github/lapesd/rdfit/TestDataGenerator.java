package com.github.lapesd.rdfit;

import com.github.lapesd.rdfit.generators.SourceGenerator;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@SuppressWarnings("UnstableApiUsage")
public class TestDataGenerator {
    private static final String EX = "http://example.org/";
    private static final Node S = NodeFactory.createURI(EX+"S");
    private static final Node P = NodeFactory.createURI(EX+"P");
    private static final Node G = NodeFactory.createURI(EX+"G");
    private static final Object nil = new Object();
    private static final List<Object> tripleClasses = asList(Triple.class, Statement.class,
            Quad.class, org.eclipse.rdf4j.model.Statement.class, nil);
    private static final List<Object> quadClasses = asList(Quad.class,
            org.eclipse.rdf4j.model.Statement.class, nil);
    private static final List<Node> objects = asList(
            NodeFactory.createURI(EX+"O1"),
            NodeFactory.createLiteral("O3"),
            NodeFactory.createLiteral("O4", "en"),
            NodeFactory.createLiteral("O4", XSDDatatype.XSDstring),
            NodeFactory.createLiteral("1", XSDDatatype.XSDinteger),
            NodeFactory.createLiteral("true", XSDDatatype.XSDboolean)
    );
    private static final List<Node> graphs = asList(
            Quad.defaultGraphIRI,
            NodeFactory.createURI(EX+"G1")
    );
    private static final List<SourceGenerator> generators;

    static {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String pkg = "com.github.lapesd.rdfit.generators";
            List<SourceGenerator> plainGens = new ArrayList<>(), allGens;
            List<Class<?>> chainingClasses = new ArrayList<>();
            for (ClassPath.ClassInfo info : ClassPath.from(classLoader).getTopLevelClasses(pkg)) {
                Class<?> cls = info.load();
                Class<SourceGenerator> interfaceCls = SourceGenerator.class;
                if (interfaceCls.isAssignableFrom(cls) && !interfaceCls.equals(cls)) {
                    try {
                        Constructor<?> ct = cls.getConstructor();
                        plainGens.add((SourceGenerator)ct.newInstance());
                    } catch (NoSuchMethodException e) {
                        chainingClasses.add(cls);
                    }
                }
            }
            allGens = new ArrayList<>(plainGens);
            for (Class<?> cls : chainingClasses) {
                Constructor<?> c = cls.getConstructor(SourceGenerator.class);
                for (SourceGenerator gen : plainGens)
                    allGens.add((SourceGenerator)c.newInstance(gen));
            }
            generators = allGens;
        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nonnull List<TestData> generateTestData() {
        List<TripleSet> sets = new ArrayList<>();
        for (Node object : objects) {
            sets.add(new TripleSet(new Triple(S, P, object)));
            sets.add(new TripleSet(new Quad(G, S, P, object)));
            sets.add(new TripleSet(new Quad(Quad.defaultGraphIRI, S, P, object),
                                   new Quad(G,                    S, P, object)));
        }

        List<TestData> dataList = new ArrayList<>();
        List<List<?>> lists = asList(sets, generators, tripleClasses, quadClasses);
        for (List<?> vs : Lists.cartesianProduct(lists)) {
            TripleSet set = (TripleSet) vs.get(0);
            SourceGenerator generator = (SourceGenerator) vs.get(1);
            if (nil.equals(vs.get(2)) && nil.equals(vs.get(3)))
                continue;
            Class<?> tCls = nil.equals(vs.get(2)) ? null : (Class<?>) vs.get(2);
            Class<?> qCls = nil.equals(vs.get(3)) ? null : (Class<?>) vs.get(3);
            dataList.add(new TestData(set, generator, tCls, qCls));
        }
        return dataList;
    }
}
