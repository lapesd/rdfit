package com.github.lapesd.rdfit.util.impl;

import com.github.lapesd.rdfit.source.RDFInputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

import static org.apache.jena.graph.NodeFactory.createURI;
import static org.testng.Assert.*;

public class EternalCacheTest {

    @Test
    public void testGetTimeOntology() throws MalformedURLException {
        EternalCache cache = EternalCache.getDefault();
        String time = "http://www.w3.org/2006/time#";
        URL url = new URL(time); //# will be ignored
        Supplier<RDFInputStream> supplier = cache.get(url);
        assertNotNull(supplier);

        Graph g = GraphFactory.createDefaultGraph();
        try (RDFInputStream is = supplier.get()) {
            RDFDataMgr.read(g, is.getInputStream(), Lang.TTL);
        }
        Node monday = createURI(time + "Monday");
        Node dayOfWeek = createURI(time + "DayOfWeek");
        ExtendedIterator<Triple> it = g.find(monday, null, dayOfWeek);
        assertTrue(it.hasNext());
        assertEquals(it.next(), new Triple(monday, RDF.type.asNode(), dayOfWeek));
        assertFalse(it.hasNext());
    }

}