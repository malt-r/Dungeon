package core.gui;

import core.gui.backend.BackendImage;
import core.utils.math.Vector2f;
import core.utils.math.Vector4f;

public class GUIImage extends GUIElement {

    public enum ScaleMode {
        CONTAIN,
        COVER,
        STRETCH
    }

    private BackendImage image;

    private ScaleMode scaleMode = ScaleMode.COVER;

    public GUIImage(BackendImage image) {
        this.image = image;
        this.backgroundColor =
                new Vector4f(
                        (float) Math.random(), (float) Math.random(), (float) Math.random(), 1f);
    }

    public BackendImage image() {
        return this.image;
    }

    public GUIImage image(BackendImage image) {
        this.image = image;
        return this;
    }

    public ScaleMode scaleMode() {
        return scaleMode;
    }

    public GUIImage scaleMode(ScaleMode scaleMode) {
        this.scaleMode = scaleMode;
        return this;
    }

    @Override
    public Vector2f minimalSize() {
        float aspectRatio = (float) this.image.width() / (float) this.image.height();
        return new Vector2f(100, 100 / aspectRatio);
    }

    @Override
    public Vector2f preferredSize() {
        return new Vector2f(this.image.width(), this.image.height());
    }
}
