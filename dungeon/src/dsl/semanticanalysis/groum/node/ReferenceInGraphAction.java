package dsl.semanticanalysis.groum.node;

import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

public class ReferenceInGraphAction extends ActionNode {
  private static final int referencedVariableTypeIdx = 0;
  private static final int referencedSymbolIdx = 1;

  public ReferenceInGraphAction(Symbol referencedSymbol, long referenceId) {
    super(ActionType.referencedInGraph);
    this.addSymbolReference((Symbol) referencedSymbol.getDataType());
    this.addSymbolReference(referencedSymbol);
    this.referencedInstanceId(referenceId);
    this.updateLabels();
  }

  public IType variableType() {
    return (IType) this.symbolReferences().get(referencedVariableTypeIdx);
  }

  public Symbol variableSymbol() {
    return this.symbolReferences().get(referencedSymbolIdx);
  }

  @Override
  public String getLabel() {
    return this.variableType().getName()
        + ":<ref in graph ["
        + this.referencedInstanceId()
        + "]>"
        + "(name: '"
        + this.variableSymbol().getName()
        + "')";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
