package runtime;

import org.junit.Test;

import java.util.List;

public class TestComponentParser {

    @Test
    public void testParseComponents() {
        ComponentParser cParser = new ComponentParser();
        List<Class<?>> testList = cParser.parseComponents();

        assert testList.size() > 0;

    }
}
