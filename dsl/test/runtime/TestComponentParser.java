package runtime;

import org.junit.Test;

import java.util.List;

public class TestComponentParser {

    @Test
    public void testParseComponents() {
        DSLTypeParser cParser = new DSLTypeParser();
        List<Class<?>> testList = cParser.parseComponents();

        assert testList.size() > 0;
        assert testList.size() == 7; // current static amount of found classes

    }
}
