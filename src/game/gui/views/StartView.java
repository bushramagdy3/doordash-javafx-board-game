package game.gui.views;

import game.engine.Role;
import game.gui.utils.AlertHelper;
import game.gui.utils.ImageCache;
import game.gui.utils.PixelFont;
import game.gui.utils.SoundManager;

import java.util.function.Consumer;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class StartView {

    private StackPane root;

    private Button scarerButton;
    private Button laugherButton;
    private Button startButton;
    private Button instructionsButton;
    private Button muteButton;
    private Button exitButton;
    private ImageView muteIconView;

    private Role selectedRole;

    private Stage primaryStage;
    private Consumer<Role> startGameAction;

    public StartView(Stage primaryStage, Consumer<Role> startGameAction) {
        this.primaryStage = primaryStage;
        this.startGameAction = startGameAction;
        this.selectedRole = null;

        root = new StackPane();
        root.setStyle(PixelFont.cssFontFamily());

        ImageView background = new ImageView(loadStartImage("start_bg.png"));
        background.setPreserveRatio(false);
        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());

        VBox menu = new VBox(22);
        menu.setAlignment(Pos.CENTER);
        menu.setMaxWidth(900);
        menu.setTranslateY(-35);

        ImageView logo = new ImageView(loadStartImage("start_logo.png"));
        logo.setFitWidth(540);
        logo.setFitHeight(160);
        logo.setPreserveRatio(true);
        logo.setEffect(new DropShadow(22, Color.web("#B86CFF")));
        playLogoPulseAnimation(logo);

        Label subtitle = new Label("Choose your role and dash through the doors!");
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #F8EEFF;" +
            "-fx-padding: 6 18 6 18;" +
            "-fx-background-color: rgba(35, 12, 70, 0.45);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: rgba(190, 100, 255, 0.45);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 14;"
        );
        subtitle.setEffect(new DropShadow(10, Color.web("#B86CFF")));
        PixelFont.condense(subtitle, 0.74);

        scarerButton = createImageButton("button_scarer.png", 330, 160);
        laugherButton = createImageButton("button_laugher.png", 330, 160);

        HBox roleButtons = new HBox(34);
        roleButtons.setAlignment(Pos.CENTER);
        roleButtons.getChildren().addAll(scarerButton, laugherButton);

        startButton = createImageButton("button_start_game.png", 310, 86);
        instructionsButton = createImageButton("button_view_instructions.png", 310, 86);

        HBox bottomButtons = new HBox(24);
        bottomButtons.setAlignment(Pos.CENTER);
        bottomButtons.getChildren().addAll(startButton, instructionsButton);

        Label hint = new Label("Tip: SCARER gains from scare energy. LAUGHER gains from laugh energy.");
        hint.setAlignment(Pos.CENTER);
        hint.setTextAlignment(TextAlignment.CENTER);
        hint.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #BFEFFF;" +
            "-fx-padding: 5 14 5 14;" +
            "-fx-background-color: rgba(10, 5, 30, 0.42);" +
            "-fx-background-radius: 12;"
        );
        PixelFont.condense(hint, 0.66);

        menu.getChildren().addAll(
            logo,
            subtitle,
            roleButtons,
            bottomButtons,
            hint
        );

        muteButton = createControlIconButton(SoundManager.getInstance().isMuted() ? "button_sound_muted.png" : "button_sound_on.png", 58, 58);
        exitButton = createControlIconButton("button_exit_circle.png", 58, 58);
        muteIconView = (ImageView) muteButton.getGraphic();

        HBox topRightControls = new HBox(10);
        topRightControls.setAlignment(Pos.TOP_RIGHT);
        topRightControls.setPadding(new Insets(20, 28, 0, 0));
        topRightControls.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        topRightControls.setPickOnBounds(false);
        topRightControls.getChildren().addAll(muteButton, exitButton);

        root.getChildren().addAll(background, menu, topRightControls);
        StackPane.setAlignment(menu, Pos.CENTER);
        StackPane.setAlignment(topRightControls, Pos.TOP_RIGHT);

        setupActions();
        playStartEntranceAnimation(menu);

        // Start intro background music
        SoundManager.getInstance().playBgMusic(SoundManager.INTRO_BG);
    }

    public StackPane getRoot() {
        return root;
    }

    private void setupActions() {
        scarerButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            selectedRole = Role.SCARER;
            updateRoleSelection();
        });

        laugherButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            selectedRole = Role.LAUGHER;
            updateRoleSelection();
        });

        startButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            if (selectedRole == null) {
                SoundManager.getInstance().playSound(SoundManager.ERROR_POPUP);
                AlertHelper.showError(
                    "Choose Your Role",
                    "Please choose SCARER or LAUGHER before starting the game."
                );
                return;
            }

            startGameAction.accept(selectedRole);
        });

        instructionsButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            AlertHelper.showInstructions(primaryStage);
        });

        muteButton.setOnAction(e -> {
            boolean nowMuted = SoundManager.getInstance().toggleMute();

            if (nowMuted) {
                muteIconView.setImage(loadControlImage("button_sound_muted.png"));
            } else {
                muteIconView.setImage(loadControlImage("button_sound_on.png"));
            }

            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            playButtonHoverAnimation(muteButton, false);
        });

        exitButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            primaryStage.close();
            Platform.exit();
        });
    }

    private void updateRoleSelection() {
        scarerButton.setOpacity(0.78);
        laugherButton.setOpacity(0.78);

        scarerButton.setEffect(null);
        laugherButton.setEffect(null);

        if (selectedRole == Role.SCARER) {
            scarerButton.setOpacity(1.0);
            scarerButton.setEffect(new DropShadow(32, Color.web("#B86CFF")));
        }

        if (selectedRole == Role.LAUGHER) {
            laugherButton.setOpacity(1.0);
            laugherButton.setEffect(new DropShadow(32, Color.web("#FF5DC8")));
        }
    }

    private Button createImageButton(String imageName, int width, int height) {
        ImageView imageView = new ImageView(loadStartImage(imageName));
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(false);

        Button button = new Button();
        button.setGraphic(imageView);

        button.setMinSize(width, height);
        button.setPrefSize(width, height);
        button.setMaxSize(width, height);

        button.setCursor(Cursor.HAND);

        button.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-padding: 0;" +
            "-fx-border-color: transparent;" +
            "-fx-background-insets: 0;" +
            "-fx-border-insets: 0;"
        );

        button.setOnMouseEntered(e -> {
            if (button == scarerButton && selectedRole == Role.SCARER) {
                return;
            }

            if (button == laugherButton && selectedRole == Role.LAUGHER) {
                return;
            }

            button.setEffect(new DropShadow(18, Color.web("#C16CFF")));
            button.setOpacity(1.0);
            playButtonHoverAnimation(button, true);
        });

        button.setOnMouseExited(e -> {
            if (button == scarerButton && selectedRole == Role.SCARER) {
                return;
            }

            if (button == laugherButton && selectedRole == Role.LAUGHER) {
                return;
            }

            button.setEffect(null);
            playButtonHoverAnimation(button, false);

            if (button == scarerButton || button == laugherButton) {
                button.setOpacity(0.78);
            }
        });

        return button;
    }

    private Button createControlIconButton(String imageName, int width, int height) {
        ImageView imageView = new ImageView(loadControlImage(imageName));
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(false);

        Button button = new Button();
        button.setGraphic(imageView);

        button.setMinSize(width, height);
        button.setPrefSize(width, height);
        button.setMaxSize(width, height);
        button.setCursor(Cursor.HAND);

        button.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-padding: 0;" +
            "-fx-border-color: transparent;" +
            "-fx-background-insets: 0;" +
            "-fx-border-insets: 0;"
        );

        button.setOnMouseEntered(e -> {
            button.setEffect(new DropShadow(18, Color.web("#B86CFF")));
            playButtonHoverAnimation(button, true);
        });

        button.setOnMouseExited(e -> {
            button.setEffect(null);
            playButtonHoverAnimation(button, false);
        });

        return button;
    }

    private void playButtonHoverAnimation(Button button, boolean hover) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(120), button);
        scale.setToX(hover ? 1.05 : 1.0);
        scale.setToY(hover ? 1.05 : 1.0);
        scale.play();
    }

    private void playStartEntranceAnimation(VBox menu) {
        menu.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(650), menu);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        ScaleTransition scale = new ScaleTransition(Duration.millis(650), menu);
        scale.setFromX(0.96);
        scale.setFromY(0.96);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }

    private void playLogoPulseAnimation(ImageView logo) {
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(logo.scaleXProperty(), 1.0),
                new KeyValue(logo.scaleYProperty(), 1.0)
            ),
            new KeyFrame(Duration.millis(750),
                new KeyValue(logo.scaleXProperty(), 1.06),
                new KeyValue(logo.scaleYProperty(), 1.06)
            ),
            new KeyFrame(Duration.millis(1400),
                new KeyValue(logo.scaleXProperty(), 1.0),
                new KeyValue(logo.scaleYProperty(), 1.0)
            )
        );

        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();
    }

    private Image loadControlImage(String fileName) {
        return ImageCache.get("/game/gui/assets/controls/" + fileName);
    }

    private Image loadStartImage(String fileName) {
        return ImageCache.get("/game/gui/assets/start/" + fileName);
    }
}