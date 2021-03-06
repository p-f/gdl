/*
 * Copyright 2017 The GDL Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.s1ck.gdl;

import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.s1ck.gdl.model.Edge;
import org.s1ck.gdl.model.Graph;
import org.s1ck.gdl.model.Vertex;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.utils.ContinuousId;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Helper class that wraps ANTLR initialization logic.
 */
public class GDLHandler {
  /**
   * GDL listener implementation.
   */
  private GDLLoader loader;

  /**
   * Private constructor to avoid external initialization.
   *
   * @param loader GDL loader
   */
  private GDLHandler(GDLLoader loader) {
    this.loader = loader;
  }

  /**
   * Append the given GDL string to the current database.
   *
   * @param asciiString GDL string (must not be {@code null}).
   */
  public void append(String asciiString) {
    if (asciiString == null) {
      throw new IllegalArgumentException("AsciiString must not be null");
    }
    ANTLRInputStream antlrInputStream = new ANTLRInputStream(asciiString);
    GDLLexer lexer = new GDLLexer(antlrInputStream);
    GDLParser parser = new GDLParser(new CommonTokenStream(lexer));
    // update the loader state while walking the parse tree
    new ParseTreeWalker().walk(loader, parser.database());
  }

  /**
   * Returns a collection of all graphs defined in the GDL script.
   *
   * @return graph collection
   */
  public Collection<Graph> getGraphs() {
    return loader.getGraphs();
  }

  /**
   * Returns a collection of all vertices defined in the GDL script.
   *
   * @return vertex collection
   */
  public Collection<Vertex> getVertices() {
    return loader.getVertices();
  }

  /**
   * Returns a collection of all edges defined in the GDL script.
   *
   * @return edge collection
   */
  public Collection<Edge> getEdges() {
    return loader.getEdges();
  }

  /**
   * Returns the predicates defined by the query in CNF.
   *
   * @return predicates
   */
  public Optional<Predicate> getPredicates() { return loader.getPredicates(); }

  /**
   * Returns a cache that contains a mapping from user-defined variables to graph instances.
   *
   * @return immutable graph cache
   */
  public Map<String, Graph> getGraphCache() {
    return loader.getGraphCache();
  }

  /**
   * Returns a cache that contains a mapping from variables to graph instances.
   *
   * @param includeUserDefined true, iff user-defined variables shall be included in the cache
   * @param includeAutoGenerated true, iff auto-generated variables shall be included in the cache
   *
   * @return immutable graph cache
   */
  public Map<String, Graph> getGraphCache(boolean includeUserDefined, boolean includeAutoGenerated) {
    return loader.getGraphCache(includeUserDefined, includeAutoGenerated);
  }

  /**
   * Returns a cache that contains a mapping from user-defined variables to vertex instances.
   *
   * @return immutable vertex cache
   */
  public Map<String, Vertex> getVertexCache() {
    return loader.getVertexCache();
  }

  /**
   * Returns a cache that contains a mapping from variables to vertex instances.
   *
   * @param includeUserDefined true, iff user-defined variables shall be included in the cache
   * @param includeAutoGenerated true, iff auto-generated variables shall be included in the cache
   *
   * @return immutable vertex cache
   */
  public Map<String, Vertex> getVertexCache(boolean includeUserDefined, boolean includeAutoGenerated) {
    return loader.getVertexCache(includeUserDefined, includeAutoGenerated);
  }

  /**
   * Returns a cache that contains a mapping from user-defined variables to edge instances.
   *
   * @return immutable edge cache
   */
  public Map<String, Edge> getEdgeCache() {
    return loader.getEdgeCache();
  }

  /**
   * Returns a cache that contains a mapping from variables to edge instances.
   *
   * @param includeUserDefined true, iff user-defined variables shall be included in the cache
   * @param includeAutoGenerated true, iff auto-generated variables shall be included in the cache
   *
   * @return immutable edge cache
   */
  public Map<String, Edge> getEdgeCache(boolean includeUserDefined, boolean includeAutoGenerated) {
    return loader.getEdgeCache(includeUserDefined, includeAutoGenerated);
  }

  /**
   * Builds a GDL Handler.
   */
  public static class Builder {

    /**
     * Graph label.
     */
    private String graphLabel = "__GRAPH";

    /**
     * Vertex label.
     */
    private String vertexLabel = "__VERTEX";

    /**
     * Edge label.
     */
    private String edgeLabel = "__EDGE";

    /**
     * Flag to indicate if the default graph label shall be used if none is present.
     */
    private boolean useDefaultGraphLabel = true;

    /**
     * Flag to indicate if the default vertex label shall be used if none is present.
     */
    private boolean useDefaultVertexLabel = true;

    /**
     * Flag to indicate if the default edge label shall be used if none is present.
     */
    private boolean useDefaultEdgeLabel = true;

    /**
     * Id supplier for graphs.
     */
    private Supplier<Long> nextGraphId = new ContinuousId();

    /**
     * Id supplier for vertices.
     */
    private Supplier<Long> nextVertexId = new ContinuousId();

    /**
     * Id supplier for edges.
     */
    private Supplier<Long> nextEdgeId = new ContinuousId();

    /**
     * Strategy for handling parser errors.
     */
    private ANTLRErrorStrategy errorStrategy = new DefaultErrorStrategy();

    /**
     * Default graph label is used if none is set in the GDL script.
     *
     * @param graphLabel graph label (must not be {@code null}).
     * @return builder
     */
    public Builder setDefaultGraphLabel(String graphLabel) {
      this.graphLabel = graphLabel;
      return this;
    }

    /**
     * Default vertex label is used if none is set in the GDL script.
     *
     * @param vertexLabel vertex label (must not be {@code null}).
     * @return builder
     */
    public Builder setDefaultVertexLabel(String vertexLabel) {
      this.vertexLabel = vertexLabel;
      return this;
    }

    /**
     * Default edge label is used if none is set in the GDL script.
     *
     * @param edgeLabel edge label (must not be {@code null}).
     * @return builder
     */
    public Builder setDefaultEdgeLabel(String edgeLabel) {
      this.edgeLabel = edgeLabel;
      return this;
    }

    /**
     * Enable default graph label.
     *
     * @return builder
     */
    public Builder enableDefaultGraphLabel() {
      this.useDefaultGraphLabel = true;
      return this;
    }

    /**
     * Disable default graph label.
     *
     * @return builder
     */
    public Builder disableDefaultGraphLabel() {
      this.useDefaultGraphLabel = false;
      return this;
    }

    /**
     * Enable default vertex label.
     *
     * @return builder
     */
    public Builder enableDefaultVertexLabel() {
      this.useDefaultVertexLabel = true;
      return this;
    }

    /**
     * Disable default vertex label.
     *
     * @return builder
     */
    public Builder disableDefaultVertexLabel() {
      this.useDefaultVertexLabel = false;
      return this;
    }

    /**
     * Enable default edge label.
     *
     * @return builder
     */
    public Builder enableDefaultEdgeLabel() {
      this.useDefaultEdgeLabel = true;
      return this;
    }

    /**
     * Disable default edge label.
     *
     * @return builder
     */
    public Builder disableDefaultEdgeLabel() {
      this.useDefaultEdgeLabel = false;
      return this;
    }

    /**
     * Sets the id generation function for graphs.
     *
     * @param nextGraphId graph id function (must not be {@code null})
     * @return builder
     */
    public Builder setNextGraphId(Supplier<Long> nextGraphId) {
      this.nextGraphId = nextGraphId;
      return this;
    }

    /**
     * Sets the id generation function for vertices.
     *
     * @param nextVertexId vertex id function (must not be {@code null})
     * @return builder
     */
    public Builder setNextVertexId(Supplier<Long> nextVertexId) {
      this.nextVertexId = nextVertexId;
      return this;
    }

    /**
     * Sets the id generation function for edges.
     *
     * @param nextEdgeId edge id function (must not be {@code null})
     * @return builder
     */
    public Builder setNextEdgeId(Supplier<Long> nextEdgeId) {
      this.nextEdgeId = nextEdgeId;
      return this;
    }

    /**
     * Set the error handler strategy for ANTLR. If not set, {@link DefaultErrorStrategy} is used.
     *
     * @param errorStrategy ANTLR error strategy
     * @return builder
     */
    public Builder setErrorStrategy(ANTLRErrorStrategy errorStrategy) {
      this.errorStrategy = errorStrategy;
      return this;
    }

    /**
     * Initialize GDL Handler from given ASCII String.
     *
     * @param asciiString GDL string (must not be {@code null}).
     * @return GDL handler
     */
    public GDLHandler buildFromString(String asciiString) {
      ANTLRInputStream antlrInputStream = new ANTLRInputStream(asciiString);
      return build(antlrInputStream);
    }

    /**
     * Initializes GDL Handler from given input stream.
     *
     * @param stream InputStream (must not be {@code null}).
     * @return GDL handler
     * @throws IOException
     */
    public GDLHandler buildFromStream(InputStream stream) throws IOException {
      ANTLRInputStream antlrInputStream = new ANTLRInputStream(stream);
      return build(antlrInputStream);
    }

    /**
     * Initializes GDL Handler from given file.
     *
     * @param fileName GDL file (must not be {@code null}).
     * @return GDL handler
     */
    public GDLHandler buildFromFile(String fileName) throws IOException {
      ANTLRInputStream antlrInputStream = new ANTLRFileStream(fileName);
      return build(antlrInputStream);
    }

    /**
     * Checks valid input and creates GDL Handler.
     *
     * @param antlrInputStream ANTLR input stream
     * @return GDL handler
     */
    private GDLHandler build(ANTLRInputStream antlrInputStream) {
      if (graphLabel == null) {
        throw new IllegalArgumentException("Graph label must not be null.");
      }
      if (vertexLabel == null) {
        throw new IllegalArgumentException("Vertex label must not be null.");
      }
      if (edgeLabel == null) {
        throw new IllegalArgumentException("Edge label must not be null.");
      }
      if (errorStrategy == null) {
        throw new IllegalArgumentException("Error handler must not be null.");
      }
      if (nextGraphId == null) {
        throw new IllegalArgumentException("Graph id function must not be null.");
      }
      if (nextVertexId == null) {
        throw new IllegalArgumentException("Vertex id function must not be null.");
      }
      if (nextEdgeId == null) {
        throw new IllegalArgumentException("Edge id function must not be null.");
      }

      GDLLexer lexer = new GDLLexer(antlrInputStream);
      GDLParser parser = new GDLParser(new CommonTokenStream(lexer));
      parser.setErrorHandler(errorStrategy);

      GDLLoader loader = new GDLLoader(
              graphLabel, vertexLabel, edgeLabel,
              useDefaultGraphLabel, useDefaultVertexLabel, useDefaultEdgeLabel,
              nextGraphId, nextVertexId, nextEdgeId
      );
      new ParseTreeWalker().walk(loader, parser.database());
      return new GDLHandler(loader);
    }
  }
}

