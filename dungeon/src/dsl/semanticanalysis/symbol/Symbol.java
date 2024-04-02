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

package dsl.semanticanalysis.symbol;

import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import org.neo4j.ogm.annotation.*;

/** Represents a symbol in a program */
@NodeEntity
public class Symbol {
  public enum Type {
    Base,
    Scoped, // for handling of datatypes for object definitions
    Callable
  }

  // running index, used as unique identifier
  protected static int s_idx;

  @Property protected String name;

  @Relationship(type = "OF_TYPE", direction = Relationship.Direction.OUTGOING)
  protected IType dataType;

  // the parent scope of the symbol
  @Relationship(type = "IN_SCOPE", direction = Relationship.Direction.OUTGOING)
  protected IScope scope;

  @Property protected Type symbolType;

  @Id private int idx;

  public static Symbol NULL = new Symbol("NULL SYMBOL", null, null);

  /**
   * Getter for the name of the symbol
   *
   * @return the name of the symbol
   */
  public String getName() {
    return name;
  }

  /**
   * Getter for the full name of the symbol, which is the name of the symbol prepended by the names
   * of its parent symbols, if it has any
   *
   * @return the full name of the symbol
   */
  public String getFullName() {
    String prefix = "";
    if (this.scope != null && this.scope instanceof Symbol) {
      prefix = ((Symbol) this.scope).getFullName();
      prefix += ".";
    }
    return prefix + this.getName();
  }

  /**
   * Getter for the datatype of the symbol
   *
   * @return the datatype of the symbol (as IType)
   */
  public IType getDataType() {
    return dataType;
  }

  /**
   * Getter for the scope in which this symbol was created
   *
   * @return the scope in which this symbol was created
   */
  public IScope getScope() {
    return scope;
  }

  /**
   * Getter for the index of the symbol (the index is a unique identifier of the symbol)
   *
   * @return the index of the symbol
   */
  public int getIdx() {
    return idx;
  }

  /**
   * Getter for the {@link Type} of the symbol
   *
   * @return the {@link Type} of the symbol
   */
  public Type getSymbolType() {
    return symbolType;
  }

  /**
   * Constructor
   *
   * @param symbolName the name of the new symbol
   * @param parentScope the parent scope of the new symbol (in which it was created)
   * @param dataType the datatype of the symbol
   */
  public Symbol(String symbolName, IScope parentScope, IType dataType) {
    this.idx = s_idx++;
    this.scope = parentScope;
    this.name = symbolName;
    this.dataType = dataType;
    this.symbolType = Type.Base;
  }

  /** Constructor */
  public Symbol() {
    this.scope = null;
    this.name = "no name";
    this.dataType = null;
    this.symbolType = Type.Base;
  }

  @Override
  public String toString() {
    return getName();
  }
}
