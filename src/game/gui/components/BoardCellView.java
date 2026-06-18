package game.gui.components;

import java.util.HashMap;

import game.engine.cells.CardCell;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import game.engine.cells.TransportCell;
import game.engine.monsters.Monster;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

public class BoardCellView {

    private StackPane root;
    private ImageView backgroundImageView;
    private Label indexLabel;
    private Label energyLabel;
    private HBox tokenBox;
    private Pane highlightOverlay;

    private int index;
    private Cell cell;

    private static HashMap<String, Image> imageCache = new HashMap<String, Image>();

    public BoardCellView(int index, Cell cell) {
        this.index = index;
        this.cell = cell;

        root = new StackPane();
        root.setPrefSize(80, 80);
        root.setMinSize(80, 80);
        root.setMaxSize(80, 80);
        root.setStyle("-fx-background-color: transparent;");

        backgroundImageView = new ImageView();
        backgroundImageView.setFitWidth(80);
        backgroundImageView.setFitHeight(80);
        backgroundImageView.setPreserveRatio(false);

        indexLabel = new Label(String.valueOf(index));
        indexLabel.setMinWidth(34);
        indexLabel.setPrefWidth(34);
        indexLabel.setAlignment(Pos.CENTER);
        indexLabel.setStyle(
            "-fx-font-size: 9px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #F5D7FF;" +
            "-fx-padding: 0;"
        );
        indexLabel.setEffect(new DropShadow(5, Color.web("#7B2AFF")));
        StackPane.setAlignment(indexLabel, Pos.TOP_CENTER);
        StackPane.setMargin(indexLabel, new Insets(3, 0, 0, 0));

        energyLabel = new Label();
        energyLabel.setMinWidth(42);
        energyLabel.setPrefWidth(42);
        energyLabel.setAlignment(Pos.CENTER);
        energyLabel.setStyle(
            "-fx-font-size: 8px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #F5D7FF;" +
            "-fx-padding: 0;"
        );
        energyLabel.setEffect(new DropShadow(5, Color.web("#7B2AFF")));
        StackPane.setAlignment(energyLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(energyLabel, new Insets(0, 0, 5, 0));

        tokenBox = new HBox(2);
        tokenBox.setAlignment(Pos.CENTER);
        tokenBox.setMouseTransparent(true);
        StackPane.setAlignment(tokenBox, Pos.CENTER);

        highlightOverlay = createHighlightOverlay();
        highlightOverlay.setVisible(false);
        highlightOverlay.setOpacity(0.0);
        StackPane.setAlignment(highlightOverlay, Pos.CENTER);

        root.getChildren().addAll(
            backgroundImageView,
            highlightOverlay,
            tokenBox,
            indexLabel,
            energyLabel
        );

        setCellBackground(getCellImageName(cell));
        updateEnergyLabel(cell);
    }

    public StackPane getRoot() {
        return root;
    }

    public void addMonsterToken(Monster monster) {
        String imageName = getMonsterTokenFileName(monster.getName());

        if (imageName == null) {
            return;
        }

        String glowColor = getMonsterGlowColor(monster);

        int tokenCountAfterAdding = tokenBox.getChildren().size() + 1;

        if (tokenCountAfterAdding >= 2) {
            resizeExistingTokensForSharedCell();
            tokenBox.setSpacing(-6);
        } else {
            tokenBox.setSpacing(2);
        }

        StackPane tokenHolder;

        if (tokenCountAfterAdding >= 2) {
            tokenHolder = createGlowingToken(imageName, glowColor, 42, 38, 18);
        } else {
            tokenHolder = createGlowingToken(imageName, glowColor, 58, 50, 26);
        }

        tokenBox.getChildren().add(tokenHolder);

        setHighlighted(true, glowColor);
        playTokenBounce(tokenHolder);
    }

    public void refreshStyle(Cell cell) {
        this.cell = cell;
        setCellBackground(getCellImageName(cell));
        updateEnergyLabel(cell);
        tokenBox.getChildren().clear();
        tokenBox.setSpacing(2);
        setHighlighted(false, "#9EE6FF");
    }

    private StackPane createGlowingToken(String imageName, String glowColor, int holderSize, int tokenSize, int circleRadius) {
        StackPane tokenHolder = new StackPane();
        tokenHolder.setPrefSize(holderSize, holderSize);
        tokenHolder.setMinSize(holderSize, holderSize);
        tokenHolder.setMaxSize(holderSize, holderSize);
        tokenHolder.setAlignment(Pos.CENTER);
        tokenHolder.setMouseTransparent(true);

        Circle glowCircle = new Circle(circleRadius);
        glowCircle.setFill(Color.web(glowColor, 0.28));
        glowCircle.setStroke(Color.web(glowColor));
        glowCircle.setStrokeWidth(2.0);
        glowCircle.setEffect(new DropShadow(16, Color.web(glowColor)));

        ImageView token = createImageView(imageName, tokenSize, tokenSize);
        token.setEffect(new DropShadow(12, Color.web(glowColor)));

        tokenHolder.getChildren().addAll(glowCircle, token);

        return tokenHolder;
    }

    private void resizeExistingTokensForSharedCell() {
        for (javafx.scene.Node node : tokenBox.getChildren()) {
            if (node instanceof StackPane) {
                StackPane holder = (StackPane) node;

                holder.setPrefSize(42, 42);
                holder.setMinSize(42, 42);
                holder.setMaxSize(42, 42);

                for (javafx.scene.Node child : holder.getChildren()) {
                    if (child instanceof Circle) {
                        Circle circle = (Circle) child;
                        circle.setRadius(18);
                        circle.setStrokeWidth(2.0);
                    }

                    if (child instanceof ImageView) {
                        ImageView image = (ImageView) child;
                        image.setFitWidth(38);
                        image.setFitHeight(38);
                    }
                }
            }
        }
    }

    private void playTokenBounce(StackPane tokenHolder) {
        ScaleTransition bounce = new ScaleTransition(Duration.millis(650), tokenHolder);

        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.14);
        bounce.setToY(1.14);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(ScaleTransition.INDEFINITE);

        tokenHolder.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                bounce.stop();
            }
        });

        bounce.play();
    }

    private String getMonsterGlowColor(Monster monster) {
        String role = String.valueOf(monster.getRole());

        if (role.equalsIgnoreCase("SCARER")) {
            return "#FF5DC8";
        }

        if (role.equalsIgnoreCase("LAUGHER")) {
            return "#00E5FF";
        }

        return "#B86CFF";
    }

    private String getCellImageName(Cell cell) {
        if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;

            if (door.isActivated()) {
                return "activated_door_cell.png";
            }

            if (String.valueOf(door.getRole()).equals("SCARER")) {
                return "scarer_door_cell.png";
            }

            return "laugher_door_cell.png";
        }

        if (cell instanceof MonsterCell) {
            Monster stationedMonster = ((MonsterCell) cell).getCellMonster();
            return getMonsterCellFileName(stationedMonster.getName());
        }

        if (cell instanceof CardCell) {
            return "card_cell.png";
        }

        if (cell instanceof ConveyorBelt) {
            return "conveyor_cell.png";
        }

        if (cell instanceof ContaminationSock) {
            return "sock_cell.png";
        }

        return "normal_cell.png";
    }

    private void updateEnergyLabel(Cell cell) {
        if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;
            energyLabel.setText(String.valueOf(door.getEnergy()));
        }

        else if (cell instanceof MonsterCell) {
            Monster stationedMonster = ((MonsterCell) cell).getCellMonster();
            energyLabel.setText(String.valueOf(stationedMonster.getEnergy()));
        }

        else if (cell instanceof TransportCell) {
            TransportCell transportCell = (TransportCell) cell;
            int effect = transportCell.getEffect();
            energyLabel.setText(effect > 0 ? "+" + effect : String.valueOf(effect));
        }

        else {
            energyLabel.setText("");
        }
    }

    private String getMonsterCellFileName(String monsterName) {
        String name = monsterName.toLowerCase();

        if (name.equals("mike wazowski")) {
            return "monster_cell_mike_wazowski.png";
        }

        if (name.equals("james p. sullivan")) {
            return "monster_cell_james_p_sullivan.png";
        }

        if (name.equals("randall boggs")) {
            return "monster_cell_randall_boggs.png";
        }

        if (name.equals("celia mae")) {
            return "monster_cell_celia_mae.png";
        }

        if (name.equals("fungus")) {
            return "monster_cell_fungus.png";
        }

        if (name.equals("yeti")) {
            return "monster_cell_yeti.png";
        }

        return "normal_cell.png";
    }

    private String getMonsterTokenFileName(String monsterName) {
        String name = monsterName.toLowerCase();

        if (name.equals("mike wazowski")) {
            return "token_mike_wazowski.png";
        }

        if (name.equals("james p. sullivan")) {
            return "token_james_p_sullivan.png";
        }

        if (name.equals("randall boggs")) {
            return "token_randall_boggs.png";
        }

        if (name.equals("celia mae")) {
            return "token_celia_mae.png";
        }

        if (name.equals("fungus")) {
            return "token_fungus.png";
        }

        if (name.equals("yeti")) {
            return "token_yeti.png";
        }

        return null;
    }

    private void setCellBackground(String imageName) {
        Image image = getImage(imageName, 80, 80);
        backgroundImageView.setImage(image);
    }

    private ImageView createImageView(String imageName, int width, int height) {
        Image image = getImage(imageName, width, height);

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(false);

        return imageView;
    }

    private Image getImage(String imageName, int width, int height) {
        String key = imageName + "_" + width + "_" + height;

        Image image = imageCache.get(key);

        if (image == null) {
            image = new Image(
                getClass().getResource("/game/gui/assets/board/" + imageName).toExternalForm(),
                width,
                height,
                true,
                true
            );

            imageCache.put(key, image);
        }

        return image;
    }

    private void setHighlighted(boolean highlighted, String glowColor) {
        highlightOverlay.setVisible(highlighted);

        if (!highlighted) {
            highlightOverlay.setOpacity(0.0);
            root.setEffect(null);
            return;
        }

        for (javafx.scene.Node node : highlightOverlay.getChildren()) {
            if (node instanceof Line) {
                Line line = (Line) node;
                line.setStroke(Color.web(glowColor));
                line.setEffect(new DropShadow(15, Color.web(glowColor)));
            }
        }

        root.setEffect(new DropShadow(18, Color.web(glowColor)));

        FadeTransition pulse = new FadeTransition(Duration.millis(650), highlightOverlay);
        pulse.setFromValue(0.55);
        pulse.setToValue(1.0);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(FadeTransition.INDEFINITE);

        highlightOverlay.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                pulse.stop();
            }
        });

        pulse.play();
    }

    private Pane createHighlightOverlay() {
        Pane overlay = new Pane();

        overlay.setPrefSize(80, 80);
        overlay.setMinSize(80, 80);
        overlay.setMaxSize(80, 80);
        overlay.setMouseTransparent(true);

        double margin = 5;
        double length = 18;

        Line topLeftH = createHighlightLine(margin, margin, margin + length, margin);
        Line topLeftV = createHighlightLine(margin, margin, margin, margin + length);

        Line topRightH = createHighlightLine(80 - margin - length, margin, 80 - margin, margin);
        Line topRightV = createHighlightLine(80 - margin, margin, 80 - margin, margin + length);

        Line bottomLeftH = createHighlightLine(margin, 80 - margin, margin + length, 80 - margin);
        Line bottomLeftV = createHighlightLine(margin, 80 - margin - length, margin, 80 - margin);

        Line bottomRightH = createHighlightLine(80 - margin - length, 80 - margin, 80 - margin, 80 - margin);
        Line bottomRightV = createHighlightLine(80 - margin, 80 - margin - length, 80 - margin, 80 - margin);

        overlay.getChildren().addAll(
            topLeftH, topLeftV,
            topRightH, topRightV,
            bottomLeftH, bottomLeftV,
            bottomRightH, bottomRightV
        );

        return overlay;
    }

    private Line createHighlightLine(double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);

        line.setStroke(Color.WHITE);
        line.setStrokeWidth(3.2);
        line.setEffect(new DropShadow(10, Color.web("#9EE6FF")));

        return line;
    }
}