package game.gui.views;

import game.engine.Game;
import game.engine.monsters.Monster;
import game.gui.utils.SoundManager;
import game.gui.utils.ImageCache;
import game.gui.utils.PixelFont;

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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class EndView {

    private StackPane root;

    private Stage primaryStage;
    private Game game;
    private Monster winner;
    private Runnable playAgainAction;

    public EndView(Stage primaryStage, Game game, Monster winner, Runnable playAgainAction) {
        this.primaryStage = primaryStage;
        this.game = game;
        this.winner = winner;
        this.playAgainAction = playAgainAction;

        root = new StackPane();
        root.setStyle(PixelFont.cssFontFamily());

        boolean playerWon = winner == game.getPlayer();

        ImageView background = new ImageView(loadEndImage(
            playerWon ? "end_win_bg.png" : "end_lose_bg.png"
        ));

        background.setPreserveRatio(false);
        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());

        VBox content = createContent(playerWon);

        root.getChildren().addAll(background, content);
        StackPane.setAlignment(content, Pos.CENTER);

        playEndEntranceAnimation(content);

        // Play win or game-over sound (stop BG music first)
        SoundManager.getInstance().stopBgMusic();
        if (playerWon) {
            SoundManager.getInstance().playSound(SoundManager.WIN);
        } else {
            SoundManager.getInstance().playSound(SoundManager.GAME_OVER);
        }
    }

    public StackPane getRoot() {
        return root;
    }

    private VBox createContent(boolean playerWon) {
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(820);
        content.setPrefWidth(820);
        content.setPadding(new Insets(10, 20, 10, 20));
        content.setStyle("-fx-background-color: transparent;");

        ImageView header = createHeaderImage(playerWon);
        VBox statsBox = createStatsBox(playerWon);

        HBox buttons = new HBox(42);
        buttons.setAlignment(Pos.CENTER);

        Button playAgainButton = createImageButton("button_play_again.png", 245, 70);
        Button exitButton = createImageButton("button_exit.png", 245, 70);

        playAgainButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            playAgainAction.run();
        });

        exitButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            primaryStage.close();
            Platform.exit();
        });

        buttons.getChildren().addAll(playAgainButton, exitButton);

        content.getChildren().addAll(header, statsBox, buttons);

        return content;
    }

    private ImageView createHeaderImage(boolean playerWon) {
        ImageView headerView = new ImageView(loadEndImage(
            playerWon ? "end_win_header.png" : "end_lose_header.png"
        ));

        headerView.setFitWidth(500);
        headerView.setFitHeight(165);
        headerView.setPreserveRatio(true);
        headerView.setSmooth(false);

        headerView.setEffect(new DropShadow(
            28,
            Color.web(playerWon ? "#FFD85A" : "#FF4FD8")
        ));

        playHeaderPulseAnimation(headerView);

        return headerView;
    }

    private VBox createStatsBox(boolean playerWon) {
        VBox statsBox = new VBox(14);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPrefWidth(700);
        statsBox.setMaxWidth(700);
        statsBox.setPadding(new Insets(20, 28, 20, 28));

        statsBox.setStyle(
            "-fx-background-color: rgba(12, 5, 35, 0.66);" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: rgba(190, 100, 255, 0.55);" +
            "-fx-border-width: 1.8;" +
            "-fx-border-radius: 18;"
        );

        statsBox.setEffect(new DropShadow(18, Color.web("#5F2CCB")));

        Label header = new Label(playerWon ? "MATCH RESULTS" : "RUN RESULTS");
        header.setAlignment(Pos.CENTER);
        header.setTextAlignment(TextAlignment.CENTER);
        header.setMaxWidth(660);
        header.setStyle(
            PixelFont.cssFontFamily() +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #CFF7FF;"
        );
        header.setEffect(new DropShadow(12, Color.web("#B86CFF")));
        PixelFont.condense(header, 0.74);

        Label summary = new Label(playerWon
            ? "Your monster reached the exit first."
            : "The opponent reached the exit first."
        );

        summary.setAlignment(Pos.CENTER);
        summary.setTextAlignment(TextAlignment.CENTER);
        summary.setMaxWidth(660);
        summary.setWrapText(true);
        summary.setStyle(
            PixelFont.cssFontFamily() +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        PixelFont.condense(summary, 0.72);

        HBox statsColumns = new HBox(26);
        statsColumns.setAlignment(Pos.CENTER);

        VBox leftColumn = createStatsColumn(
            "WINNER",
            "Name: " + winner.getName(),
            "Role: " + winner.getRole(),
            "Energy: " + winner.getEnergy(),
            "Position: " + winner.getPosition()
        );

        VBox rightColumn = createStatsColumn(
            "PLAYERS",
            "Player: " + game.getPlayer().getName(),
            "P Energy: " + game.getPlayer().getEnergy(),
            "Opponent: " + game.getOpponent().getName(),
            "O Energy: " + game.getOpponent().getEnergy()
        );

        statsColumns.getChildren().addAll(leftColumn, rightColumn);

        Label positions = new Label(
            "Player Position: " + game.getPlayer().getPosition() +
            "     |     Opponent Position: " + game.getOpponent().getPosition()
        );

        positions.setAlignment(Pos.CENTER);
        positions.setTextAlignment(TextAlignment.CENTER);
        positions.setWrapText(true);
        positions.setMaxWidth(660);
        positions.setStyle(
            PixelFont.cssFontFamily() +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #FFE9A8;"
        );
        PixelFont.condense(positions, 0.62);

        statsBox.getChildren().addAll(
            header,
            summary,
            statsColumns,
            positions
        );

        return statsBox;
    }

    private VBox createStatsColumn(String title, String line1, String line2, String line3, String line4) {
        VBox column = new VBox(8);
        column.setAlignment(Pos.TOP_LEFT);
        column.setPrefWidth(310);
        column.setMaxWidth(310);
        column.setPadding(new Insets(14, 16, 14, 16));

        column.setStyle(
            "-fx-background-color: rgba(20, 8, 48, 0.62);" +
            "-fx-background-radius: 13;" +
            "-fx-border-color: rgba(158, 230, 255, 0.35);" +
            "-fx-border-width: 1.2;" +
            "-fx-border-radius: 13;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            PixelFont.cssFontFamily() +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #9EE6FF;"
        );
        PixelFont.condense(titleLabel, 0.76);

        Label l1 = createStatLine(line1);
        Label l2 = createStatLine(line2);
        Label l3 = createStatLine(line3);
        Label l4 = createStatLine(line4);

        column.getChildren().addAll(titleLabel, l1, l2, l3, l4);

        return column;
    }

    private Label createStatLine(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(280);
        label.setStyle(
            PixelFont.cssFontFamily() +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #F8EEFF;"
        );
        PixelFont.condense(label, 0.68);
        return label;
    }

    private Button createImageButton(String imageName, int width, int height) {
        ImageView imageView = new ImageView(loadEndImage(imageName));
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
            button.setEffect(new DropShadow(18, Color.web("#FF5DC8")));
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

    private void playHeaderPulseAnimation(ImageView header) {
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(header.scaleXProperty(), 1.0),
                new KeyValue(header.scaleYProperty(), 1.0)
            ),
            new KeyFrame(Duration.millis(750),
                new KeyValue(header.scaleXProperty(), 1.06),
                new KeyValue(header.scaleYProperty(), 1.06)
            ),
            new KeyFrame(Duration.millis(1400),
                new KeyValue(header.scaleXProperty(), 1.0),
                new KeyValue(header.scaleYProperty(), 1.0)
            )
        );

        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();
    }

    private void playEndEntranceAnimation(VBox content) {
        content.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(700), content);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        ScaleTransition scale = new ScaleTransition(Duration.millis(700), content);
        scale.setFromX(0.95);
        scale.setFromY(0.95);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }

    private Image loadEndImage(String fileName) {
        return ImageCache.get("/game/gui/assets/end/" + fileName);
    }
}