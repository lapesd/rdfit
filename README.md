

rdfit - Agnostic and decoupled iteration over RDF data. 
=====

Suppose you want to iterate over all triples in these sources:
1. An `InputStream` of unknown RDF syntax
2. All graphs of a local TDB database
3. An HDT file
4. All files inside a compressed archive

[Apache Jena](https://jena.apache.org/) and [Eclipse RDF4J](https://rdf4j.org/)
provide functionality for RDF I/O in multiple syntaxes, but each of the above 
cases will require from tens to a few hundred lines of boilerplate code around 
functionality in those libraries.

rdfit hides the boring bits behind a `Iterator<>` ...
```java
RIt.iterateTriples(Triple.class, inputStream, "/path/to/tdb", 
                   new URI("https://example.org/file.hdt"), new File("Affymetrix.7z")
).forEachRemaining(this::handleTriple);
```

... or an push-style `RDFListener`:
```java
RIt.parse(new TripleListener(Triple.class) {
              @Override public void triple(@Nonnull Triple triple) {
                  handleTriple(triple);
              }
          }, 
          inputStream, "/path/to/tdb", new URI("https://example.org/file.hdt"), new File("Affymetrix.7z")
);
```

rdfit provides:
- Automatic syntax detection (no need to keep track of InputStream syntax)
- Detecting RDF data, file paths and URIs in Strings
- Using Java objects as sources (e.g.,
  RDF4J [Model](https://rdf4j.org/javadoc/latest/org/eclipse/rdf4j/model/Model.html),
  [Repository](),
  [RepositoryConnection](),
  [RepositoryResult](),
  [TupleQuery](),
  [TupleQueryResult](),
  [GraphQuery](),
  [GraphQueryResult](),
  Jena [Model](https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/rdf/model/Model.html), 
  [Graph](https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/graph/Graph.html), 
  [Dataset](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/query/Dataset.html), 
  [DatasetGraph](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/sparql/core/DatasetGraph.html), 
  [QueryExecution](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/query/QueryExecution.html), 
  hdt-java's [HDT](https://github.com/rdfhdt/hdt-java/blob/master/hdt-api/src/main/java/org/rdfhdt/hdt/hdt/HDT.java)) and
  `Iterable<T>` or `T[]` where `T` is a triple/quad representation.
- Implicit conversion between triple/quad representations (e.g.,
  [Statement](https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/rdf/model/Statement.html), 
  [Triple](https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/graph/Triple.html), 
  [Quad](https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/graph/Triple.html), 
  [Statement](https://rdf4j.org/javadoc/latest/org/eclipse/rdf4j/model/Statement.html) or 
  [TripleString](https://github.com/rdfhdt/hdt-java/blob/master/hdt-api/src/main/java/org/rdfhdt/hdt/triples/TripleString.java)).
- Implicit conversion of control flow (pull-style iterators from push-style parsers and vice-versa)

Quickstart
==========

On Maven: add this to your pom.xml:
```xml
<dependency>
  <groupId>com.github.lapesd.rdfit</groupId>
  <artifactId>rdfit-jena-libs</artifactId> <!-- or rdfit-rdf4j-libs -->
  <version>1.0.0</version>
  <type>pom</type>                         <!-- not a jar, just deps -->
</dependency>
```

On gradle, add this to your build.gradle:
```groovy
implementation 'com.github.lapesd.rdfit:rdfit-jena-libs:1.0.0'
```

> Note: `rdfit-jena-libs` and `rdfit-rdf4j-libs` may bring unwanted transitive 
> dependencies. See [Modules](#modules) below for minimal dependencies.

Iterator (RDFIt<T>)
-------------------

Iterate over all triples in a file:
```java
try (RDFIt<Triple> it = RIt.iterateTriples(Triple.class, "/tmp/data.trig")) {
    while (it.hasNext()) {
        Triple triple = it.next();
        process(triple);
    }
}
```

What happened:
- `RIt` is a shortcut for the default `RDFItFactory` instance. It holds a set of 
  `Parsers` and `Converters` which are wired together to handle each 
  source and provide triple quad representations in the type desired 
  by the user.
- `Triple.class` is Jena's representation of a triple, that is the type of 
  returned triples even if a RDF4J or other type of parser is used.
- `/tmp/data.trig` is the input file. Since it is a 
  [TriG](https://www.w3.org/TR/trig/) file, it may contain both triples and 
  quads (triples inside named graphs). Quads will be transparently delivered 
  as triples.  
- `try () { /*...+/ }` The `RDFIt<Triple>` indirectly holds all system 
  resources (e.g., file handles) used during parsing. Thus, it should be closed
  when no longer needed. As a safety measure, resources are released as soon 
  as parsing finishes. Thus, if the iterator is consumed until exhaustion 
  resources will be released before `it.close()` is called.
- `it.hasNext()`/`it.next()` other than its creation and the recommended 
  try-with block, this behaves like any `java.lang.Iterator<>`

The try/while/hasNext/next bit is classic Java boilerplate. To save keystrokes:

```java
RIt.forEachTriple(this::process, "/tmp/data.trig")
```

If instead of triples the goal is to iterate over quads, use `iterateQuads`. 
If the input provides triples, they will be converted to quad representations. 
```java
RDFIt<Quad> it = RIt.iterateQuads(Quad.class, "/tmp/data.nt");
```

> A triple converted to a Jena `Quad` will have `Quad.defaultGraphIRI` as 
> its graph. A triple converted to a RDF4J `Statement` will have a null 
> `getContext()`. For finer control of this conversion, use the overload 
> that takes a `QuadLifter`  

Listeners (push-style iteration)
--------------------------------

Using the listener pattern (a.k.a. a callback), methods in an `RDFListnener` 
implementation are called for every triple/quad or error. Use this if quads 
need dedicated processing in your application or to gracefully handle invalid 
input data (`RDFIt` stops on first error). 

```java
RIt.parse(new TripleListenerBase<>(Triple.class) {
    @Override public void triple(@Nonnull Triple triple) {
        // handle triple
    }

    @Override public void quad(@Nonnull String graph, @Nonnull Triple triple) {
        //handle triple at graph
        //if not overriden, triple(triple) would be called instead
    }
}, "/tmp/data.trig");
```

> The `RDFListener` *Base classes take the triple (and/or quad) class objects 
> in the constructs, this enables conversion at runtime. Any of those can be 
> null (causing conversion of quads to triples and triples to quads), but at 
> least one must be non-null.

To handle both triples and quad objects, extend `RDFListenerBase` instead:
```java
RIt.parse(new RDFListenerBase<>(Triple.class, Quad.class) {
  @Override public void triple(@Nonnull Triple triple) {
    // handle triple
  }

  @Override public void quad(@Nonnull Quad quad) {
    // handle quad. The triple part will not be delivered to 
    // #triple(Triple) nor to #quad(String, Triple)
  }
}, "/tmp/data.trig");
```

A third base class, `QuadListenerBase` allows implementing only the 
`quad(Q quad)` method. Any triple will be converted to a quad representation. 

Supported sources
-----------------

The last parameter in the above factory method examples is variadic, 
thus multiple sources of multiple types can be given. Sources are parsed 
in the same sequence they were given and triples/quads are delivered through 
`RDFIt`/`RDFListener` in the same order they were parsed in each source. 

The following example, while unusual, showcases possible sources. Parsing 
support for a source is independent of whether `iterate*()` or `parse()` 
was called.    

```java
try (RDFIt<Triple> it = RIt.iterateTriples(Triple.class,
        "http://www.w3.org/2006/time", //strings can be URLs (content negotiation)
        "/tmp/quads.nq", //file paths
        "guess-my-rdf-syntax", //file paths can be relative and syntaxes can eb guessed
        "file:/tmp/data.rdf", // file:/ and file:// work as expected
        "_:bnode a <http://www.w3.org/2002/07/owl#Thing>", // RDF string, any syntax
        new File("a-file-object.rdf"), //
        // URLs can be given in java.net classes or RDF terms (jena/rdf4j):
        URI.create("http://www.w3.org/2004/02/skos/core"), //java.net.URI
        URI.create("http://www.w3.org/2004/02/skos/core").toURL(),
        aJenaResource, // will take Resouce.getURI()
        rdf4jIRI,      // IRI.toString() gives the URI
        getClass().getResourceAsStream("any-input-stream-guessed-syntax"),
        // A syntax can be matched to an input stream using this wrapper:
        new RDFInputStream(new FileInputStream("/tmp/file"), RDFSyntax.TTL),
        new RDFFile(new File("/tmp/other_file"), RDFSyntax.TTL),
        // Giving the syntax is optional. It will detect the syntax from data
        new RDFInputStreamSupplier(inputStreamSupplier),
        // RDFInputStream subclasses can be created with the Rit.wrap shothand
        RIt.wrap(inputStreamSupplier)
        // in-memory RDF can also be parsed:
        jenaModel,
        jenaGraph,
        jenaDataset,
        rdf4jModel,
        hdtDict, //HDT dict
        // Collections of triple and quad objects:
        Arrays.asList(jenaTriple, jenaStatement, hdtTripleString),
        // Collections can also contain other sources:
        Arrays.asList(jenaGraph, rdf4jModel, new File("somewhere.ttl")),
        // SPARQL query: type of query (SELECT/CONSTRUCT/DESCRIBE) is auto-detected
        // for SELECT queries, this will work only if the results have exactly
        // three (or four) variables, the order of which determines subject, 
        // predicate,  object (and graph).
        jenaQueryExecution,
        // an RDF4J SELECT query, same caveats (3 or 4 variables) apply:
        rdf4jTupleQueryResult,
        // an RDF4J CONSTRUCT/DESCRIBE query result
        rdf4jGraphQueryResult,
        rdf4jRepositoryResult,
        // RDF4J Query instances will be evaluate()ed and their *Result 
        // instances parsed according to the above rules
        rdf4jTupleQuery,
        rdf4jGraphQuery,
        // A RDF4J model, Repository or a RepositoryConnection can also be parsed
        rdf4jRepository,
        rdf4jRepositoryConnection,
        rdf4jModel,
        // rdfit can handle querying itself:
        new SPARQLQuery("SELECT * WHERE {?s ?p ?o}", "http://example.org/sparql/query")
     )) {
    while (it.hasNext()) {
        Triple triple = it.next();
        process(triple);
    }
}
```

Modules
=======

- **rdfit-core**: mostly interfaces implemented by other modules. 
  There are some implementations residing in this module:
  - `DefaultRDFItFactory`, usually accessed through `RIt`, which 
    wires parsers and converters to match the iteration style and types
    the user expects. 
  - There are iterator and callback parsers for `Iterable<T>`, `Stream<T>` 
    and `T[]`. Each modules, will register instantiations of these parsers 
    (e.g, `Triple`, `Quad` and `Statement`). For stand-alone usage of 
    rdfit-core or for other classes, register instances of these 
    parsers manually with `JavaParsersHelper.registerAll()`.
- **rdfit-jena**: [Apache Jena](https://jena.apache.org/) adapters for 
  `StreamRDF`, create `Model`/`Graph` from `RDFIt`/`RDFCallback`, and 
  `Converter` implementations between `Triple`, `Quad` and `Statement`.
- **rdfit-jena-parsers**: All `Parser` implementations using Jena.
- **rdfit-hdt**: `Parser` implementations that iterate
  [HDT](http://www.rdfhdt.org/) files using 
  [hdt-java](https://github.com/rdfhdt/hdt-java) and converters to/from Jena 
  (`hdt-java` already depends on Jena)
- **rdfit-rdf4j**: Helpers [RDF4J](https://rdf4j.org/), including a 
  `RDFHandler`/`RDFCallback` adapter.
- **rdfit-rdf4j-parsers**: `Parser` implementations using RDF4J. 
- **rdfit-jena-rdf4j-converters**: `Converter` implementations between RDF4J 
  `Statement` and Jena's `Triple`, `Quad` and `Statement
  
> Too many modules? Consider **rdfit-jena-libs** or **rdfit-rdf4j-libs** 
> (shown in the [quickstart](#quickstart)).

Writing parsers
===============

TODO

Writing Converters
==================

TODO

FAQ - Frequently Asked Questions
================================

### Does it support federation?
Although you can use a SPARQL CONSTRUCT/SELECT and TPF queries as sources,
joins between the results are not done by rdfit. Since each source yields 
a stream of triples, there is no straightforward way to join them and 
hammering the API to allow that would cause more suffering than writing 
`SERVICE` clauses in SPARQL queries or using a federated query mediator such 
as FedX (integrated in RDF4J). For more exotic sources there are (mostly 
research) tools, such as [Ontario](https://github.com/SDM-TIB/Ontario), 
[FREQEL](https://github.com/alexishuf/FREQEL) (for Web APIs) and 
[others](https://github.com/semantalytics/awesome-semantic-web/blob/master/README.md#federated-sparql).  

> SPARQL SELECT queries yield a "table" with solutions, not triples. If 
> the query has **exactly three columns** rdfit uses them to build triples, but 
> that is not  a general standard/convention and can lead to confusing 
> triples being built. 

### How do I iterate over all contents in a SPARQL/TPF endpoint
You absolutely should *NOT DO THIS*. Usually the endpoint will refuse a 
`SELECT * WHERE {?s ?p ?o}` query or will go down while trying to answer it.
Use specific queries with at most a few thousand results or search for dump 
files. If storing dump files is not an option, URLs are also sources (parsers 
will work as the dump downloads, except for XML syntaxes which may require 
more memory than there is available).

### How do I query the whole parsed data?
Write it to a queryable format. For large graphs, consider creating a 
disk-backed HDT file. For small datasets an in-memory HDT dictionary, an RDF4J
`Model` or a Jena `Model`/`Graph` should suffice. 
See the next question: ["How do I write RDF data?"](#how-do-i-write-rdf-data).

### How do I write RDF data?
Writing usually occurs to a single destination in a single syntax. Thus using 
the writer in your preferred RDF library will be simpler than using
an indirection layer. There are several ways to do this:
- Direct feeding the `RDFIt<>` to:
  - Jena's `RDFDataMgr.writeTriples()`/`writeQuads()`
  - hdt-java's `HDTManager.generateHDT()`/`getHDTWriter()`
- Wrapping a callback
  - `RDFHandlerListener(rdfWriter)` feeds the given RDF4J `RDFWriter`
  - `StreamRDFListener(streamRDF)` feeds the given Jena `StreamRDF`
- Consolidating data in-memory before writing:
  - Jena:
    - `JenaHelpers.toModel(it)`/`toGraph(it)` builds a `Model`/`Graph` with all 
      triples in the iterator. This is the only way to write richer formats 
      such as TURTLE_PRETTY, JSON-LD and XML
    - `...jena.GraphFeeder` / `...jena.ModelFeeder` builds a `Model`/`Graph`
      with all `Statement`/`Triple`s delivered to the listener
    - `...jena.DatasetGraphFeeder`/`...jena.DatasetFeeder`: build 
      a `DatasetGraph`/`Dataset` instance
  - RDF4J:
    - `RDF4JHelpers.toModel(it)` builds a `Model` instace from an iterator 
      of triples or quads
    - `ModelFeeder(dest)`, `RepositoryConnection` and 
      `RepositoryConnectionFeeder` add all triples and quads to a `Model`, 
      `Repository` or `RepositoryConnection`.

### Does it support RDF*?
RDF4J and Jena do. Import the parser module for the one you prefer. 
On your code, conversions between representations of both libraries preserve 
the RDF* nature of triples (and quads). 

### I have RDF* data on input, but my client code cannot handle it
If the chosen representation (RDF4J or Jena) supports RDF*, you should do the 
filtering before forwarding the RDF* triples. If your chosen representation 
does not support RDF*, exceptions will be thrown (or notified) when a RDF* 
triple is met. You can disable exception throwing on RDFIt by calling 
`setErrorHandler()` and you can override the `notifyInconvertible*()` methods 
in RDFCallback to stop logging.  

