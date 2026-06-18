package game.gui.utils;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;

public class ImageCache {

    private static final Map<String, Image> cache = new HashMap<>();

    public static Image get(String path) {
        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        java.net.URL url = ImageCache.class.getResource(path);

        if (url == null) {
            throw new RuntimeException("Missing image: " + path);
        }

        Image image = new Image(url.toExternalForm(), false);

        if (image.isError()) {
            throw new RuntimeException(
                "Could not load image: " + path + "\n" + image.getException()
            );
        }

        cache.put(path, image);
        return image;
    }
}