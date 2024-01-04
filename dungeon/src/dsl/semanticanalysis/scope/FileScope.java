package dsl.semanticanalysis.scope;

import dsl.parser.ast.Node;
import entrypoint.ParsedFile;

public class FileScope extends Scope {
    public static FileScope NULL = new FileScope(new ParsedFile(null, Node.NONE), Scope.NULL);
    protected final ParsedFile file;

    public FileScope(ParsedFile file, IScope parentScope) {
        super(parentScope);
        this.file = file;
    }

    public ParsedFile file() {
        return this.file;
    }
}
