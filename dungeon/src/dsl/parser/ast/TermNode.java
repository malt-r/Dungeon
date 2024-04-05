package dsl.parser.ast;

public class TermNode extends BinaryNode {

  public enum TermType {
    none,
    plus,
    minus
  }

  private final TermType termType;

  public TermType getTermType() {
    return termType;
  }

  public TermNode(TermType type, Node lhs, Node rhs) {
    super(Type.Term, lhs, rhs);
    this.termType = type;
  }

  public TermNode() {
    super(Type.Term);
    this.termType = TermType.none;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
