package dsl.helpers;

import core.Entity;
import dsl.antlr.DungeonDSLLexer;
import dsl.antlr.DungeonDSLParser;
import dsl.antlr.ParseTracerForTokenType;
import dsl.antlr.TreeUtils;
import dsl.error.ErrorListener;
import dsl.error.ErrorStrategy;
import dsl.interpreter.DSLInterpreter;
import dsl.parser.DungeonASTConverter;
import dsl.parser.ParseTreeValidator;
import dsl.parser.ast.Node;
import dsl.runtime.memoryspace.MemorySpace;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.analyzer.SemanticAnalyzer;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.symbol.ScopedSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import entrypoint.DungeonConfig;
import entrypoint.ParsedFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import task.tasktype.quizquestion.SingleChoice;

public class Helpers {

  private static DungeonDSLParser.ProgramContext getParseTreeFromCharStream(
      CharStream stream, IEnvironment env) {
    var lexer = new DungeonDSLLexer(stream, env);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new DungeonDSLParser(tokenStream, env);

    return parser.program();
  }

  /**
   * Uses ANTLR-classes to create a parse tree from passed program string
   *
   * @param program the program to parse
   * @return the {@link DungeonDSLParser.ProgramContext} for the parsed program
   */
  public static DungeonDSLParser.ProgramContext getParseTree(String program, IEnvironment env) {
    var stream = CharStreams.fromString(program);
    return getParseTreeFromCharStream(stream, env);
  }

  /**
   * Converts a {@link DungeonDSLParser.ProgramContext} to a {@link Node}
   *
   * @param parseTree the parser tree to convert
   * @return the AST for the parse tree
   */
  public static Node convertToAST(
      DungeonDSLParser.ProgramContext parseTree, List<String> parserRuleNames) {
    DungeonASTConverter converter = new DungeonASTConverter(parserRuleNames);
    return converter.walk(parseTree);
  }

  /**
   * Generates the AST for a passed program string
   *
   * @param program the program to generate an AST for
   * @return the AST
   */
  public static Node getASTFromString(String program, IEnvironment env) {
    return DungeonASTConverter.getProgramAST(program, env);
  }

  public static List<String> getParserRuleNames() {
    CharStream stream = CharStreams.fromString("");
    DungeonDSLLexer lexer = new DungeonDSLLexer(stream);
    CommonTokenStream cts = new CommonTokenStream(lexer);
    DungeonDSLParser parser = new DungeonDSLParser(cts);
    return Arrays.stream(parser.getRuleNames()).toList();
  }

  public static Node getASTFromString(String program) {
    return DungeonASTConverter.getProgramAST(program, null);
  }

  /**
   * Load a resource file and generate an AST for it's contents
   *
   * @param fileResourceURL the URL of the resourceFile
   * @return the AST for the contents of the file
   * @throws URISyntaxException on invalid URI syntax
   * @throws IOException if the file does not exist
   */
  public static Node getASTFromResourceFile(URL fileResourceURL, IEnvironment env)
      throws URISyntaxException, IOException {
    var file = new File(fileResourceURL.toURI());
    var stream = CharStreams.fromFileName(file.getAbsolutePath());

    var parseTree = getParseTreeFromCharStream(stream, env);
    return convertToAST(parseTree, getParserRuleNames());
  }

  /**
   * Performs semantic analysis for given AST and returns the {@link SemanticAnalyzer.Result} output
   * from the SymbolTableParser
   *
   * @param ast the AST to create the symbol table for
   * @return the {@link SemanticAnalyzer.Result} of the semantic analysis
   */
  public static SemanticAnalyzer.Result getSymtableForAST(Node ast) {
    SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
    symbolTableParser.setup(new GameEnvironment());
    return symbolTableParser.walk(ast);
  }

  /**
   * Performs semantic analysis with custom environment for given AST and returns the {@link
   * SemanticAnalyzer.Result} output from the SymbolTableParser
   *
   * @param ast the AST to create the symbol table for
   * @param environment the environment to use during semantic analysis
   * @return the {@link SemanticAnalyzer.Result} of the semantic analysis
   */
  public static SemanticAnalyzer.Result getSymtableForASTWithCustomEnvironment(
      Node ast, IEnvironment environment) {
    SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
    symbolTableParser.setup(environment);
    return symbolTableParser.walk(ast);
  }

  public static void bindDefaultValueInMemorySpace(
      Symbol symbol, MemorySpace ms, DSLInterpreter interpreter) {
    var defaultValue = interpreter.createDefaultValue(symbol.getDataType());
    ms.bindValue(symbol.getName(), defaultValue);
  }

  /**
   * @param program String representation of DSL program to generate the quest config for
   * @param environment GameEnvironment to use for loading types and semantic analysis
   * @param interpreter DSLInterpreter to use to generate the quest config
   * @param classesToLoadAsTypes List of all classes marked with @DSLType to load as types into the
   *     environment
   * @return the generated quest config
   */
  public static Object generateQuestConfigWithCustomTypes(
      String program,
      GameEnvironment environment,
      DSLInterpreter interpreter,
      Class<?>... classesToLoadAsTypes) {

    for (var clazz : classesToLoadAsTypes) {
      var type =
          environment
              .typeBuilder()
              .createDSLTypeForJavaTypeInScope(environment.getGlobalScope(), clazz);
      environment.loadTypes(type);
    }

    SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
    symbolTableParser.setup(environment);
    var ast = Helpers.getASTFromString(program, environment);
    symbolTableParser.walk(ast);
    ParsedFile latestParsedFile = symbolTableParser.latestParsedFile;

    interpreter.initializeRuntime(environment, latestParsedFile.filePath());
    Value questConfigValue = (Value) interpreter.generateQuestConfig(ast, latestParsedFile);
    return questConfigValue.getInternalValue();
  }

  /**
   * @param program String representation of DSL program to generate the quest config for
   * @param environment GameEnvironment to use for loading functions and semantic analysis
   * @param interpreter DSLInterpreter to use to generate the quest config
   * @param functions List of all functions to load into the environment
   * @return the generated quest config
   */
  public static Object generateQuestConfigWithCustomFunctions(
      String program,
      GameEnvironment environment,
      DSLInterpreter interpreter,
      ScopedSymbol... functions) {

    environment.loadFunctions(functions);

    SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
    symbolTableParser.setup(environment);
    var ast = Helpers.getASTFromString(program, environment);
    symbolTableParser.walk(ast);
    ParsedFile latestParsedFile = symbolTableParser.latestParsedFile;

    interpreter.initializeRuntime(environment, latestParsedFile.filePath());

    return interpreter.generateQuestConfig(ast, latestParsedFile);
  }

  public static HashSet<HashSet<Entity>> buildTask(
      String program, ByteArrayOutputStream outputStream) {
    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = (SingleChoice) config.dependencyGraph().nodeIterator().next().task();

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    System.setOut(new PrintStream(outputStream));
    return (HashSet<HashSet<Entity>>) interpreter.buildTask(task).get();
  }

  public static HashSet<HashSet<Entity>> buildTask(
      String program, ByteArrayOutputStream outputStream, DSLInterpreter interpreter) {
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = (SingleChoice) config.dependencyGraph().nodeIterator().next().task();

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    System.setOut(new PrintStream(outputStream));
    return (HashSet<HashSet<Entity>>) interpreter.buildTask(task).get();
  }

  public static String printParseTreeForProgram(String program) {
    DungeonDSLLexer lexer = new DungeonDSLLexer(CharStreams.fromString(program));
    var tokenStream = new CommonTokenStream(lexer);
    DungeonDSLParser parser = new DungeonDSLParser(tokenStream);
    var parseTree = parser.program();

    List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
    return TreeUtils.toPrettyTree(parseTree, ruleNamesList);
  }

  public static String getPrettyPrintedParseTree(
      String program, IEnvironment environment, boolean trace) {
    ErrorListener el = new ErrorListener();
    var stream = CharStreams.fromString(program);
    var lexer = new dsl.antlr.DungeonDSLLexer(stream, environment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);
    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream, environment);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    if (trace) {
      ParseTracerForTokenType ptftt = new ParseTracerForTokenType(parser);
      parser.addParseListener(ptftt);
    }
    parser.setTrace(trace);
    var programParseTree = parser.program();
    return TreeUtils.toPrettyTree(programParseTree, Arrays.stream(parser.getRuleNames()).toList());
  }

  public static String getPrettyPrintedParseTree(String program, IEnvironment environment) {
    return getPrettyPrintedParseTree(program, environment, false);
  }

  public static List<String> validateParseTree(
      String program, IEnvironment environment, String expectedTree, boolean trace) {
    ErrorListener el = new ErrorListener();
    var stream = CharStreams.fromString(program);
    var lexer = new dsl.antlr.DungeonDSLLexer(stream, environment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);
    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream, environment);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    if (trace) {
      ParseTracerForTokenType ptftt = new ParseTracerForTokenType(parser);
      parser.addParseListener(ptftt);
    }
    parser.setTrace(trace);
    var programParseTree = parser.program();
    ParseTreeValidator ptv = new ParseTreeValidator();
    List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
    return ptv.validate(expectedTree, programParseTree, ruleNamesList);
  }

  public static List<String> validateParseTree(
      String program, IEnvironment environment, String expectedTree) {
    return validateParseTree(program, environment, expectedTree, false);
  }
}
