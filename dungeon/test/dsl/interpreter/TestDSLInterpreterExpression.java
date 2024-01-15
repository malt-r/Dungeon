package dsl.interpreter;

import dsl.helpers.Helpers;
import org.junit.Assert;
import org.junit.Test;
import java.io.ByteArrayOutputStream;

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
        testProgramPreamble +
        """
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
            testProgramPreamble +
                """
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
            testProgramPreamble +
                """
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
}
