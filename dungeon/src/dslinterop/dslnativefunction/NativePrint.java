package dslinterop.dslnativefunction;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.Node;
import dsl.runtime.callable.ICallable;
import dsl.runtime.callable.NativeFunction;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.TypeFactory;
import java.util.List;

public class NativePrint extends NativeFunction {
  public static NativePrint func = new NativePrint(Scope.NULL);

  /**
   * Constructor
   *
   * @param parentScope parent scope of this function
   */
  private NativePrint(IScope parentScope) {
    super(
        "print",
        parentScope,
        TypeFactory.INSTANCE.functionType(BuiltInType.noType, BuiltInType.stringType));

    // bind parameters
    Symbol param = new Symbol("param", this, BuiltInType.stringType);
    this.bind(param);
  }

  @Override
  public Object call(DSLInterpreter interperter, List<Node> parameters) {
    assert parameters != null && parameters.size() > 0;
    try {
      Value param = (Value) parameters.get(0).accept(interperter);
      String paramAsString = param.toString();
      System.out.println(paramAsString);
    } catch (ClassCastException ex) {
      // TODO: handle.. although this should not be a problem because
      //  of typechecking, once it is impelemented
    }
    return null;
  }

  @Override
  public ICallable.Type getCallableType() {
    return ICallable.Type.Native;
  }
}
