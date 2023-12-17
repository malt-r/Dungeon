package dsl.semanticanalysis.analyzer;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.*;

import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class ImportAnalyzer implements AstVisitor<Void> {
    @Override
    public Void visit(Node node) {
        switch (node.type) {
            case Program:
                visitChildren(node);
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public Void visit(ImportNode node) {
        // TODO:
        //  - check for well-formed-ness of path
        //  - get file of path
        //  - call semantic analysis for file
        //  - -> need to check-for/resolve/handle recursive imports
        //       (non trivial, need to think about that)
        //  - resolve symbol in file-scope
        //  - create "proxy"-symbol for resolved symbol

        // TODO: should handle missing ".dng" file-extension

        // check for well-formed-ness of path
        Path path = Path.of("test.dng");
        Path libPath = DSLInterpreter.libPath;
        Path filePath = Path.of(libPath.toString(), path.toString()).toAbsolutePath();
        Path realPath;
        try {
            realPath = filePath.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (Exception ex) {
            // TODO: let it throw for now, should rethink this, when partial parsing
            //  will be implemented
            throw new RuntimeException("No file with the given path '" + filePath + "' found!");
        }
        Path relativeFilePath = libPath.relativize(realPath);
        if (relativeFilePath.startsWith("..")) {
            throw new RuntimeException("Importing from files outside of the 'lib' directory is not allowed!");
        }

        return null;
    }

    @Override
    public Void visit(DotDefNode node) {
        return null;
    }

    @Override
    public Void visit(ObjectDefNode node) {
        return null;
    }

    @Override
    public Void visit(FuncDefNode node) {
        return null;
    }

    @Override
    public Void visit(PrototypeDefinitionNode node) {
        return null;
    }

    @Override
    public Void visit(ItemPrototypeDefinitionNode node) {
        return AstVisitor.super.visit(node);
    }
}
