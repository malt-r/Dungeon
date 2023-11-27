package core.gui;

import core.utils.math.Vector2f;

import java.util.List;

public interface IGUILayout {
    void layout(GUIElement parent, List<GUIElement> elements);

    Vector2f calcMinSize(GUIElement parent, List<GUIElement> elements);
}