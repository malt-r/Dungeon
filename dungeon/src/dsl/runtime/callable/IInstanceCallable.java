package dsl.runtime.callable;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.Node;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionMethod;
import java.util.List;

/**
 * Simple interface for methods, which require an instance for context. This interface is used for
 * built-in native methods only, for external methods, use {@link IDSLExtensionMethod}!
 */
public interface IInstanceCallable {
    Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters);
}
