package org.example.rdfit;

import com.github.lapesd.rdfit.RIt;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.graph.GraphFactory;

public class App {
    public static void main( String[] args ) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar rdfit-example-1.0.-SNAPSHOT.jar SRC_1 SRC_2 ...");
            System.out.println("Each SRC argument should be a URL or filepath.");
            System.out.println("Example:");
            System.out.println("    java -jar target/rdfit-example-1.0-SNAPSHOT.jar data/*");
            return;
        }

        // Create a in-memory graph to store parsed triples
        Graph graph = GraphFactory.createDefaultGraph();
        graph.getPrefixMapping().setNsPrefix("ex", "http://example.org/");

        // parse all inputs, stripping away graphs from quads
        RIt.forEachTriple(Triple.class, graph::add, (Object[]) args);

        // spew everything to stdout as Turtle
        RDFDataMgr.write(System.out, graph, RDFFormat.TURTLE_PRETTY);
    }
}
