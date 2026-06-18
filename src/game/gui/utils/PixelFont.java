package game.gui.utils;

import javafx.scene.Group;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;

public final class PixelFont {

    private static final String FALLBACK_FONT = "Consolas";
    private static String family;

    static {
        try {
            java.net.URL url = PixelFont.class.getResource(
                "/game/gui/assets/fonts/pixel_font.ttf"
            );

            if (url != null) {
                Font loaded = Font.loadFont(url.toExternalForm(), 14);

                if (loaded != null
                    && loaded.getFamily() != null
                    && !loaded.getFamily().trim().isEmpty()) {

                    family = loaded.getFamily();
                }
            }
        } catch (Exception ignored) {
        }

        if (family == null || family.trim().isEmpty()) {
            family = FALLBACK_FONT;
        }
    }

    private PixelFont() {
    }

    public static String getFamily() {
        return family;
    }

    public static String cssFontFamily() {
        return "-fx-font-family: '"
            + family.replace("'", "\\'")
            + "';";
    }

    /**
     * Compresses only the visible letters horizontally. The Label's
     * background, border, padding and vertical letter size stay unchanged.
     */
    public static void condense(Label label, double scaleX) {
        applyCondensedText(label, scaleX, 0, 0);
    }

    /**
     * Horizontal compression for multi-line labels. contentWidth is the
     * desired visible width of the compressed text, excluding Label padding.
     */
    public static void condenseWrapped(
        Label label,
        double scaleX,
        double contentWidth,
        double lineSpacing
    ) {
        applyCondensedText(label, scaleX, contentWidth, lineSpacing);
    }

    private static void applyCondensedText(
        Label label,
        double scaleX,
        double contentWidth,
        double lineSpacing
    ) {
        if (label == null) {
            return;
        }

        if (scaleX <= 0 || scaleX > 1) {
            throw new IllegalArgumentException(
                "scaleX must be greater than 0 and at most 1."
            );
        }

        Text text = new Text();
        text.textProperty().bind(label.textProperty());
        text.fontProperty().bind(label.fontProperty());
        text.fillProperty().bind(label.textFillProperty());
        text.textAlignmentProperty().bind(label.textAlignmentProperty());
        text.setLineSpacing(lineSpacing);

        if (contentWidth > 0) {
            text.setWrappingWidth(contentWidth / scaleX);
        }

        text.getTransforms().add(new Scale(scaleX, 1.0, 0, 0));

        // A Group reports the transformed text bounds to the Label, so the
        // layout uses the compressed width rather than clipping the full text.
        Group compressedText = new Group(text);

        label.setGraphic(compressedText);
        label.setGraphicTextGap(0);
        label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }
}