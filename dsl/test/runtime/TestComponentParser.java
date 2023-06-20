package runtime;

import org.junit.Test;
import semanticanalysis.types.DSLType;

import java.util.List;

public class TestComponentParser {

    @Test
    public void testParseComponents() {
        DSLTypeParser cParser = new DSLTypeParser();
        List<Class<?>> testList = cParser.parseComponents(DSLType.class);

        assert testList.size() > 0;
        assert testList.size() == 7; // current static amount of found classes

    }
}
