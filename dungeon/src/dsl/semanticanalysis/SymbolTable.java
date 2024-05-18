/*
 * MIT License
 *
 * Copyright (c) 2022 Malte Reinsch, Florian Warzecha, Sebastian Steinmeyer, BC George, Carsten Gips
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dsl.semanticanalysis;

import dsl.parser.ast.Node;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.symbol.SymbolCreation;
import dsl.semanticanalysis.symbol.SymbolReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** The results of semantic analysis done by SymbolTableParser */
public class SymbolTable {
  /** The global scope of the program */
  IScope globalScope;

  /** Store all symbols in a key-value store for easy referencing by their index */
  private final HashMap<Integer, Symbol> symbolIdxToSymbol;

  /** Store all referenced astNodes in a key-value store for easy referencing by their index */
  private final HashMap<Integer, Node> astNodeIdxToAstNode;

  /**
   * Creates an association between a specific AST node and a symbol -> e.g. "which symbol is
   * referenced by the identifier in a specific AST node?"
   */
  private final HashMap<Node, Symbol> astNodeSymbolRelation;

  /**
   * Creates an association between a specific symbol and an ast node -> e.g. "by which ast node was
   * this symbol created?"
   */
  private final HashMap<Symbol, Node> creationASTNodeRelation;

  /**
   * Getter for the global {@link IScope}, which is the topmost scope in the scope stack
   *
   * @return the global {@link IScope}
   */
  public IScope globalScope() {
    return globalScope;
  }

  private final List<SymbolCreation> symbolCreations;

  public List<SymbolCreation> getSymbolCreations() {
    return symbolCreations;
  }

  private final List<SymbolReference> symbolReferences;

  public List<SymbolReference> getSymbolReferences() {
    return symbolReferences;
  }

  private List<IScope> scopes;

  /**
   * Add an association between symbol and AST node. The nodeOfSymbol passed to this method with a
   * symbol, which was not previously passed, will be treated as the node, which created the symbol
   *
   * @param symbol The symbol
   * @param nodeOfSymbol The AST Node, which references the symbol
   */
  public void addSymbolNodeRelation(Symbol symbol, Node nodeOfSymbol, boolean isNodeCreationNode) {
    // TODO: are there situations, in which multiple symbols are associated with the same
    //  AST-Node? if not, this could be simplified
    /*if (!astNodeSymbolRelation.containsKey(nodeOfSymbol)) {
      astNodeSymbolRelation.put(nodeOfSymbol, new ArrayList<>());
    }*/

    // TODO: model this as :REFERENCES in neo4j
    // astNodeSymbolRelation.get(nodeOfSymbol).add(symbol);
    astNodeSymbolRelation.put(nodeOfSymbol, symbol);
    this.symbolReferences.add(new SymbolReference(nodeOfSymbol, symbol));

    if (isNodeCreationNode) {
      // TODO: model this as :CREATES in neo4j
      setCreationAstNode(symbol, nodeOfSymbol);
    }

    // this is just for housekeeping and keeping track of the objects
    // symbolIdxToSymbol.put(symbol.getIdx(), symbol);
    // astNodeIdxToAstNode.put(nodeOfSymbol.getIdx(), nodeOfSymbol);
  }

  /**
   * Try to get the Symbol referenced by a specific AST node
   *
   * @param node The AST node
   * @return The Symbol referenced by node, or Symbol.NULL, if no Symbol could be found
   */
  public ArrayList<Symbol> getSymbolsForAstNode(Node node) {
    if (!astNodeSymbolRelation.containsKey(node)) {
      // TODO: just empty list?
      var list = new ArrayList<Symbol>();
      list.add(Symbol.NULL);
      return list;
    }

    // var symbolIdxs = astNodeSymbolRelation.get(node.getIdx());
    var symbols = astNodeSymbolRelation.get(node);
    return new ArrayList<>(List.of(symbols));
  }

  private void setCreationAstNode(Symbol symbol, Node creationNode) {
    creationASTNodeRelation.put(symbol, creationNode);
    this.symbolCreations.add(new SymbolCreation(creationNode, symbol));
  }

  /**
   * Gets the AST Node, which was passed to {@link #addSymbolNodeRelation(Symbol, Node, boolean)}
   * the first time the symbol was passed to that method, which will be treated as the AST Node,
   * which creates the Symbol
   *
   * @param symbol The symbol to get the creation AST node for
   * @return The creation AST node or Node.NONE, if none could be found for the passed symbol
   */
  public Node getCreationAstNode(Symbol symbol) {
    if (!creationASTNodeRelation.containsKey(symbol)) {
      return Node.NONE;
    }

    return creationASTNodeRelation.get(symbol);
  }

  /**
   * Get the {@link Symbol} with passed index
   *
   * @param idx the index of the {@link Symbol} to get
   * @return the {@link Symbol} with passed index or Symbol.NULL, if no symbol with given index
   *     exists
   */
  public Symbol getSymbolByIdx(int idx) {
    return symbolIdxToSymbol.getOrDefault(idx, Symbol.NULL);
  }

  public List<IScope> getScopes() {
    return this.scopes;
  }

  public void addScope(IScope scope) {
    this.scopes.add(scope);
  }

  /**
   * Constructor
   *
   * @param globalScope the global scope to use for this symbol Table
   */
  public SymbolTable(IScope globalScope) {
    this.globalScope = globalScope;
    this.symbolCreations = new ArrayList<>();
    this.symbolReferences = new ArrayList<>();
    this.scopes = new ArrayList<>();
    astNodeSymbolRelation = new HashMap<>();
    symbolIdxToSymbol = new HashMap<>();
    astNodeIdxToAstNode = new HashMap<>();
    creationASTNodeRelation = new HashMap<>();
  }
}
