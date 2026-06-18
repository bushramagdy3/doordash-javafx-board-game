package game.gui.utils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.Region;

public class AlertHelper {

    public static void showError(String titleText, String messageText) {
        Stage popup = createBasePopup(titleText);

        VBox root = createPopupRoot(
            "rgba(55, 8, 42, 0.94)",
            "rgba(255, 93, 200, 0.95)",
            "#FF5DC8"
        );

        HBox header = createHeader(
            "/game/gui/assets/popups/popup_alert_icon.png",
            titleText,
            "#FF8AD8",
            82
        );

        Label message = createMessageLabel(messageText, "#FFFFFF", 440);
        message.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-line-spacing: 3px;"
        );

        Button understood = createImageButton(
            "/game/gui/assets/popups/button_understood_red.png",
            245,
            70,
            "#FF5DC8"
        );
        understood.setOnAction(e -> popup.close());

        root.getChildren().addAll(header, message, understood);

        Scene scene = new Scene(root, 620, 330);
        scene.setFill(Color.TRANSPARENT);

        popup.setScene(scene);
        popup.showAndWait();
    }

    public static void showCard(String cardName, String effectText) {
        Stage popup = createBasePopup("Card Drawn");

        VBox root = createPopupRoot(
            "rgba(8, 24, 58, 0.94)",
            "rgba(80, 225, 255, 0.95)",
            "#00E5FF"
        );

        HBox header = createHeader(
            "/game/gui/assets/controls/card_icon.png",
            cardName,
            "#9EE6FF",
            90
        );

        Label effect = createMessageLabel("Effect:\n" + effectText, "#FFFFFF", 480);
        effect.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-line-spacing: 4px;"
        );

        Button understood = createImageButton(
            "/game/gui/assets/popups/button_understood_blue.png",
            245,
            70,
            "#00E5FF"
        );
        understood.setOnAction(e -> popup.close());

        root.getChildren().addAll(header, effect, understood);

        Scene scene = new Scene(root, 650, 360);
        scene.setFill(Color.TRANSPARENT);

        popup.setScene(scene);
        popup.showAndWait();
    }

    public static void showInstructions(Stage owner) {
        Stage popup = createBasePopup("Instructions");

        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox root = createPopupRoot(
            "rgba(26, 10, 62, 0.95)",
            "rgba(190, 100, 255, 0.95)",
            "#B86CFF"
        );

        ImageView headerImage = new ImageView(loadImage("/game/gui/assets/popups/popup_instructions_header.png"));
        headerImage.setFitWidth(560);
        headerImage.setFitHeight(110);
        headerImage.setPreserveRatio(true);
        headerImage.setEffect(new DropShadow(18, Color.web("#B86CFF")));

        Label instructions = new Label(
            "Welcome, little dasher!\n\n" +

            "Beyond these glowing doors, two monsters race through a magical board of tricks, traps, and surprises.\n\n" +

            "First, choose your side: SCARER or LAUGHER. Each side gains energy from different doors, so every path can help you or hurt you.\n\n" +

            "On your turn, you may activate your powerup before rolling. If you use it, your monster spends energy to gain a special advantage.\n\n" +

            "Then roll the dice and dash forward. Your monster moves according to the dice number, and the cell you land on decides what happens next.\n\n" +

            "Door cells can increase or decrease your energy depending on your role and the door type. Scarer doors are useful for SCARERS, while laugher doors are useful for LAUGHERS.\n\n" +

            "Monster cells contain stationed monsters and show their energy. Card cells reveal a mystery card. Conveyor cells push your monster ahead. Sock cells may contaminate your monster.\n\n" +

            "Cards can shield you, freeze you, confuse your role, swap positions, move monsters, or change energy. Watch the Last Card Drawn box to remember what happened.\n\n" +

            "If your monster is frozen, it skips its turn. If it is confused, its current role changes temporarily. If it is shielded, it can block harmful effects.\n\n" +

            "Keep watching your energy. Having a high position is not enough by itself your monster must also have enough energy to escape.\n\n" +

            "To win the game, your monster must reach cell 99 with more than 1000 energy.\n\n" +

            "The first monster to reach cell 99 with more than 1000 energy becomes the winner of the dash!\n\n" +

            "TA Testing Shortcuts:\n" +
            " Press W to instantly trigger the win screen.\n" +
            " Press L to instantly trigger the lose screen.\n" +
            " Press E to add 500 energy to the current monster.\n\n" +
            "These shortcuts are included only to make testing and grading faster."
        );

        instructions.setWrapText(true);
        instructions.setMaxWidth(650);
        instructions.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-line-spacing: 5px;"
        );
        PixelFont.condenseWrapped(instructions, 0.78, 625, 5);

        VBox scrollContent = new VBox(instructions);
        scrollContent.setPadding(new Insets(12));
        scrollContent.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxWidth(680);
        scrollPane.setPrefViewportHeight(360);
        scrollPane.setPannable(true);

        scrollPane.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background: transparent;" +
            "-fx-control-inner-background: transparent;" +
            "-fx-border-color: rgba(190, 100, 255, 0.35);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 14;" +
            "-fx-background-radius: 14;"
        );

        Button understood = createImageButton(
            "/game/gui/assets/popups/button_understood_purple.png",
            245,
            70,
            "#B86CFF"
        );
        understood.setOnAction(e -> popup.close());

        root.getChildren().addAll(headerImage, scrollPane, understood);

        Scene scene = new Scene(root, 760, 650);
        scene.setFill(Color.TRANSPARENT);

        popup.setScene(scene);

        Platform.runLater(() -> {
            if (scrollPane.lookup(".viewport") != null) {
                scrollPane.lookup(".viewport").setStyle("-fx-background-color: transparent;");
            }

            if (scrollPane.lookup(".scroll-pane") != null) {
                scrollPane.lookup(".scroll-pane").setStyle("-fx-background-color: transparent;");
            }

            scrollContent.setStyle("-fx-background-color: transparent;");
        });

        popup.showAndWait();
    }

    private static Stage createBasePopup(String title) {
        Stage popup = new Stage();
        popup.setTitle(title);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setResizable(false);
        return popup;
    }

    private static VBox createPopupRoot(String bgColor, String borderColor, String glowColor) {
        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(28));

        root.setStyle(
            PixelFont.cssFontFamily() +
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 22;" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 22;"
        );

        root.setEffect(new DropShadow(24, Color.web(glowColor)));

        return root;
    }

    private static HBox createHeader(
    	    String iconPath,
    	    String titleText,
    	    String titleColor,
    	    int iconSize
    	) {
    	    HBox header = new HBox(18);
    	    header.setAlignment(Pos.CENTER);

    	    // Prevent the HBox from stretching across the entire popup.
    	    // The root VBox will then center the icon-title group properly.
    	    header.setMaxWidth(Region.USE_PREF_SIZE);

    	    ImageView icon = new ImageView(loadImage(iconPath));
    	    icon.setFitWidth(iconSize);
    	    icon.setFitHeight(iconSize);
    	    icon.setPreserveRatio(true);
    	    icon.setSmooth(false);
    	    icon.setEffect(new DropShadow(16, Color.web(titleColor)));

    	    Label title = new Label(titleText);
    	    title.setWrapText(false);
    	    title.setAlignment(Pos.CENTER);
    	    title.setTextAlignment(TextAlignment.CENTER);

    	    title.setStyle(
    	        "-fx-font-size: 23px;" +
    	        "-fx-font-weight: bold;" +
    	        "-fx-text-fill: " + titleColor + ";"
    	    );

    	    title.setEffect(new DropShadow(12, Color.web(titleColor)));

    	    PixelFont.condense(title, 0.72);

    	    header.getChildren().addAll(icon, title);

    	    return header;
    }

    private static Label createMessageLabel(String text, String color, int width) {
        Label message = new Label(text == null ? "" : text);
        message.setWrapText(true);
        message.setMaxWidth(width);
        message.setAlignment(Pos.CENTER);
        message.setTextAlignment(TextAlignment.CENTER);
        message.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + color + ";"
        );
        PixelFont.condenseWrapped(message, 0.76, width - 20, 4);
        return message;
    }

    private static Button createImageButton(String imagePath, int width, int height, String glowColor) {
        Button button = new Button();
        button.setCursor(Cursor.HAND);

        ImageView imageView = new ImageView(loadImage(imagePath));
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(false);

        button.setGraphic(imageView);
        button.setMinSize(width, height);
        button.setPrefSize(width, height);
        button.setMaxSize(width, height);

        button.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-padding: 0;" +
            "-fx-border-color: transparent;" +
            "-fx-background-insets: 0;" +
            "-fx-border-insets: 0;"
        );

        button.setOnMouseEntered(e -> {
            button.setScaleX(1.05);
            button.setScaleY(1.05);
            button.setEffect(new DropShadow(18, Color.web(glowColor)));
        });

        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
            button.setEffect(null);
        });

        return button;
    }

    private static Image loadImage(String path) {
        return ImageCache.get(path);
    }
}