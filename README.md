


rdfit - Agnostic and decoupled iteration over RDF data. 
=====

Suppose you want to iterate over all triples in these sources:
1. An `InputStream` of unknown RDF syntax
2. All graphs of a local TDB database
3. An HDT file
4. All files inside a compressed archive

[Apache Jena](https://jena.apache.org/) and [Eclipse RDF4J](https://rdf4j.org/)
provide functionality for RDF I/O in multiple syntaxes, but each of the above 
cases will require too much code.

rdfit hides the boring bits behind a `Iterator<>` ...
```java
RIt.iterateTriples(Triple.class, inputStream, "/path/to/tdb", 
                   new URI("https://example.org/file.hdt"), new File("Affymetrix.7z")
).forEachRemaining(this::handleTriple);
```

... or an push-style `RDFListener`:
```java
RIt.parseTriples(this::handleTriple, inputStream, anURIToAnHDTFile);
```

rdfit provides:
- Automatic syntax detection (no need to keep track of `InputStream` syntax)
- Detecting RDF data, file paths and URIs in Strings
- Using Java objects as sources (e.g.,
  RDF4J [Model](https://rdf4j.org/javadoc/latest/org/eclipse/rdf4j/model/Model.html),
  Jena [Dataset](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/query/Dataset.html),
  hdt-java's [HDT](https://github.com/rdfhdt/hdt-java/blob/master/hdt-api/src/main/java/org/rdfhdt/hdt/hdt/HDT.java)) and
  `Iterable<T>` or `T[]` where `T` is a triple/quad representation.
- Implicit conversion between triple/quad representations
- Implicit conversion of control flow (pull-style iterators from push-style parsers and vice-versa)

Quick Quickstart
================

1. Get the example project
```shell
git clone --depth=1 https://github.com/lapesd/rdfit.git && mv rdfit/example . && rm -fr rdfit
```
2. Edit [App.java](example/src/main/java/org/example/rdfit/App.java)
3. Build über-jar & run:
```shell
mvn package && java -jar target/example-1.0-SNAPSHOT.jar data/*
```

Quickstart
==========

On Maven: add this to your pom.xml:
```xml
<dependency>
  <groupId>com.github.lapesd.rdfit</groupId>
  <artifactId>rdfit-all-libs</artifactId> <!-- "all" could be jena or df4j -->
  <version>1.0.4</version>
  <type>pom</type>                    <!-- don't forget, this is not a jar -->
</dependency>
```

On gradle, add this to your build.gradle:
```groovy
implementation 'com.github.lapesd.rdfit:rdfit-jena-libs:1.0.3'
```

> Note: if `rdfit-all-libs` bring too much transitive, see [Modules](#modules)
> below for minimal dependencies.

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

Supported sources objects:

- Java objects
  - Iterable/arrays of:
    - ... Jena [Statement](https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/rdf/model/Statement.html),
      [Triple](https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/graph/Triple.html) or
      [Quad](https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/graph/Triple.html)
    - ... RDF4J [Statement](https://rdf4j.org/javadoc/latest/org/eclipse/rdf4j/model/Statement.html)
    - ... HDT [TripleString](https://github.com/rdfhdt/hdt-java/blob/master/hdt-api/src/main/java/org/rdfhdt/hdt/triples/TripleString.java)
    - ... other sources in this list
  - `Supplier<?>`/`Callable<?>` yielding any source in this list
  - `CharSequence`/`String`s that contain RDF data (not paths or URIs)
  - `byte[]` containing compressed data, a compressed archive or RDF data. 
  - Jena:
    [Model](https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/rdf/model/Model.html),
    [Graph](https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/graph/Graph.html),
    [Dataset](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/query/Dataset.html),
    [DatasetGraph](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/sparql/core/DatasetGraph.html),
    [QueryExecution](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/query/QueryExecution.html),
  - hdt-java [HDT](https://github.com/rdfhdt/hdt-java/blob/master/hdt-api/src/main/java/org/rdfhdt/hdt/hdt/HDT.java) intances
  - RDF4J:
    [Model](https://rdf4j.org/javadoc/latest/org/eclipse/rdf4j/model/Model.html),
    [Repository](),
    [RepositoryConnection](),
    [RepositoryResult](),
    [TupleQuery](),
    [TupleQueryResult](),
    [GraphQuery](),
    [GraphQueryResult](),
- External sources (RDF syntax is guessed):
  - `URI` (converted to a `File` or to an `URL`)
  - `URL` remote content is fetched, using content negotiation if possible 
  - `File`/`Path` (can be wrapped with `RDFFile`)
  - `InputStream` (can be wrapped with `RDFInputStream`)
  - Java resources, wrapped with `RDFResource`
  - `Reader`
  - `byte[]` with data that could've been read from an `InputStream` 
  - `CharSequence`/`String` containing either URIs or file paths
  - All of the above support the following compression formats (`commons-compress`):
    - bzip2
    - gzip
    - pack2000
    - xz
    - snappy
    - z
    - deflate, deflate64
    - lz4
    - zstd
  - All of the above support the following archive formats (`commons-compress`):
    - tar (usually within a compressed stream, see above)
    - ar
    - arj
    - cpio
    - dump
    - jar
    - zip
    - 7z
  - All of the above support the following RDF syntaxes:
    - [N-Triples](https://www.w3.org/TR/n-triples/)
    - [N-Quads](https://www.w3.org/TR/n-quads/)
    - [Turtle](https://www.w3.org/TR/turtle/)
    - [TriG](https://www.w3.org/TR/trig/)
    - [RDF/XML](https://www.w3.org/TR/rdf-syntax-grammar/)
    - [OWL/XML](https://www.w3.org/TR/2012/REC-owl2-xml-serialization-20121211/)
    - [TriX](https://www.hpl.hp.com/techreports/2004/HPL-2004-56.html)
    - [RDFa](https://www.w3.org/TR/rdfa-primer/) (no auto-detection)
    - [JSON-LD](https://www.w3.org/TR/json-ld/)
    - [RDF-JSON](https://www.w3.org/TR/rdf-json/) (no auto-detection)
    - [THRIFT](https://jena.apache.org/documentation/io/rdf-binary.html) (no auto-detection)
    - Sesame (old RDF4J) BynaryRDF (no auto-detection)
    - [HDT](https://www.rdfhdt.org/)
- `rdfit` sources (allow setting RDF syntax and base IRIs):
  - `RDFInputStream`
    - `RDFInputStreamSupplier` wraps Callable/Suppliers
    - `RDFFile` wraps a `File`
  
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
- **rdfit-compress**: Load compressed RDF and compressed archives with 
  multiple RDF files inside
- **rdfit-compress-libs**: Loads also optional dependencies from commons-compress: 
  com.github.luben:zstd-jni, org.brotli:dec and org.tukaani:xz
- **rdfit-commons-rdf**: Parsers for collections and commons-rdf Graph/Dataset 
  instances.
- **rdfit-commons-rdf-jena**: Converters between Jena `Triple`/`Statement`/`Quad`s 
  and commons-rdf `Triple`/`Quad`.
- **rdfit-commons-rdf-rdf4j**: Converters between RDF4J `Statement`
  and commons-rdf `Triple`/`Quad`.

> Too many modules? Consider one of these bundles: 
> - **rdfit-jena-libs**: core, compress-libs, hdt, jena, commons-rdf-jena and jena-parsers
> - **rdfit-rdf4j-libs**: core, compress-libs, hdt, rdf4j, commons-rdf-rdf4j and rdf4j-parsers
> - **rdfit-all-libs**: rdf-jena-libs and rdf-rdf4j-libs

Component availability is automatic. On first use, the core module will 
try to load all classes from other modules that are effectively visible in 
the classpath.
 
Extending rdfit
===============

Writing Source Normalizers
--------------------------

- [ ] Write me!

Writing parsers
---------------

- [ ] Write me!

Writing Converters
------------------

- [ ] Write me!


FAQ - Frequently Asked Questions
================================

### Can it handle invalid RDF 1.1

Yes, some types of invalid NT/Turtle/TriG data as well as RDF/XML and OWL/XML 
files with invalid IRIs can be made valid wrapping the source object 
(be it a File, InputStream etc.) with `RIt.tolerant()`. This will:

1. Percent-encode characters not allowed at their current position in the IRI 
   by RFC 3987.
   - If percent-encoding is not allowed at that position by RFC 3987 
     (e.g., `iport` rule), the character will be erased 
2. Erase invalid character encodings (when the binary representation is 
   so messed up it does not appear as the wrong character but is straight up 
   invalid UTF-8)
3. Replace '_' in language tags with '-' (e.g., `en_US` becomes `en-US`) 
4. For NT/Turtle/TriG, `\`-escape occurrences of `\r` (0x0D) and `\n` (0x0A)
   inside single-quoted lexical forms.
5. For NT/Turtle/TriG, replace `\` with `\\` in any `\x`-escape where x is not
   in `tbnrf"'` (see [ECHAR](https://www.w3.org/TR/turtle/#grammar-production-ECHAR)).
6. For NT/Turtle/TriG, identify [UCHAR](https://www.w3.org/TR/turtle/#grammar-production-UCHAR))
   escape sequences that repsent an UTF-8 encoding instead of an unicode 
   code point. Such sequences are composed of only byte-sized code points,
   which value sequence correspond to a valid UTF-8 sequence and where at 
   least one such byte has a value that is the code point of a control 
   character in unicode. Given such conditions, the sequence of UCHARs is 
   replaced by a single UCHAR for the character encoded in UTF-8.
7. For NT/Turtle/TriG, @PREFIX and @BASE are rewritten to @prefix and @base
8. For NT/Turtle/TriG, literals `true` and `false` with any variation in case 
   (e.g., `True`) are replaced with the standard `true` and `false`.
9. For NT/Turtle/TriG, a lexical form followed by an `<IRI>` without space or
   with a number of `^` characters different from 2 is replaced with `^^<IRI>`
10. For NT/Turtle/TriG, replace invalid unquoted plain literals with plain 
   string literals. For this, the code assumes the invalid unquoted literal 
   has no spaces (i.e., whitespace is a separator and never part of the 
   invalid literal). Examples of this fix in action:
   - `<an-iri> t:chromosome X` becomes `<an-iri> t:chromosome "X"`
   - `<s> <p> 2e-3.4` becomes `<s> <p> "2e-3.4"` (expoent must be an integer)
   - `<s> <p> falseful` becomes `<s> <p> "falseful"`
11. Strip leading whitespace, `%20`, `%09`, `%0A` `%0D` and strip `_`s at 
    any position from IRI schemes. Use case: LargeRDFBench/Affymetrix and Jamendo

The percent-encoding is context sensitive and assumes the input IRIs are 
(mostly) correct IRIs. Some delimiters (e.g., /), if wrongly placed will be
interpreted as delimiters instead of being percent-encoded. Examples of IRI 
fixes:

| Bad IRI                                                               | Fixed IRI                             |
|:----------------------------------------------------------------------|:--------------------------------------|
| `relative space`                                                      | `relative%20space`                    |
| `http://example.org/a<b`                                              | `http://example.org/a%3Cb`            |
| `http://example.org:80x/`                                             | `http://example.org:80/`              |
| `http://example.org:/cão` (encoded as ISO-8859-1 but parsed as UTF-8) | `http://example.org:80/co`            |
| `http://example.org/p?q=?`                                            | `http://example.org/p?q=%3F`          |
| `http://bob:?secret@:example.org`                                     | `http://bob:secret%3F@%3Aexample.org` |

For RDF/XML and OWL/XML, the namespace prefixes (typically `rdf:` and `owl:`) 
are detected from the XML input itself. If no `xmlns=` or `xmlns:.+=` is found
for these namespaces, then the default will be assumed. When fixing XML, 
only IRIs inside one of these attributes are "fixed" using the same rules 
outlined above:

- `rdf:about`
- `rdf:resource`
- `rdf:datatype`
- `owl:IRI`

Note that setting RDF4J to make parsing errors non-fatal (the default 
behavior with rdfit), will not always cause the triple to be silently dropped. 
For example, bad object IRIs may be parsed as null and cause a NPE to be 
thrown instead. Using `RIt.tolerant()` avoids that. 

Also note that the effect of `RIt.tolerant()` only applies to sources that are 
known or detected to be NT/Turtle/TriG or RDF/XML or OWL/XML. The fixing 
procedure blindly believes the input has no other issues, and using it over 
data with more serious syntax violations may add to the confusion of parsing 
errors down the line.

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
  - hdt-java's `HDTHelpers.generateHDT()`/`getHDTWriter()`
    - Sending the iterator to `HDTHelper.toHDT{,File}()`
    - Using a listener created from `HDTHelper.{file,}Feeder()`
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

### Does it fetch owl:imports?
Yes. If using a RDFListener, wrap it with one of the *ImportingRDFListener 
classes: `JenaImportingRDFListener`, `HDTImportingRDFListener` or 
`RDF4JImportingRDFListener`. If using a RDFIt, wrap it with one of the 
`*ImportingRDFIt` classes: `JenaImportingRDFIt`, `HDTImportingRDFIt` or
`RDF4JImportingRDFIt`.

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

### Release workflow

Integration tests take >2min thus are disabled by default. Unfortunately, due 
to [NEXUS-9138](https://issues.sonatype.org/browse/NEXUS-9138), they cannot be 
part of the release profile. Also, to prevent IDE-related havoc with frequent 
adding/removing the module through profiles, integration-tests is an 
independent maven project, like the examples directory. The recommended 
workflow for a release is:

1. `mvn -Prelease clean install` to install locally
2. `cd integration-tests && mvn verify ; cd ..` to run integration tests 
2. `mvn -Prelease release:prepare` to set version
3. `mvn -Prelease release:perform` to stage & release to maven central
