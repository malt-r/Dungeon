package dsl.interpreter;

import dsl.helpers.Helpers;
import dsl.parser.ast.FuncDefNode;
import dsl.parser.ast.Node;
import dsl.semanticanalysis.scope.FileScope;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import dsl.semanticanalysis.typesystem.typebuilding.type.ListType;
import dsl.semanticanalysis.typesystem.typebuilding.type.SetType;
import java.io.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;

public class TestDSLInterpreterExpression {
  private static String testProgramPreamble =
      """
            single_choice_task t1 {
                description: "Task1",
                answers: [ "1", "2", "3", "4"],
                correct_answer_index: 3
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }
        """;

  @Test
  public void addInteger() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var int1 : int;
                    int1 = 21;
                    var int2 : int;
                    int2 = int1 + 21;

                    print(int2);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("42" + System.lineSeparator(), output);
  }

  @Test
  public void addFloat() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var f1 : float;
                    f1 = 3.14;
                    var f2 : float;
                    f2 = f1 + 3.14;

                    print(f2);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("6.28" + System.lineSeparator(), output);
  }

  @Test
  public void addString() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var s1 : string;
                    s1 = "Hello";
                    var s2 : string;
                    s2 = s1 + ", World" + "!";

                    print(s2);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("Hello, World!" + System.lineSeparator(), output);
  }

  @Test
  public void subInteger() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var int1 : int;
                    int1 = 21;
                    var int2 : int;
                    int2 = int1 - 63;

                    print(int2);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("-42" + System.lineSeparator(), output);
  }

  @Test
  public void subFloat() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var f1 : float;
                    f1 = 3.14;
                    var f2 : float;
                    f2 = f1 - 6.28;

                    print(f2);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("-3.14" + System.lineSeparator(), output);
  }

  @Test
  public void mulInteger() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var int1 : int;
                    int1 = 21;
                    var int2 : int;
                    int2 = int1 * 2;

                    print(int2);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("42" + System.lineSeparator(), output);
  }

  @Test
  public void mulFloat() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var f1 : float;
                    f1 = 3.14;
                    var f2 : float;
                    f2 = f1 * 2.5;

                    print(f2);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("7.85"));
  }

  @Test
  public void unaryNot() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var b1 : bool;
                    b1 = false;
                    print(true == !b1);

                    print(!(true == b1));

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator() + "true" + System.lineSeparator(), output);
  }

  @Test
  public void unaryMinus() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var n1 : int;
                    n1 = 42;
                    print(-n1);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("-42" + System.lineSeparator(), output);
  }

  @Test
  public void inequality() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var n1 : int;
                    n1 = 42;
                    var n2 : int;
                    n2 = 32;
                    print(n1 != n2);
                    print(n1 != n1);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator() + "false" + System.lineSeparator(), output);
  }

  @Test
  public void comparisonLEQ() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var n1 : int;
                    n1 = 42;
                    var n2 : int;
                    n2 = 32;
                    print(n1 <= n1);
                    print(n1 <= n2);
                    print(n2 <= n1);
                    print(n2 <= n2);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals(
        "true"
            + System.lineSeparator()
            + "false"
            + System.lineSeparator()
            + "true"
            + System.lineSeparator()
            + "true"
            + System.lineSeparator(),
        output);
  }

  @Test
  public void comparisonLT() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var n1 : int;
                    n1 = 42;
                    var n2 : int;
                    n2 = 32;
                    print(n1 < n1);
                    print(n1 < n2);
                    print(n2 < n1);
                    print(n2 < n2);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals(
        "false"
            + System.lineSeparator()
            + "false"
            + System.lineSeparator()
            + "true"
            + System.lineSeparator()
            + "false"
            + System.lineSeparator(),
        output);
  }

  @Test
  public void comparisonGEQ() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var n1 : int;
                    n1 = 42;
                    var n2 : int;
                    n2 = 32;
                    print(n1 >= n1);
                    print(n1 >= n2);
                    print(n2 >= n1);
                    print(n2 >= n2);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals(
        "true"
            + System.lineSeparator()
            + "true"
            + System.lineSeparator()
            + "false"
            + System.lineSeparator()
            + "true"
            + System.lineSeparator(),
        output);
  }

  @Test
  public void comparisonGT() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var n1 : int;
                    n1 = 42;
                    var n2 : int;
                    n2 = 32;
                    print(n1 > n1);
                    print(n1 > n2);
                    print(n2 > n1);
                    print(n2 > n2);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals(
        "false"
            + System.lineSeparator()
            + "true"
            + System.lineSeparator()
            + "false"
            + System.lineSeparator()
            + "false"
            + System.lineSeparator(),
        output);
  }

  @Test
  public void logicOr() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var b1 : bool;
                    b1 = true;
                    var b2 : bool;
                    b2 = false;
                    print(b1 or b2);
                    print(b2 or b1);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator() + "true" + System.lineSeparator(), output);
  }

  @Test
  public void logicOrShortCircuit() {
    String program =
        testProgramPreamble
            + """
                fn lhs_true() -> bool {
                    print("lhs_true");
                    return true;
                }

                fn lhs_false() -> bool {
                    print("lhs_false");
                    return false;
                }

                fn rhs() -> bool {
                    print("rhs");
                    return true;
                }

                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    print(lhs_true() or rhs());
                    print(lhs_false() or rhs());

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals(
        "lhs_true"
            + System.lineSeparator()
            + "true"
            + System.lineSeparator()
            + "lhs_false"
            + System.lineSeparator()
            + "rhs"
            + System.lineSeparator()
            + "true"
            + System.lineSeparator(),
        output);
  }

  @Test
  public void logicAnd() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var b1 : bool;
                    b1 = true;
                    var b2 : bool;
                    b2 = true;
                    var b3 : bool;
                    b3 = false;
                    print(b1 and b2);
                    print(b1 and b3);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator() + "false" + System.lineSeparator(), output);
  }

  @Test
  public void logicAndShortCircuit() {
    String program =
        testProgramPreamble
            + """
                fn lhs_true() -> bool {
                    print("lhs_true");
                    return true;
                }

                fn lhs_false() -> bool {
                    print("lhs_false");
                    return false;
                }

                fn rhs() -> bool {
                    print("rhs");
                    return true;
                }

                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    print(lhs_true() and rhs());
                    print(lhs_false() and rhs());

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals(
        "lhs_true"
            + System.lineSeparator()
            + "rhs"
            + System.lineSeparator()
            + "true"
            + System.lineSeparator()
            + "lhs_false"
            + System.lineSeparator()
            + "false"
            + System.lineSeparator(),
        output);
  }

  @Test
  public void varDeclAssignmentInt() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var int_var = 42;
                    print(int_var);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("42" + System.lineSeparator(), output);
  }

  @Test
  public void varDeclAssignmentBool() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var bool_var = true;
                    print(bool_var);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("true" + System.lineSeparator(), output);
  }

  @Test
  public void varDeclAssignmentFuncCall() {
    String program =
        testProgramPreamble
            + """
                fn test() -> int {
                    return 42;
                }

                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var int_var = test();
                    print(int_var);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("42" + System.lineSeparator(), output);
  }

  @Test
  public void varDeclAssignmentVar() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var int_var : int;
                    int_var = 42;

                    var other_int_var = int_var;
                    print(other_int_var);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("42" + System.lineSeparator(), output);
  }

  @Test
  public void varDeclAssignmentList() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var list_var = [1,2,3];

                    print(list_var);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("[1, 2, 3]" + System.lineSeparator(), output);
  }

  @Test
  public void varDeclAssignmentListOfSets() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var set1 = <1>;
                    var set2 = <2>;
                    var set3 = <3>;
                    var list_var = [set1,set2,set3];

                    print(list_var);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.buildTask(program, outputStream, interpreter);

    String output = outputStream.toString();
    Assert.assertEquals("[[1], [2], [3]]" + System.lineSeparator(), output);

    // check for correct type of list_var
    var rtEnv = interpreter.getRuntimeEnvironment();
    FileScope fs = rtEnv.getFileScopes().values().stream().findFirst().get();
    ;
    FunctionSymbol functionSymbol = (FunctionSymbol) fs.resolve("build_task");
    FuncDefNode rootNode = functionSymbol.getAstRootNode();
    Node listVarDeclNode = rootNode.getStmts().get(5);
    Symbol varDeclSymbol = rtEnv.getSymbolTable().getSymbolsForAstNode(listVarDeclNode).getFirst();
    IType varDeclDataType = varDeclSymbol.getDataType();
    Assert.assertEquals("int<>[]", varDeclDataType.getName());
    Assert.assertTrue(varDeclDataType instanceof ListType);
    ListType listType = (ListType) varDeclDataType;
    Assert.assertTrue(listType.getElementType() instanceof SetType);
    SetType setType = (SetType) listType.getElementType();
    Assert.assertEquals(BuiltInType.intType, setType.getElementType());
  }

  @Test
  public void varDeclAssignmentSet() {
    String program =
        testProgramPreamble
            + """
                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var set_var = <1,2,3>;

                    print(set_var);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("1"));
    Assert.assertTrue(output.contains("2"));
    Assert.assertTrue(output.contains("3"));
    Assert.assertTrue(output.contains("["));
    Assert.assertTrue(output.contains("]"));
  }

  @Test
  public void varDeclAssignmentMemberAccess() {
    String program =
        testProgramPreamble
            + """
                entity_type my_type {
                  interaction_component{
                    radius: 42.0
                  }
                }

                fn build_task(single_choice_task t) -> entity<><> {
                    var return_set : entity<><>;
                    var room_set : entity<>;

                    var my_ent = instantiate(my_type);
                    var float_var = my_ent.interaction_component.radius;

                    print(float_var);

                    return_set.add(room_set);
                    return return_set;
                }
                """;

    var outputStream = new ByteArrayOutputStream();
    Helpers.buildTask(program, outputStream);

    String output = outputStream.toString();
    Assert.assertEquals("42.0" + System.lineSeparator(), output);
  }
}
