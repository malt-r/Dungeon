package dsl.interpreter;

import dsl.parser.ast.Node;
import dsl.runtime.callable.ICallable;
import dsl.runtime.callable.NativeFunction;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.TypeFactory;
import java.util.List;

public class TestFunctionReturnHelloWorld extends NativeFunction {
  public static TestFunctionReturnHelloWorld func = new TestFunctionReturnHelloWorld(Scope.NULL);

  /**
   * Constructor
   *
   * @param parentScope parent scope of this function
   */
  private TestFunctionReturnHelloWorld(IScope parentScope) {
    super(
        "testReturnHelloWorld",
        parentScope,
        TypeFactory.INSTANCE.functionType(BuiltInType.stringType));
  }

  @Override
  public Object call(DSLInterpreter interperter, List<Node> parameters) {
    assert parameters != null && parameters.size() == 0;
    return "Hello, World!";
  }

  @Override
  public ICallable.Type getCallableType() {
    return ICallable.Type.Native;
  }
}
