package game.gui.views;

import game.engine.Board;
import game.engine.Game;
import game.engine.Constants;
import game.engine.cards.Card;
import game.engine.cells.Cell;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.Monster;
import game.gui.components.BoardCellView;
import game.gui.utils.AlertHelper;
import game.gui.utils.ImageCache;
import game.gui.utils.SoundManager;
import game.gui.utils.PixelFont;

import java.util.function.Consumer;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class BoardView {

    private static final int TOTAL_CARDS = 25;

    private BorderPane root;
    private GridPane boardGrid;

    private VBox leftPanel;
    private VBox rightPanel;
    private VBox legendPanel;
    private StackPane opponentInfoPanelNode;
    private StackPane playerInfoPanelNode;
    private VBox actionControlsNode;
    private HBox actionButtonsBoxNode;
    private VBox lastCardBoxNode;
    private HBox topButtonsNode;

    private BoardCellView[] cellViews;

    private Label statusLabel;
    private Label turnLabel;
    private VBox statusBox;

    private TextArea logArea;

    private Button rollDiceButton;
    private Button powerupButton;
    private Button returnStartButton;
    private Button viewInstructionsButton;

    private Button muteButton;
    private Button exitButton;
    private ImageView muteIconView;

    private ImageView diceImageView;
    private ImageView lastCardIconView;

    private Label lastCardNameLabel;
    private Label lastCardEffectLabel;
    private Label cardsLeftLabel;

    private boolean powerupSelected;
    private boolean muted;
    private boolean robotTurnActive;
    private boolean tutorialActive;

    private Pane tutorialOverlay;
    private VBox tutorialBox;
    private Label tutorialTitleLabel;
    private Label tutorialMessageLabel;
    private Label tutorialStepLabel;
    private Button tutorialNextButton;
    private Button tutorialSkipButton;
    private Button tutorialStartButton;
    private HBox tutorialButtonsBox;
    private Rectangle tutorialHighlight;
    private Label tutorialArrowLabel;
    private Line tutorialArrowLine;
    private Polygon tutorialArrowHead;
    private int tutorialIndex;

    private int pendingPlayerEnergyDelta = 0;
    private int pendingOpponentEnergyDelta = 0;

    private int lastAnimatedCardDrawCount = 0;

    private Game game;
    private Stage primaryStage;
    private Runnable restartAction;
    private Consumer<Monster> endGameAction;

    public BoardView(Stage primaryStage, Runnable restartAction, Consumer<Monster> endGameAction) {
        this.primaryStage = primaryStage;
        this.restartAction = restartAction;
        this.endGameAction = endGameAction;
        this.powerupSelected = false;
        this.muted = SoundManager.getInstance().isMuted();
        this.robotTurnActive = false;
        this.tutorialActive = false;

        root = new BorderPane();

        root.setStyle(
            "-fx-background-image: url('/game/gui/assets/board/board_bg.png');" +
            "-fx-background-size: cover;" +
            "-fx-background-position: center center;" +
            PixelFont.cssFontFamily()
        );

        statusLabel = new Label("Game initialized. Waiting for first turn.");
        statusLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 2;"
        );
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        PixelFont.condense(statusLabel, 0.70);

        turnLabel = new Label("Current Turn");
        turnLabel.setStyle(
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        PixelFont.condense(turnLabel, 0.78);


        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);

        rollDiceButton = createImageButton("button_roll_dice.png", 145, 50);
        powerupButton = createImageButton("button_use_powerup.png", 145, 50);

        viewInstructionsButton = createImageButton("button_view_instructions.png", 185, 44);
        returnStartButton = createImageButton("button_return_start.png", 185, 44);

        muteButton = createToggleIconButton(muted ? "button_sound_muted.png" : "button_sound_on.png", 58, 58);
        exitButton = createToggleIconButton("button_exit_circle.png", 58, 58);
        muteIconView = (ImageView) muteButton.getGraphic();

        diceImageView = new ImageView(loadControlImage("dice_1.png"));
        diceImageView.setFitWidth(90);
        diceImageView.setFitHeight(90);
        diceImageView.setPreserveRatio(true);
        diceImageView.setOpacity(0.65);
        diceImageView.setEffect(new DropShadow(14, Color.web("#B86CFF")));

        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setStyle("-fx-background-color: transparent;");

        cellViews = new BoardCellView[100];

        StackPane boardFrame = new StackPane();
        boardFrame.setAlignment(Pos.CENTER);
        boardFrame.setPadding(new Insets(10));
        boardFrame.setMinSize(824, 824);
        boardFrame.setPrefSize(824, 824);
        boardFrame.setMaxSize(824, 824);
        boardFrame.setStyle(
            "-fx-background-color: rgba(38, 8, 76, 0.34);" +
            "-fx-background-radius: 24;" +
            "-fx-border-color: rgba(190, 100, 255, 0.78);" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 24;"
        );
        boardFrame.setEffect(new DropShadow(26, Color.web("#B86CFF")));
        boardFrame.getChildren().add(boardGrid);

        VBox boardArea = new VBox();
        boardArea.setAlignment(Pos.TOP_CENTER);
        boardArea.setStyle("-fx-background-color: transparent;");
        boardArea.getChildren().add(boardFrame);

        leftPanel = new VBox(16);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPrefWidth(410);
        leftPanel.setMinWidth(410);
        leftPanel.setMaxWidth(410);
        leftPanel.setPadding(new Insets(0, 8, 8, 8));
        leftPanel.setStyle("-fx-background-color: transparent;");

        rightPanel = new VBox(16);
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setPrefWidth(390);
        rightPanel.setMinWidth(390);
        rightPanel.setMaxWidth(390);
        rightPanel.setPadding(new Insets(0, 8, 8, 8));
        rightPanel.setStyle("-fx-background-color: transparent;");

        legendPanel = createLegendPanel();

        root.setTop(createTopBar());
        root.setLeft(leftPanel);
        root.setCenter(boardArea);
        root.setRight(rightPanel);

        tutorialOverlay = createTutorialOverlay();
        tutorialOverlay.setManaged(false);
        tutorialOverlay.prefWidthProperty().bind(root.widthProperty());
        tutorialOverlay.prefHeightProperty().bind(root.heightProperty());
        root.widthProperty().addListener((obs, oldWidth, newWidth) ->
            tutorialOverlay.resizeRelocate(0, 0, newWidth.doubleValue(), root.getHeight())
        );
        root.heightProperty().addListener((obs, oldHeight, newHeight) ->
            tutorialOverlay.resizeRelocate(0, 0, root.getWidth(), newHeight.doubleValue())
        );
        root.getChildren().add(tutorialOverlay);
        Platform.runLater(() -> tutorialOverlay.resizeRelocate(0, 0, root.getWidth(), root.getHeight()));

        setupTopButtons();
        setupPowerupButton();
        setupRollButton();

        // Start gameplay background music
        SoundManager.getInstance().playBgMusic(SoundManager.GAMEPLAY_BG);
    }

    private StackPane createTopBar() {
        StackPane topBar = new StackPane();

        topBar.setMinHeight(130);
        topBar.setPrefHeight(130);
        topBar.setMaxHeight(130);
        topBar.setPadding(new Insets(0, 28, 0, 28));
        topBar.setStyle("-fx-background-color: transparent;");

        ImageView logo = new ImageView(loadControlImage("logo_doordash.png"));
        logo.setFitWidth(410);
        logo.setFitHeight(112);
        logo.setPreserveRatio(true);
        logo.setEffect(new DropShadow(16, Color.web("#B86CFF")));

        VBox logoBox = new VBox(logo);
        logoBox.setAlignment(Pos.TOP_LEFT);
        logoBox.setMinHeight(105);
        logoBox.setPrefHeight(105);
        logoBox.setTranslateY(14);

        VBox centerStatus = new VBox(3);
        centerStatus.setAlignment(Pos.CENTER);
        centerStatus.setMinWidth(650);
        centerStatus.setPrefWidth(650);
        centerStatus.setMaxWidth(650);
        centerStatus.setMinHeight(70);
        centerStatus.setPrefHeight(70);
        centerStatus.setMaxHeight(70);
        centerStatus.setPadding(new Insets(10, 20, 8, 20));
        centerStatus.setStyle(
            "-fx-background-color: rgba(35, 12, 70, 0.50);" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: rgba(190, 100, 255, 0.45);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 18;"
        );
        centerStatus.setEffect(new DropShadow(14, Color.web("#6F2DFF")));
        centerStatus.setTranslateY(18);
        statusBox = centerStatus;
        centerStatus.getChildren().addAll(statusLabel, turnLabel);

        HBox topButtons = new HBox(10);
        topButtonsNode = topButtons;
        topButtons.setAlignment(Pos.TOP_RIGHT);
        topButtons.setMinHeight(105);
        topButtons.setPrefHeight(105);
        topButtons.setPadding(new Insets(8, 0, 0, 0));
        topButtons.setTranslateY(14);

        topButtons.getChildren().addAll(
            viewInstructionsButton,
            returnStartButton,
            muteButton,
            exitButton
        );

        topBar.getChildren().addAll(logoBox, centerStatus, topButtons);
        StackPane.setAlignment(logoBox, Pos.TOP_LEFT);
        StackPane.setAlignment(centerStatus, Pos.TOP_CENTER);
        StackPane.setAlignment(topButtons, Pos.TOP_RIGHT);

        return topBar;
    }

    private void setupTopButtons() {
        returnStartButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            restartAction.run();
        });

        viewInstructionsButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            AlertHelper.showInstructions(primaryStage);
        });

        muteButton.setOnAction(e -> {
            boolean nowMuted = SoundManager.getInstance().toggleMute();
            muted = nowMuted;

            if (muted) {
                muteIconView.setImage(loadControlImage("button_sound_muted.png"));
                updateStatus("Sound muted.");
            } else {
                muteIconView.setImage(loadControlImage("button_sound_on.png"));
                updateStatus("Sound unmuted.");
            }

            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            playButtonClickAnimation(muteButton);
        });

        exitButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            primaryStage.close();
            Platform.exit();
        });
    }

    public BorderPane getRoot() {
        return root;
    }

    public void renderInitialGame(Game game) {
        this.game = game;
        powerupSelected = false;
        robotTurnActive = false;
        lastAnimatedCardDrawCount = Board.getCardDrawCount();
        updatePowerupButtonVisual();

        try {
            renderBoard(game);
            renderMonsterInfo(game);
            renderMonsterPositions(game);
            updateCurrentTurn(game);
            updateLastCardInfo(Board.getLastDrawnCard());
            Platform.runLater(() -> showTutorial());
        } catch (Exception e) {
            AlertHelper.showError(
                "Board Rendering Error",
                "Could not render the board:\n" + e.getMessage()
            );
        }
    }

    private void renderBoard(Game game) {
        boardGrid.getChildren().clear();

        Board board = game.getBoard();
        Cell[][] boardCells = board.getBoardCells();

        for (int index = 0; index < 100; index++) {
            Cell cell = getCellFromBoardArray(boardCells, index);

            BoardCellView cellView = new BoardCellView(index, cell);
            cellViews[index] = cellView;

            int visualRow = getVisualRow(index);
            int visualCol = getVisualColumn(index);

            boardGrid.add(cellView.getRoot(), visualCol, visualRow);
        }
    }

    private void renderMonsterPositions(Game game) {
        Monster player = game.getPlayer();
        Monster opponent = game.getOpponent();

        int playerPosition = player.getPosition();
        int opponentPosition = opponent.getPosition();

        if (cellViews[playerPosition] != null) {
            cellViews[playerPosition].addMonsterToken(player);
        }

        if (cellViews[opponentPosition] != null) {
            cellViews[opponentPosition].addMonsterToken(opponent);
        }
    }

    private void renderMonsterInfo(Game game) {
        leftPanel.getChildren().clear();
        rightPanel.getChildren().clear();

        StackPane opponentPanel = createMonsterInfoPanel(false, game.getOpponent());
        StackPane playerPanel = createMonsterInfoPanel(true, game.getPlayer());
        opponentInfoPanelNode = opponentPanel;
        playerInfoPanelNode = playerPanel;

        leftPanel.getChildren().addAll(opponentPanel, legendPanel);
        VBox actionControls = createActionControls();
        actionControlsNode = actionControls;
        rightPanel.getChildren().addAll(playerPanel, actionControls);

        updateLastCardInfo(Board.getLastDrawnCard());
    }

    private VBox createActionControls() {
        HBox buttonsBox = new HBox(10);
        actionButtonsBoxNode = buttonsBox;
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.getChildren().addAll(rollDiceButton, powerupButton);

        VBox actionBox = new VBox(6);
        actionControlsNode = actionBox;
        actionBox.setAlignment(Pos.TOP_CENTER);
        actionBox.getChildren().addAll(
            buttonsBox,
            diceImageView,
            createLastCardBox()
        );

        return actionBox;
    }

    private VBox createLastCardBox() {
        VBox box = new VBox(5);
        lastCardBoxNode = box;
        box.setAlignment(Pos.TOP_CENTER);
        box.setPrefWidth(350);
        box.setMinWidth(350);
        box.setMaxWidth(350);
        box.setPrefHeight(205);
        box.setMaxHeight(205);
        box.setPadding(new Insets(10, 14, 10, 14));

        box.setStyle(
            "-fx-background-color: rgba(35, 12, 70, 0.54);" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: rgba(190, 100, 255, 0.50);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 18;"
        );

        Label title = new Label("LAST CARD DRAWN");
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setMinWidth(320);
        title.setPrefWidth(320);
        title.setMaxWidth(320);
        title.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #CFF7FF;"
        );
        title.setEffect(new DropShadow(13, Color.web("#B86CFF")));
        PixelFont.condense(title, 0.74);

        lastCardIconView = new ImageView(loadControlImage("card_icon.png"));
        lastCardIconView.setFitWidth(62);
        lastCardIconView.setFitHeight(62);
        lastCardIconView.setPreserveRatio(true);
        lastCardIconView.setEffect(new DropShadow(12, Color.web("#B86CFF")));

        lastCardNameLabel = new Label("No card drawn yet");
        lastCardNameLabel.setAlignment(Pos.CENTER);
        lastCardNameLabel.setTextAlignment(TextAlignment.CENTER);
        lastCardNameLabel.setWrapText(true);
        lastCardNameLabel.setMaxWidth(315);
        lastCardNameLabel.setStyle(
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        PixelFont.condense(lastCardNameLabel, 0.72);

        lastCardEffectLabel = new Label("Effect: ---");
        lastCardEffectLabel.setAlignment(Pos.TOP_CENTER);
        lastCardEffectLabel.setTextAlignment(TextAlignment.CENTER);
        lastCardEffectLabel.setWrapText(true);
        lastCardEffectLabel.setMaxWidth(315);
        lastCardEffectLabel.setMaxHeight(42);
        lastCardEffectLabel.setStyle(
            "-fx-font-size: 9px;" +
            "-fx-text-fill: #9EE6FF;" +
            "-fx-line-spacing: 1px;"
        );
        PixelFont.condenseWrapped(lastCardEffectLabel, 0.78, 295, 1);

        cardsLeftLabel = new Label("Cards left: " + getCardsLeft());
        cardsLeftLabel.setAlignment(Pos.CENTER);
        cardsLeftLabel.setTextAlignment(TextAlignment.CENTER);
        cardsLeftLabel.setMinWidth(315);
        cardsLeftLabel.setPrefWidth(315);
        cardsLeftLabel.setStyle(
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #FFE9A8;" +
            "-fx-padding: 0;"
        );
        PixelFont.condense(cardsLeftLabel, 0.76);

        box.getChildren().addAll(
            title,
            lastCardIconView,
            lastCardNameLabel,
            lastCardEffectLabel,
            cardsLeftLabel
        );

        updateLastCardInfo(Board.getLastDrawnCard());

        return box;
    }

    private int getCardsLeft() {
        int left = TOTAL_CARDS - Board.getCardDrawCount();

        if (left < 0) {
            return 0;
        }

        return left;
    }

    private void updateLastCardInfo(Card card) {
        if (lastCardNameLabel == null || lastCardEffectLabel == null || cardsLeftLabel == null) {
            return;
        }

        int currentDrawCount = Board.getCardDrawCount();

        if (card == null) {
            lastCardNameLabel.setText("No card drawn yet");
            lastCardEffectLabel.setText("Effect: ---");
        } else {
            lastCardNameLabel.setText(card.getName());
            lastCardEffectLabel.setText("Effect: " + card.getDescription());

            if (currentDrawCount > lastAnimatedCardDrawCount) {
                playCardPopAnimation();
                lastAnimatedCardDrawCount = currentDrawCount;
            }
        }

        cardsLeftLabel.setText("Cards left: " + getCardsLeft());
    }

    private void refreshBoard(Game game) {
        renderBoard(game);
        renderMonsterPositions(game);
    }

    private StackPane createMonsterInfoPanel(boolean isPlayer, Monster monster) {
        StackPane panel = new StackPane();

        panel.setPrefSize(360, 430);
        panel.setMinSize(360, 430);
        panel.setMaxSize(360, 430);
        panel.setStyle("-fx-background-color: transparent;");

        boolean isCurrentTurn = game != null && game.getCurrent() == monster;

        ImageView background = new ImageView(loadPanelImage(
            isPlayer ? "player_info_box.png" : "opponent_info_box.png"
        ));

        background.setFitWidth(360);
        background.setFitHeight(430);
        background.setPreserveRatio(false);

        if (isCurrentTurn) {
            playPanelPulse(background);
        } else {
            background.setEffect(new DropShadow(12, Color.web("#4F1BA8")));
        }

        ImageView profile = new ImageView(loadBoardImage(getMonsterTokenFileName(monster.getName())));
        profile.setFitWidth(82);
        profile.setFitHeight(82);
        profile.setPreserveRatio(true);

        if (isCurrentTurn) {
            profile.setEffect(new DropShadow(22, Color.web("#FFF36D")));
        } else {
            profile.setEffect(new DropShadow(8, Color.web("#00E5FF")));
        }

        Label nameLabel = createMainInfoLabel(monster.getName());

        VBox infoRows = new VBox(2);
        infoRows.setAlignment(Pos.CENTER);
        infoRows.setMaxWidth(220);
        infoRows.setTranslateX(5);

        infoRows.getChildren().addAll(
            createSmallInfoLabel("Type  " + monster.getClass().getSimpleName()),
            createSmallInfoLabel("Original Role  " + monster.getOriginalRole()),
            createCurrentRoleInfoLabel("Current Role  " + monster.getRole(), monster.isConfused()),
            createEnergyInfoLabel("Energy  " + monster.getEnergy(), monster.getEnergy()),
            createSmallInfoLabel("Position  " + monster.getPosition())
        );

        HBox statusIcons = new HBox(10);
        statusIcons.setAlignment(Pos.CENTER);
        statusIcons.setMinHeight(34);
        statusIcons.setPrefHeight(34);
        statusIcons.setTranslateY(-8);
        addStatusIcons(monster, statusIcons);

        VBox spacer = new VBox();
        spacer.setMinHeight(4);
        spacer.setPrefHeight(6);
        spacer.setMaxHeight(8);

        VBox content = new VBox(4);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPrefSize(252, 392);
        content.setMaxSize(252, 392);
        content.setPadding(new Insets(66, 0, 12, 0));

        content.getChildren().addAll(profile, nameLabel, infoRows, spacer, statusIcons);

        panel.getChildren().addAll(background, content);

        int energyDelta = getPendingEnergyDelta(monster);
        if (energyDelta != 0) {
            Label energyPopup = createEnergyDeltaPopup(energyDelta);
            StackPane.setAlignment(energyPopup, Pos.TOP_CENTER);
            energyPopup.setTranslateX(58);
            energyPopup.setTranslateY(280);
            panel.getChildren().add(energyPopup);
            playEnergyDeltaAnimation(energyPopup);
        }

        return panel;
    }

    private Label createMainInfoLabel(String text) {
        Label label = new Label(text);

        label.setWrapText(false);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.CENTER);

        label.setMinWidth(240);
        label.setPrefWidth(240);
        label.setMaxWidth(240);

        label.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 3 0 3 0;"
        );

        label.setEffect(new DropShadow(8, Color.web("#C16CFF")));
        PixelFont.condense(label, 0.70);

        return label;
    }

    private Label createSmallInfoLabel(String text) {
        Label label = new Label(text);

        label.setWrapText(false);
        label.setTextAlignment(TextAlignment.LEFT);
        label.setAlignment(Pos.CENTER_LEFT);

        label.setMinWidth(220);
        label.setPrefWidth(220);
        label.setMaxWidth(220);

        label.setStyle(
            "-fx-font-size: 9px;" +
            "-fx-text-fill: #F8EEFF;" +
            "-fx-padding: 2 4 2 8;" +
            "-fx-border-color: transparent transparent rgba(170, 95, 255, 0.45) transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        label.setEffect(new DropShadow(5, Color.web("#000000")));
        PixelFont.condense(label, 0.66);

        return label;
    }

    private Label createCurrentRoleInfoLabel(String text, boolean confused) {
        Label label = createSmallInfoLabel(text);

        if (confused) {
            label.setStyle(
                "-fx-font-size: 9px;" +
                "-fx-text-fill: #FF77D9;" +
                "-fx-padding: 2 4 2 8;" +
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent transparent rgba(170, 95, 255, 0.45) transparent;" +
                "-fx-border-width: 0 0 1 0;"
            );

            label.setEffect(new DropShadow(14, Color.web("#FF4FD8")));
        }

        return label;
    }

    private Label createEnergyInfoLabel(String text, int energy) {
        Label label = createSmallInfoLabel(text);

        if (energy > 500) {
            label.setStyle(
                "-fx-font-size: 9px;" +
                "-fx-text-fill: #7FEAFF;" +
                "-fx-padding: 2 4 2 8;" +
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent transparent rgba(170, 95, 255, 0.45) transparent;" +
                "-fx-border-width: 0 0 1 0;"
            );

            label.setEffect(new DropShadow(14, Color.web("#00E5FF")));
        }

        return label;
    }

    private VBox createLegendPanel() {
        VBox legend = new VBox(12);
        legend.setAlignment(Pos.TOP_CENTER);
        legend.setPrefWidth(390);
        legend.setMinWidth(390);
        legend.setMaxWidth(390);
        legend.setPrefHeight(390);
        legend.setPadding(new Insets(12, 10, 14, 10));
        legend.setTranslateX(18);

        legend.setStyle(
            "-fx-background-color: rgba(35, 12, 70, 0.46);" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: rgba(190, 100, 255, 0.42);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 18;"
        );

        Label title = new Label("BOARD LEGEND");
        title.setMinWidth(350);
        title.setPrefWidth(350);
        title.setMaxWidth(350);
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setStyle(
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #CFF7FF;" +
            "-fx-padding: 0 0 4 0;"
        );
        title.setEffect(new DropShadow(14, Color.web("#B86CFF")));
        PixelFont.condense(title, 0.72);

        HBox columns = new HBox(18);
        columns.setAlignment(Pos.TOP_CENTER);
        columns.setMinWidth(350);
        columns.setPrefWidth(350);
        columns.setMaxWidth(350);
        columns.setTranslateX(10);

        VBox leftColumn = new VBox(12);
        leftColumn.setAlignment(Pos.TOP_LEFT);
        leftColumn.setPrefWidth(166);
        leftColumn.setMinWidth(166);
        leftColumn.setMaxWidth(166);

        VBox rightColumn = new VBox(12);
        rightColumn.setAlignment(Pos.TOP_LEFT);
        rightColumn.setPrefWidth(166);
        rightColumn.setMinWidth(166);
        rightColumn.setMaxWidth(166);

        leftColumn.getChildren().addAll(
            createLegendRow("normal_cell.png", "Normal", "safe cell"),
            createLegendRow("card_cell.png", "Card", "draw card"),
            createLegendRow("conveyor_cell.png", "Conveyor", "move ahead"),
            createLegendRow("sock_cell.png", "Sock", "contamination")
        );

        rightColumn.getChildren().addAll(
            createLegendRow("monster_cell.png", "Monster", "stationed monster"),
            createLegendRow("scarer_door_cell.png", "Scare Door", "scarer energy"),
            createLegendRow("laugher_door_cell.png", "Laugh Door", "laugher energy"),
            createLegendRow("activated_door_cell.png", "Used Door", "already used")
        );

        columns.getChildren().addAll(leftColumn, rightColumn);

        HBox statusRow = new HBox(26);
        statusRow.setAlignment(Pos.CENTER);
        statusRow.setMinWidth(350);
        statusRow.setPrefWidth(350);
        statusRow.setMaxWidth(350);
        statusRow.setPadding(new Insets(8, 0, 0, 0));
        statusRow.getChildren().addAll(
            createStatusLegendIcon("shield_icon.png", "Shield"),
            createStatusLegendIcon("frozen_icon.png", "Frozen"),
            createStatusLegendIcon("confused_icon.png", "Confused")
        );

        legend.getChildren().addAll(title, columns, statusRow);

        return legend;
    }

    private HBox createLegendRow(String imageName, String title, String description) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefWidth(166);
        row.setMinWidth(166);
        row.setMaxWidth(166);

        ImageView icon = new ImageView(loadBoardImage(imageName));
        icon.setFitWidth(46);
        icon.setFitHeight(46);
        icon.setPreserveRatio(true);
        icon.setEffect(new DropShadow(7, Color.web("#7FEAFF")));

        VBox textBox = new VBox(1);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 9px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        PixelFont.condense(titleLabel, 0.76);

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(false);
        descriptionLabel.setStyle(
            "-fx-font-size: 8px;" +
            "-fx-text-fill: #9EE6FF;"
        );
        PixelFont.condense(descriptionLabel, 0.66);

        textBox.getChildren().addAll(titleLabel, descriptionLabel);
        row.getChildren().addAll(icon, textBox);

        return row;
    }

    private VBox createStatusLegendIcon(String iconName, String labelText) {
        VBox item = new VBox(4);
        item.setAlignment(Pos.CENTER);
        item.setPrefWidth(82);
        item.setMinWidth(82);
        item.setMaxWidth(82);

        ImageView icon = new ImageView(loadStatusIcon(iconName));
        icon.setFitWidth(40);
        icon.setFitHeight(40);
        icon.setPreserveRatio(true);
        icon.setEffect(new DropShadow(8, Color.web("#B86CFF")));

        Label label = new Label(labelText);
        label.setAlignment(Pos.CENTER);
        label.setStyle(
            "-fx-font-size: 8px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        PixelFont.condense(label, 0.74);

        item.getChildren().addAll(icon, label);
        return item;
    }

    private void addStatusIcons(Monster monster, HBox statusIcons) {
        statusIcons.getChildren().clear();

        if (monster.isShielded()) {
            statusIcons.getChildren().add(createStatusIconView("shield_icon.png"));
        }

        if (monster.isFrozen()) {
            statusIcons.getChildren().add(createStatusIconView("frozen_icon.png"));
        }

        if (monster.isConfused()) {
            statusIcons.getChildren().add(createStatusIconView("confused_icon.png"));
        }
    }

    private ImageView createStatusIconView(String fileName) {
        ImageView icon = new ImageView(loadStatusIcon(fileName));

        icon.setFitWidth(34);
        icon.setFitHeight(34);
        icon.setPreserveRatio(true);
        icon.setEffect(new DropShadow(10, Color.web("#E6D6FF")));

        return icon;
    }

    private Button createImageButton(String imageName, int width, int height) {
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
            if (button != powerupButton || !powerupSelected) {
                button.setEffect(null);
            }

            playButtonHoverAnimation(button, false);
        });

        return button;
    }

    private Button createToggleIconButton(String imageName, int width, int height) {
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


    private void updateStatus(String text) {
        if (statusLabel == null) {
            return;
        }

        String newText = text == null ? "" : text;
        if (newText.equals(statusLabel.getText())) {
            return;
        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(90), statusLabel);
        fadeOut.setFromValue(statusLabel.getOpacity());
        fadeOut.setToValue(0.35);

        fadeOut.setOnFinished(e -> {
            statusLabel.setText(newText);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), statusLabel);
            fadeIn.setFromValue(0.35);
            fadeIn.setToValue(1.0);

            ScaleTransition pop = new ScaleTransition(Duration.millis(150), statusLabel);
            pop.setFromX(0.97);
            pop.setFromY(0.97);
            pop.setToX(1.03);
            pop.setToY(1.03);
            pop.setAutoReverse(true);
            pop.setCycleCount(2);

            ParallelTransition animation = new ParallelTransition(fadeIn, pop);
            animation.setOnFinished(done -> {
                statusLabel.setScaleX(1.0);
                statusLabel.setScaleY(1.0);
                statusLabel.setOpacity(1.0);
            });
            animation.play();

            if (statusBox != null) {
                DropShadow glow = new DropShadow(18, Color.web("#B86CFF"));
                statusBox.setEffect(glow);

                Timeline pulse = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 14)),
                    new KeyFrame(Duration.millis(180), new KeyValue(glow.radiusProperty(), 26)),
                    new KeyFrame(Duration.millis(360), new KeyValue(glow.radiusProperty(), 14))
                );
                pulse.play();
            }
        });

        fadeOut.play();
    }

    private int getPendingEnergyDelta(Monster monster) {
        if (game == null || monster == null) {
            return 0;
        }

        if (monster == game.getPlayer()) {
            return pendingPlayerEnergyDelta;
        }

        if (monster == game.getOpponent()) {
            return pendingOpponentEnergyDelta;
        }

        return 0;
    }

    private Label createEnergyDeltaPopup(int delta) {
        String text = delta > 0 ? "+" + delta : String.valueOf(delta);

        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setMinWidth(120);
        label.setPrefWidth(120);
        label.setMaxWidth(120);

        String textColor = delta > 0 ? "#7FEAFF" : "#FF77D9";
        String glowColor = delta > 0 ? "#00E5FF" : "#FF4FD8";

        label.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-padding: 4 8 4 8;" +
            "-fx-background-color: rgba(20, 8, 48, 0.45);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(255, 255, 255, 0.18);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;"
        );

        label.setEffect(new DropShadow(22, Color.web(glowColor)));
        return label;
    }

    private void playEnergyDeltaAnimation(Label label) {
        label.setOpacity(0.0);
        label.setScaleX(0.75);
        label.setScaleY(0.75);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(140), label);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ScaleTransition pop = new ScaleTransition(Duration.millis(180), label);
        pop.setFromX(0.75);
        pop.setFromY(0.75);
        pop.setToX(1.15);
        pop.setToY(1.15);
        pop.setAutoReverse(true);
        pop.setCycleCount(2);

        TranslateTransition floatUp = new TranslateTransition(Duration.millis(850), label);
        floatUp.setByY(-42);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(850), label);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.millis(260));

        ParallelTransition animation = new ParallelTransition(fadeIn, pop, floatUp, fadeOut);
        animation.setOnFinished(e -> {
            if (label.getParent() instanceof StackPane) {
                ((StackPane) label.getParent()).getChildren().remove(label);
            }
        });
        animation.play();
    }

    private void playButtonHoverAnimation(Button button, boolean hover) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(120), button);
        scale.setToX(hover ? 1.05 : 1.0);
        scale.setToY(hover ? 1.05 : 1.0);
        scale.play();
    }

    private void playButtonClickAnimation(Button button) {
        ScaleTransition click = new ScaleTransition(Duration.millis(110), button);
        click.setFromX(1.0);
        click.setFromY(1.0);
        click.setToX(0.90);
        click.setToY(0.90);
        click.setAutoReverse(true);
        click.setCycleCount(2);
        click.play();
    }

    private void playDiceAnimation() {
        if (diceImageView == null) {
            return;
        }

        ScaleTransition pop = new ScaleTransition(Duration.millis(160), diceImageView);
        pop.setFromX(1.0);
        pop.setFromY(1.0);
        pop.setToX(1.18);
        pop.setToY(1.18);
        pop.setAutoReverse(true);
        pop.setCycleCount(2);
        pop.play();
    }

    private void playCardPopAnimation() {
        if (lastCardIconView == null) {
            return;
        }

        ScaleTransition pop = new ScaleTransition(Duration.millis(170), lastCardIconView);
        pop.setFromX(1.0);
        pop.setFromY(1.0);
        pop.setToX(1.22);
        pop.setToY(1.22);
        pop.setAutoReverse(true);
        pop.setCycleCount(2);
        pop.play();

        lastCardIconView.setEffect(new DropShadow(22, Color.web("#FF5DC8")));
    }

    private void playPanelPulse(ImageView background) {
        DropShadow glow = new DropShadow(24, Color.web("#FFF36D"));
        background.setEffect(glow);

        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 18)),
            new KeyFrame(Duration.millis(700), new KeyValue(glow.radiusProperty(), 34))
        );

        pulse.setAutoReverse(true);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();
    }

    private void refreshBoardWithMovingMonster(Monster movingMonster, int movingPosition) {
        if (game == null || movingMonster == null) {
            return;
        }

        renderBoard(game);

        Monster player = game.getPlayer();
        Monster opponent = game.getOpponent();

        int playerVisualPosition = player == movingMonster ? movingPosition : player.getPosition();
        int opponentVisualPosition = opponent == movingMonster ? movingPosition : opponent.getPosition();

        addMonsterTokenAtPosition(player, playerVisualPosition);
        addMonsterTokenAtPosition(opponent, opponentVisualPosition);
    }

    private void addMonsterTokenAtPosition(Monster monster, int position) {
        if (monster == null || position < 0 || position >= cellViews.length) {
            return;
        }

        if (cellViews[position] != null) {
            cellViews[position].addMonsterToken(monster);
        }
    }

    private int normalizeBoardPosition(int position) {
        int normalized = position % 100;

        if (normalized < 0) {
            normalized += 100;
        }

        return normalized;
    }

    private int getDiceLandingPosition(int oldPosition, int roll) {
        if (roll <= 0) {
            return normalizeBoardPosition(oldPosition);
        }

        return normalizeBoardPosition(oldPosition + roll);
    }

    private java.util.List<Integer> buildDicePath(int oldPosition, int roll) {
        java.util.List<Integer> path = new java.util.ArrayList<Integer>();

        if (roll <= 0) {
            return path;
        }

        for (int i = 1; i <= roll; i++) {
            path.add(normalizeBoardPosition(oldPosition + i));
        }

        return path;
    }

    private void refreshBoardWithMovingMonster(Monster movingMonster, int movingPosition,
                                               Monster otherMonster, int otherVisualPosition) {
        if (game == null || movingMonster == null) {
            return;
        }

        renderBoard(game);

        Monster player = game.getPlayer();
        Monster opponent = game.getOpponent();

        int playerVisualPosition;
        int opponentVisualPosition;

        if (player == movingMonster) {
            playerVisualPosition = normalizeBoardPosition(movingPosition);
            opponentVisualPosition = normalizeBoardPosition(otherVisualPosition);
        } else {
            playerVisualPosition = normalizeBoardPosition(otherVisualPosition);
            opponentVisualPosition = normalizeBoardPosition(movingPosition);
        }

        addMonsterTokenAtPosition(player, playerVisualPosition);
        addMonsterTokenAtPosition(opponent, opponentVisualPosition);
    }

    private void playMonsterMoveAnimation(Monster movingMonster,
                                          Monster otherMonster,
                                          int oldPosition,
                                          int otherOldPosition,
                                          int roll,
                                          int finalPosition,
                                          boolean wasFrozen,
                                          Runnable afterDiceLanding,
                                          Runnable afterMoveRefresh) {
        if (movingMonster == null || oldPosition < 0 || oldPosition >= cellViews.length ||
            finalPosition < 0 || finalPosition >= cellViews.length || wasFrozen || roll <= 0) {
            if (afterDiceLanding != null) {
                afterDiceLanding.run();
            }
            afterMoveRefresh.run();
            return;
        }

        java.util.List<Integer> dicePath = buildDicePath(oldPosition, roll);
        int diceLandingPosition = getDiceLandingPosition(oldPosition, roll);

        if (dicePath.isEmpty()) {
            if (afterDiceLanding != null) {
                afterDiceLanding.run();
            }
            afterMoveRefresh.run();
            return;
        }

        animateDicePathStep(
            movingMonster,
            otherMonster,
            otherOldPosition,
            oldPosition,
            dicePath,
            0,
            () -> {
                playMonsterArriveAnimation(diceLandingPosition);

                PauseTransition landingPause = new PauseTransition(Duration.millis(120));
                landingPause.setOnFinished(e -> {
                    if (afterDiceLanding != null) {
                        afterDiceLanding.run();
                    }

                    if (normalizeBoardPosition(finalPosition) != normalizeBoardPosition(diceLandingPosition)) {
                        playSpecialTeleportAnimation(
                            movingMonster,
                            otherMonster,
                            otherOldPosition,
                            diceLandingPosition,
                            finalPosition,
                            afterMoveRefresh
                        );
                    } else {
                        afterMoveRefresh.run();
                    }
                });
                landingPause.play();
            }
        );
    }

    private void animateDicePathStep(Monster movingMonster,
                                     Monster otherMonster,
                                     int otherOldPosition,
                                     int previousPosition,
                                     java.util.List<Integer> path,
                                     int index,
                                     Runnable finished) {
        if (index >= path.size()) {
            finished.run();
            return;
        }

        int currentPosition = path.get(index);
        boolean wrappedFromEndToStart = previousPosition == 99 && currentPosition == 0;

        if (wrappedFromEndToStart) {
            playWrapTeleportAnimation(
                movingMonster,
                otherMonster,
                otherOldPosition,
                previousPosition,
                currentPosition,
                () -> animateDicePathStep(
                    movingMonster,
                    otherMonster,
                    otherOldPosition,
                    currentPosition,
                    path,
                    index + 1,
                    finished
                )
            );
            return;
        }

        PauseTransition stepPause = new PauseTransition(Duration.millis(62));
        stepPause.setOnFinished(e -> {
            refreshBoardWithMovingMonster(movingMonster, currentPosition, otherMonster, otherOldPosition);
            playMonsterHopOnCell(currentPosition);

            PauseTransition nextPause = new PauseTransition(Duration.millis(42));
            nextPause.setOnFinished(done -> animateDicePathStep(
                movingMonster,
                otherMonster,
                otherOldPosition,
                currentPosition,
                path,
                index + 1,
                finished
            ));
            nextPause.play();
        });
        stepPause.play();
    }

    private void playMonsterHopOnCell(int position) {
        if (position < 0 || position >= cellViews.length || cellViews[position] == null) {
            return;
        }

        StackPane cellRoot = cellViews[position].getRoot();
        cellRoot.setScaleX(0.96);
        cellRoot.setScaleY(0.96);

        ScaleTransition hop = new ScaleTransition(Duration.millis(115), cellRoot);
        hop.setFromX(0.96);
        hop.setFromY(0.96);
        hop.setToX(1.06);
        hop.setToY(1.06);
        hop.setAutoReverse(true);
        hop.setCycleCount(2);
        hop.setOnFinished(e -> {
            cellRoot.setScaleX(1.0);
            cellRoot.setScaleY(1.0);
        });
        hop.play();
    }

    private void playWrapTeleportAnimation(Monster movingMonster,
                                           Monster otherMonster,
                                           int otherOldPosition,
                                           int fromPosition,
                                           int toPosition,
                                           Runnable finished) {
        refreshBoardWithMovingMonster(movingMonster, fromPosition, otherMonster, otherOldPosition);
        updateStatus(movingMonster.getName() + " reached the final door and teleported back to cell 0!");

        StackPane fromCell = cellViews[fromPosition] == null ? null : cellViews[fromPosition].getRoot();

        if (fromCell == null) {
            refreshBoardWithMovingMonster(movingMonster, toPosition, otherMonster, otherOldPosition);
            playMonsterArriveAnimation(toPosition);
            finished.run();
            return;
        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), fromCell);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.38);

        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), fromCell);
        shrink.setFromX(1.0);
        shrink.setFromY(1.0);
        shrink.setToX(0.74);
        shrink.setToY(0.74);

        ParallelTransition vanish = new ParallelTransition(fadeOut, shrink);
        vanish.setOnFinished(e -> {
            fromCell.setOpacity(1.0);
            fromCell.setScaleX(1.0);
            fromCell.setScaleY(1.0);

            refreshBoardWithMovingMonster(movingMonster, toPosition, otherMonster, otherOldPosition);
            playMonsterArriveAnimation(toPosition);

            PauseTransition pause = new PauseTransition(Duration.millis(120));
            pause.setOnFinished(done -> finished.run());
            pause.play();
        });
        vanish.play();
    }

    private void playSpecialTeleportAnimation(Monster movingMonster,
                                              Monster otherMonster,
                                              int otherOldPosition,
                                              int fromPosition,
                                              int toPosition,
                                              Runnable afterMoveRefresh) {
        refreshBoardWithMovingMonster(movingMonster, fromPosition, otherMonster, otherOldPosition);
        updateStatus("Cell effect activated! " + movingMonster.getName() + " teleported to cell " + toPosition + ".");

        StackPane fromCell = cellViews[fromPosition] == null ? null : cellViews[fromPosition].getRoot();

        if (fromCell == null) {
            refreshBoardWithMovingMonster(movingMonster, toPosition, otherMonster, otherOldPosition);
            playMonsterArriveAnimation(toPosition);
            afterMoveRefresh.run();
            return;
        }

        DropShadow teleportGlow = new DropShadow(30, Color.web("#FF5DC8"));
        fromCell.setEffect(teleportGlow);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(170), fromCell);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.25);

        ScaleTransition shrink = new ScaleTransition(Duration.millis(170), fromCell);
        shrink.setFromX(1.0);
        shrink.setFromY(1.0);
        shrink.setToX(0.70);
        shrink.setToY(0.70);

        ParallelTransition disappear = new ParallelTransition(fadeOut, shrink);
        disappear.setOnFinished(e -> {
            fromCell.setOpacity(1.0);
            fromCell.setScaleX(1.0);
            fromCell.setScaleY(1.0);
            fromCell.setEffect(null);

            refreshBoardWithMovingMonster(movingMonster, toPosition, otherMonster, otherOldPosition);
            playMonsterArriveAnimation(toPosition);

            PauseTransition settle = new PauseTransition(Duration.millis(230));
            settle.setOnFinished(done -> afterMoveRefresh.run());
            settle.play();
        });
        disappear.play();
    }

    private void playMonsterArriveAnimation(int position) {
        if (position < 0 || position >= cellViews.length || cellViews[position] == null) {
            return;
        }

        StackPane newCellRoot = cellViews[position].getRoot();

        newCellRoot.setScaleX(0.82);
        newCellRoot.setScaleY(0.82);
        newCellRoot.setOpacity(0.65);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), newCellRoot);
        fadeIn.setFromValue(0.65);
        fadeIn.setToValue(1.0);

        ScaleTransition popIn = new ScaleTransition(Duration.millis(220), newCellRoot);
        popIn.setFromX(0.82);
        popIn.setFromY(0.82);
        popIn.setToX(1.08);
        popIn.setToY(1.08);
        popIn.setAutoReverse(true);
        popIn.setCycleCount(2);

        ParallelTransition arriveAnimation = new ParallelTransition(fadeIn, popIn);

        arriveAnimation.setOnFinished(e -> {
            newCellRoot.setOpacity(1.0);
            newCellRoot.setScaleX(1.0);
            newCellRoot.setScaleY(1.0);
        });

        arriveAnimation.play();
    }

    private Pane createTutorialOverlay() {
        Pane overlay = new Pane();
        overlay.setVisible(false);
        overlay.setStyle("-fx-background-color: rgba(12, 4, 34, 0.24);");
        overlay.setPickOnBounds(true);

        tutorialHighlight = new Rectangle();
        tutorialHighlight.setManaged(false);
        tutorialHighlight.setFill(Color.web("#11062E", 0.14));
        tutorialHighlight.setArcWidth(18);
        tutorialHighlight.setArcHeight(18);
        tutorialHighlight.setStroke(Color.web("#9EE6FF"));
        tutorialHighlight.setStrokeWidth(3.0);
        tutorialHighlight.setMouseTransparent(true);
        tutorialHighlight.setEffect(new DropShadow(22, Color.web("#00E5FF")));

        tutorialArrowLine = new Line();
        tutorialArrowLine.setManaged(false);
        tutorialArrowLine.setMouseTransparent(true);
        tutorialArrowLine.setStroke(Color.web("#CFF7FF"));
        tutorialArrowLine.setStrokeWidth(4.0);
        tutorialArrowLine.setEffect(new DropShadow(22, Color.web("#00E5FF")));

        tutorialArrowHead = new Polygon();
        tutorialArrowHead.setManaged(false);
        tutorialArrowHead.setMouseTransparent(true);
        tutorialArrowHead.setFill(Color.web("#CFF7FF"));
        tutorialArrowHead.setStroke(Color.web("#00E5FF"));
        tutorialArrowHead.setStrokeWidth(1.5);
        tutorialArrowHead.setEffect(new DropShadow(18, Color.web("#00E5FF")));

        tutorialArrowLabel = new Label("✦");
        tutorialArrowLabel.setManaged(false);
        tutorialArrowLabel.setMouseTransparent(true);
        tutorialArrowLabel.setStyle(
            "-fx-font-size: 34px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #FF5DC8;"
        );
        tutorialArrowLabel.setEffect(new DropShadow(20, Color.web("#FF5DC8")));

        tutorialBox = new VBox(8);
        tutorialBox.setManaged(false);
        tutorialBox.setAlignment(Pos.CENTER);
        tutorialBox.setPrefWidth(560);
        tutorialBox.setMaxWidth(560);
        tutorialBox.setPadding(new Insets(18, 24, 18, 24));
        tutorialBox.setStyle(
            "-fx-background-color: rgba(29, 9, 67, 0.74);" +
            "-fx-background-radius: 22;" +
            "-fx-border-color: rgba(190, 100, 255, 0.92);" +
            "-fx-border-width: 2.4;" +
            "-fx-border-radius: 22;"
        );
        tutorialBox.setEffect(new DropShadow(28, Color.web("#B86CFF")));

        tutorialStepLabel = new Label("Tutorial");
        tutorialStepLabel.setAlignment(Pos.CENTER);
        tutorialStepLabel.setTextAlignment(TextAlignment.CENTER);
        tutorialStepLabel.setStyle(
            "-fx-font-size: 9px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #9EE6FF;"
        );
        PixelFont.condense(tutorialStepLabel, 0.78);

        tutorialTitleLabel = new Label();
        tutorialTitleLabel.setAlignment(Pos.CENTER);
        tutorialTitleLabel.setTextAlignment(TextAlignment.CENTER);
        tutorialTitleLabel.setWrapText(true);
        tutorialTitleLabel.setMaxWidth(510);
        tutorialTitleLabel.setStyle(
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #FFFFFF;"
        );
        tutorialTitleLabel.setEffect(new DropShadow(12, Color.web("#FF5DC8")));
        PixelFont.condense(tutorialTitleLabel, 0.72);

        tutorialMessageLabel = new Label();
        tutorialMessageLabel.setAlignment(Pos.CENTER);
        tutorialMessageLabel.setTextAlignment(TextAlignment.CENTER);
        tutorialMessageLabel.setWrapText(true);
        tutorialMessageLabel.setMaxWidth(510);
        tutorialMessageLabel.setStyle(
            "-fx-font-size: 10px;" +
            "-fx-text-fill: #F8EEFF;" +
            "-fx-line-spacing: 4px;"
        );
        PixelFont.condenseWrapped(tutorialMessageLabel, 0.78, 490, 4);

        tutorialNextButton = createImageButton("button_tutorial_next.png", 180, 62);
        tutorialSkipButton = createImageButton("button_tutorial_skip.png", 240, 62);
        tutorialStartButton = createImageButton("button_tutorial_start_game.png", 240, 62);

        tutorialNextButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            tutorialIndex++;
            updateTutorialStep();
        });

        tutorialSkipButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            closeTutorial();
        });

        tutorialStartButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            closeTutorial();
        });

        tutorialButtonsBox = new HBox(18);
        tutorialButtonsBox.setManaged(false);
        tutorialButtonsBox.setAlignment(Pos.CENTER);
        tutorialButtonsBox.setPadding(new Insets(0));
        tutorialButtonsBox.setPickOnBounds(false);
        tutorialButtonsBox.getChildren().addAll(tutorialSkipButton, tutorialNextButton, tutorialStartButton);

        tutorialBox.getChildren().addAll(tutorialStepLabel, tutorialTitleLabel, tutorialMessageLabel);

        // Arrows were removed: the tutorial now uses only accurate glowing rectangles.
        tutorialArrowLine.setVisible(false);
        tutorialArrowHead.setVisible(false);
        tutorialArrowLabel.setVisible(false);

        overlay.getChildren().addAll(
            tutorialHighlight,
            tutorialBox,
            tutorialButtonsBox
        );

        return overlay;
    }

    private void showTutorial() {
        if (tutorialOverlay == null || game == null) {
            return;
        }

        tutorialIndex = 0;
        tutorialActive = true;
        tutorialOverlay.setVisible(true);
        tutorialOverlay.toFront();
        setTurnControlsDisabled(true);
        updateTutorialStep();

        tutorialOverlay.setOpacity(0.0);
        FadeTransition fade = new FadeTransition(Duration.millis(260), tutorialOverlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    private void updateTutorialStep() {
        String[][] steps = {
            {
                "Welcome to DoorDasH!",
                "You control the PLAYER monster. The OPPONENT is controlled by a robot, so you only roll and choose powerups for yourself. After your turn, the robot waits briefly, then plays automatically while you watch the effects."
            },
            {
                "Your Monster Panel",
                "This is your monster information panel. It shows your monster name, type, original role, current role, energy, position, and active status icons. Watch your energy carefully: reaching cell 99 only wins if your energy is more than 1000."
            },
            {
                "Robot Opponent Panel",
                "This is the robot opponent. It takes its own turns without you clicking Roll Dice for it. If it has enough energy, it may randomly activate a powerup before rolling."
            },
            {
                "Roll and Powerup Buttons",
                "Use ROLL DICE on your turn to move your monster. You may press USE POWERUP before rolling if your monster has enough energy. These buttons are disabled during the robot's turn so you can follow what happens clearly."
            },
            {
                "Dice and Cards",
                "The dice shows the latest roll. The Last Card Drawn box shows the newest card name, its effect, and how many cards are left. If the robot draws a card, the popup still appears so you can see the effect."
            },
            {
                "Board Legend",
                "The legend explains the board cells and status icons. Door cells affect energy, card cells draw cards, conveyor cells push monsters forward, sock cells may contaminate, and glowing bouncing tokens show current monster positions."
            },
            {
                "Status Bar",
                "The top status bar tells you what is happening right now: whose turn it is, what was rolled, whether a powerup was used, and important game messages."
            },
            {
                "Start the Race!",
                "Goal: reach cell 99 with more than 1000 energy before the robot opponent. Use doors, cards, movement, and powerups wisely. Press START GAME when you are ready."
            }
        };

        if (tutorialIndex < 0) {
            tutorialIndex = 0;
        }

        if (tutorialIndex >= steps.length) {
            tutorialIndex = steps.length - 1;
        }

        tutorialStepLabel.setText("Tutorial " + (tutorialIndex + 1) + " / " + steps.length);
        tutorialTitleLabel.setText(steps[tutorialIndex][0]);
        tutorialMessageLabel.setText(steps[tutorialIndex][1]);

        boolean lastStep = tutorialIndex == steps.length - 1;
        tutorialNextButton.setVisible(!lastStep);
        tutorialNextButton.setManaged(!lastStep);
        tutorialSkipButton.setVisible(!lastStep);
        tutorialSkipButton.setManaged(!lastStep);
        tutorialStartButton.setVisible(lastStep);
        tutorialStartButton.setManaged(lastStep);

        Node target = getTutorialTargetNode(tutorialIndex);

        Platform.runLater(() -> {
            positionTutorialFixed(target, lastStep);
            animateTutorialStep(lastStep);
        });
    }

    private Node getTutorialTargetNode(int step) {
        switch (step) {
            case 1:
                return playerInfoPanelNode != null ? playerInfoPanelNode : rightPanel;
            case 2:
                return opponentInfoPanelNode != null ? opponentInfoPanelNode : leftPanel;
            case 3:
                return actionButtonsBoxNode != null ? actionButtonsBoxNode : actionControlsNode;
            case 4:
                return lastCardBoxNode != null ? lastCardBoxNode : diceImageView;
            case 5:
                return legendPanel;
            case 6:
                return statusBox != null ? statusBox : topButtonsNode;
            default:
                return null;
        }
    }

    private void positionTutorialFixed(Node target, boolean lastStep) {
        if (tutorialOverlay == null || tutorialBox == null || tutorialHighlight == null) {
            return;
        }

        tutorialBox.applyCss();
        tutorialBox.layout();
        tutorialButtonsBox.applyCss();
        tutorialButtonsBox.layout();

        double overlayWidth = tutorialOverlay.getWidth();
        double overlayHeight = tutorialOverlay.getHeight();

        if (overlayWidth <= 0 || overlayHeight <= 0) {
            return;
        }

        double boxW = Math.min(620, overlayWidth - 60);
        double boxH = tutorialBox.prefHeight(boxW);

        if (boxH <= 0 || Double.isNaN(boxH)) {
            boxH = 178;
        }

        // Bottom buttons are positioned independently, so they never sit on top of the message.
        double buttonsW = tutorialButtonsBox.prefWidth(-1);
        double buttonsH = tutorialButtonsBox.prefHeight(-1);
        if (buttonsW <= 0 || Double.isNaN(buttonsW)) {
            buttonsW = 460;
        }
        if (buttonsH <= 0 || Double.isNaN(buttonsH)) {
            buttonsH = 70;
        }

        double buttonsX = (overlayWidth - buttonsW) / 2.0;
        double buttonsY = overlayHeight - buttonsH - 34;
        buttonsX = clamp(buttonsX, 18, overlayWidth - buttonsW - 18);
        buttonsY = clamp(buttonsY, 18, overlayHeight - buttonsH - 18);
        tutorialButtonsBox.resize(buttonsW, buttonsH);
        tutorialButtonsBox.setLayoutX(buttonsX);
        tutorialButtonsBox.setLayoutY(buttonsY);

        // Message stays fixed around the center, but moves up if the screen is short.
        double boxX = (overlayWidth - boxW) / 2.0;
        double boxY = (overlayHeight - boxH) / 2.0;
        double maxBoxYBeforeButtons = buttonsY - boxH - 26;
        if (boxY > maxBoxYBeforeButtons) {
            boxY = maxBoxYBeforeButtons;
        }
        boxX = clamp(boxX, 18, overlayWidth - boxW - 18);
        boxY = clamp(boxY, 110, overlayHeight - boxH - 120);

        tutorialBox.resize(boxW, boxH);
        tutorialBox.setLayoutX(boxX);
        tutorialBox.setLayoutY(boxY);

        if (lastStep || target == null) {
            tutorialHighlight.setVisible(false);
            tutorialArrowLine.setVisible(false);
            tutorialArrowHead.setVisible(false);
            tutorialArrowLabel.setVisible(false);
            tutorialBox.toFront();
            tutorialButtonsBox.toFront();
            return;
        }

        Bounds targetBounds = getTargetBoundsInOverlay(target);
        if (targetBounds == null) {
            tutorialHighlight.setVisible(false);
            tutorialArrowLine.setVisible(false);
            tutorialArrowHead.setVisible(false);
            tutorialArrowLabel.setVisible(false);
            tutorialBox.toFront();
            tutorialButtonsBox.toFront();
            return;
        }

        double pad = getTutorialTargetPadding(tutorialIndex);
        double highX = targetBounds.getMinX() - pad;
        double highY = targetBounds.getMinY() - pad;
        double highW = targetBounds.getWidth() + pad * 2;
        double highH = targetBounds.getHeight() + pad * 2;

        // Keep rectangles tight and aligned with the actual object. Do not enlarge to area containers.
        double maxAllowedW = overlayWidth - 24;
        double maxAllowedH = overlayHeight - 24;
        highW = Math.min(highW, maxAllowedW);
        highH = Math.min(highH, maxAllowedH);
        highX = clamp(highX, 12, overlayWidth - highW - 12);
        highY = clamp(highY, 12, overlayHeight - highH - 12);

        tutorialHighlight.setX(0);
        tutorialHighlight.setY(0);
        tutorialHighlight.setWidth(highW);
        tutorialHighlight.setHeight(highH);
        tutorialHighlight.setLayoutX(highX);
        tutorialHighlight.setLayoutY(highY);
        tutorialHighlight.setVisible(true);

        // Arrows are intentionally hidden; the glowing rectangle is the only pointer.
        tutorialArrowLine.setVisible(false);
        tutorialArrowHead.setVisible(false);
        tutorialArrowLabel.setVisible(false);

        tutorialBox.toFront();
        tutorialButtonsBox.toFront();
    }

    private Bounds getTargetBoundsInOverlay(Node target) {
        try {
            if (target == null || target.getScene() == null || tutorialOverlay.getScene() == null) {
                return null;
            }

            Bounds localBounds = target.getLayoutBounds();
            Point2D sceneMin = target.localToScene(localBounds.getMinX(), localBounds.getMinY());
            Point2D sceneMax = target.localToScene(localBounds.getMaxX(), localBounds.getMaxY());

            Point2D overlayMin = tutorialOverlay.sceneToLocal(sceneMin);
            Point2D overlayMax = tutorialOverlay.sceneToLocal(sceneMax);

            double minX = Math.min(overlayMin.getX(), overlayMax.getX());
            double minY = Math.min(overlayMin.getY(), overlayMax.getY());
            double maxX = Math.max(overlayMin.getX(), overlayMax.getX());
            double maxY = Math.max(overlayMin.getY(), overlayMax.getY());

            return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
        } catch (Exception e) {
            return null;
        }
    }

    private double getTutorialTargetPadding(int step) {
        if (step == 3) {
            return 7;
        }
        if (step == 4) {
            return 10;
        }
        if (step == 6) {
            return 9;
        }
        return 12;
    }

    private void positionTutorialArrow(double boxX, double boxY, double boxW, double boxH,
                                       double targetCenterX, double targetCenterY,
                                       double targetW, double targetH) {
        double boxCenterX = boxX + boxW / 2.0;
        double boxCenterY = boxY + boxH / 2.0;

        double dx = targetCenterX - boxCenterX;
        double dy = targetCenterY - boxCenterY;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length < 1) {
            tutorialArrowLine.setVisible(false);
            tutorialArrowHead.setVisible(false);
            tutorialArrowLabel.setVisible(false);
            return;
        }

        double ux = dx / length;
        double uy = dy / length;

        // Keep the arrow short and cute: it points at the object without drawing across the whole screen.
        double targetRadius = Math.min(70, Math.max(32, Math.min(targetW, targetH) / 2.0 + 12));
        double endX = targetCenterX - ux * targetRadius;
        double endY = targetCenterY - uy * targetRadius;

        double arrowLength = 118;
        double startX = endX - ux * arrowLength;
        double startY = endY - uy * arrowLength;

        // If the short arrow would start inside the message box, shorten it more instead of crossing the box.
        if (startX > boxX - 8 && startX < boxX + boxW + 8 && startY > boxY - 8 && startY < boxY + boxH + 8) {
            arrowLength = 76;
            startX = endX - ux * arrowLength;
            startY = endY - uy * arrowLength;
        }

        tutorialArrowLine.setStartX(startX);
        tutorialArrowLine.setStartY(startY);
        tutorialArrowLine.setEndX(endX);
        tutorialArrowLine.setEndY(endY);
        tutorialArrowLine.setVisible(true);

        double headSize = 14;
        double leftX = endX - ux * headSize - uy * headSize * 0.65;
        double leftY = endY - uy * headSize + ux * headSize * 0.65;
        double rightX = endX - ux * headSize + uy * headSize * 0.65;
        double rightY = endY - uy * headSize - ux * headSize * 0.65;

        tutorialArrowHead.getPoints().setAll(
            endX, endY,
            leftX, leftY,
            rightX, rightY
        );
        tutorialArrowHead.setVisible(true);

        tutorialArrowLabel.setLayoutX((startX + endX) / 2.0 - 16);
        tutorialArrowLabel.setLayoutY((startY + endY) / 2.0 - 18);
        tutorialArrowLabel.setVisible(true);
    }

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private void animateTutorialStep(boolean lastStep) {
        if (tutorialBox == null || tutorialHighlight == null || tutorialArrowLine == null || tutorialArrowHead == null) {
            return;
        }

        tutorialBox.setOpacity(0.0);
        tutorialBox.setScaleX(0.95);
        tutorialBox.setScaleY(0.95);

        FadeTransition boxFade = new FadeTransition(Duration.millis(200), tutorialBox);
        boxFade.setFromValue(0.0);
        boxFade.setToValue(1.0);

        ScaleTransition boxPop = new ScaleTransition(Duration.millis(230), tutorialBox);
        boxPop.setFromX(0.95);
        boxPop.setFromY(0.95);
        boxPop.setToX(1.0);
        boxPop.setToY(1.0);

        ParallelTransition entrance = new ParallelTransition(boxFade, boxPop);
        entrance.play();

        if (lastStep) {
            return;
        }

        FadeTransition arrowPulse = new FadeTransition(Duration.millis(620), tutorialArrowLine);
        arrowPulse.setFromValue(0.48);
        arrowPulse.setToValue(1.0);
        arrowPulse.setAutoReverse(true);
        arrowPulse.setCycleCount(4);

        FadeTransition arrowHeadPulse = new FadeTransition(Duration.millis(620), tutorialArrowHead);
        arrowHeadPulse.setFromValue(0.48);
        arrowHeadPulse.setToValue(1.0);
        arrowHeadPulse.setAutoReverse(true);
        arrowHeadPulse.setCycleCount(4);

        FadeTransition sparklePulse = new FadeTransition(Duration.millis(620), tutorialArrowLabel);
        sparklePulse.setFromValue(0.45);
        sparklePulse.setToValue(1.0);
        sparklePulse.setAutoReverse(true);
        sparklePulse.setCycleCount(4);

        Timeline highlightPulse = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(tutorialHighlight.opacityProperty(), 0.55),
                new KeyValue(tutorialHighlight.strokeWidthProperty(), 2.7)
            ),
            new KeyFrame(Duration.millis(520),
                new KeyValue(tutorialHighlight.opacityProperty(), 1.0),
                new KeyValue(tutorialHighlight.strokeWidthProperty(), 4.2)
            )
        );
        highlightPulse.setAutoReverse(true);
        highlightPulse.setCycleCount(4);

        new ParallelTransition(arrowPulse, arrowHeadPulse, sparklePulse).play();
        highlightPulse.play();
    }

    private void closeTutorial() {
        if (tutorialOverlay == null) {
            return;
        }

        FadeTransition fade = new FadeTransition(Duration.millis(220), tutorialOverlay);
        fade.setFromValue(tutorialOverlay.getOpacity());
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            tutorialOverlay.setVisible(false);
            tutorialActive = false;
            setTurnControlsDisabled(false);
            updateStatus("Your turn. Roll the dice to begin!");
        });
        fade.play();
    }

    private void setupPowerupButton() {
        powerupButton.setOnAction(e -> {
            if (tutorialActive || robotTurnActive || game == null || game.getCurrent() != game.getPlayer()) {
                return;
            }

            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            powerupSelected = !powerupSelected;
            updatePowerupButtonVisual();

            if (powerupSelected) {
                updateStatus("Powerup selected. Roll dice to activate it.");
            } else {
                updateStatus("Powerup cancelled.");
            }
        });
    }

    private void updatePowerupButtonVisual() {
        if (powerupButton == null) {
            return;
        }

        if (powerupSelected) {
            powerupButton.setOpacity(1.0);
            powerupButton.setEffect(new DropShadow(22, Color.web("#00E5FF")));
        } else {
            powerupButton.setOpacity(0.78);
            powerupButton.setEffect(null);
        }
    }

    private void updateDiceImage(int roll) {
        if (roll < 1 || roll > 6) {
            return;
        }

        diceImageView.setImage(loadControlImage("dice_" + roll + ".png"));
        diceImageView.setOpacity(1.0);

        playDiceAnimation();
    }

    private Image loadPanelImage(String fileName) {
        return ImageCache.get("/game/gui/assets/panels/" + fileName);
    }

    private Image loadBoardImage(String fileName) {
        return ImageCache.get("/game/gui/assets/board/" + fileName);
    }

    private Image loadStatusIcon(String fileName) {
        return ImageCache.get("/game/gui/assets/status/" + fileName);
    }

    private Image loadControlImage(String fileName) {
        return ImageCache.get("/game/gui/assets/controls/" + fileName);
    }

    private String getMonsterTokenFileName(String monsterName) {
        String name = monsterName.toLowerCase();

        if (name.equals("celia mae")) {
            return "token_celia_mae.png";
        }

        if (name.equals("fungus")) {
            return "token_fungus.png";
        }

        if (name.equals("james p. sullivan")) {
            return "token_james_p_sullivan.png";
        }

        if (name.equals("mike wazowski")) {
            return "token_mike_wazowski.png";
        }

        if (name.equals("randall boggs")) {
            return "token_randall_boggs.png";
        }

        if (name.equals("yeti")) {
            return "token_yeti.png";
        }

        return "token_mike_wazowski.png";
    }

    private void setupRollButton() {
        rollDiceButton.setOnAction(e -> {
            if (tutorialActive || robotTurnActive || game == null) {
                return;
            }

            if (game.getCurrent() != game.getPlayer()) {
                updateStatus("Opponent is playing automatically...");
                return;
            }

            SoundManager.getInstance().playSound(SoundManager.BUTTON_CLICK);
            executeTurn(false);
        });
    }

    private void executeTurn(boolean robotTurn) {
        try {
            if (game == null) {
                return;
            }

            if (robotTurn) {
                robotTurnActive = true;
                setTurnControlsDisabled(true);
            }

            Monster actingMonster = game.getCurrent();
            Monster otherMonster = getOtherMonster(actingMonster);

            boolean wasFrozen = actingMonster.isFrozen();

            int actingOldPosition = actingMonster.getPosition();
            int otherOldPosition = otherMonster.getPosition();

            int actingOldEnergy = actingMonster.getEnergy();
            int otherOldEnergy = otherMonster.getEnergy();

            String actingOldRole = String.valueOf(actingMonster.getRole());
            String otherOldRole = String.valueOf(otherMonster.getRole());

            boolean actingWasShielded = actingMonster.isShielded();
            boolean otherWasShielded = otherMonster.isShielded();

            int cardDrawCountBefore = Board.getCardDrawCount();

            if (robotTurn) {
                maybeUseRobotPowerup(actingMonster);
            } else if (powerupSelected) {
                game.usePowerup();
                SoundManager.getInstance().playSound(SoundManager.POWERUP);
                log("Powerup activated by " + actingMonster.getName());
                powerupSelected = false;
                updatePowerupButtonVisual();
            }

            SoundManager.getInstance().playSound(SoundManager.DICE_ROLL);

            int roll = game.playTurn();
            updateDiceImage(roll);

            if (wasFrozen) {
                log(actingMonster.getName() + " was frozen and skipped the turn.");
                updateStatus(actingMonster.getName() + " was frozen and skipped the turn.");
            } else if (robotTurn) {
                log("Robot opponent " + actingMonster.getName() + " rolled: " + roll);
                updateStatus("Opponent " + actingMonster.getName() + " rolled: " + roll);
            } else {
                log(actingMonster.getName() + " rolled: " + roll);
                updateStatus(actingMonster.getName() + " rolled: " + roll);
            }

            int cardDrawCountAfter = Board.getCardDrawCount();
            Card cardAfter = Board.getLastDrawnCard();
            boolean drewCardThisTurn = !wasFrozen && cardDrawCountAfter > cardDrawCountBefore && cardAfter != null;

            logTurnEffects(
                actingMonster,
                otherMonster,
                actingOldPosition,
                otherOldPosition,
                actingOldEnergy,
                otherOldEnergy,
                actingOldRole,
                otherOldRole,
                actingWasShielded,
                otherWasShielded
            );

            int actingNewPosition = actingMonster.getPosition();

            pendingPlayerEnergyDelta = game.getPlayer().getEnergy() -
                (actingMonster == game.getPlayer() ? actingOldEnergy : otherOldEnergy);
            pendingOpponentEnergyDelta = game.getOpponent().getEnergy() -
                (actingMonster == game.getOpponent() ? actingOldEnergy : otherOldEnergy);

            final Card drawnCardForPopup = cardAfter;
            final boolean shouldShowCardPopup = drewCardThisTurn;

            playMonsterMoveAnimation(
                actingMonster,
                otherMonster,
                actingOldPosition,
                otherOldPosition,
                roll,
                actingNewPosition,
                wasFrozen,
                () -> {
                    if (!wasFrozen && actingNewPosition != actingOldPosition) {
                        playLandingSoundForCell(game, actingNewPosition);
                    }
                },
                () -> {
                    refreshBoard(game);
                    updateMonsterInfo(game);
                    updateCurrentTurn(game);

                    if (shouldShowCardPopup && drawnCardForPopup != null) {
                        updateLastCardInfo(drawnCardForPopup);
                        showCardPopup(drawnCardForPopup);
                    } else {
                        updateLastCardInfo(Board.getLastDrawnCard());
                    }

                    Monster winner = game.getWinner();
                    if (winner != null) {
                        setTurnControlsDisabled(true);
                        endGameAction.accept(winner);
                        return;
                    }

                    if (robotTurn) {
                        robotTurnActive = false;
                        setTurnControlsDisabled(false);
                    }

                    if (!robotTurn && game.getCurrent() == game.getOpponent()) {
                        scheduleRobotOpponentTurn();
                    }
                }
            );

        } catch (OutOfEnergyException ex) {
            SoundManager.getInstance().playSound(SoundManager.ERROR_POPUP);
            AlertHelper.showError("Not Enough Energy", ex.getMessage());
            powerupSelected = false;
            robotTurnActive = false;
            updatePowerupButtonVisual();
            setTurnControlsDisabled(false);
        } catch (InvalidMoveException ex) {
            SoundManager.getInstance().playSound(SoundManager.ERROR_POPUP);
            AlertHelper.showError("Invalid Move", ex.getMessage());

            powerupSelected = false;
            updatePowerupButtonVisual();

            if (robotTurn) {
                log("Robot opponent made an invalid move and will try again.");
                updateStatus("Robot opponent made an invalid move. It will try again...");
                updateCurrentTurn(game);
                robotTurnActive = false;
                setTurnControlsDisabled(true);
                scheduleRobotOpponentRetry();
            } else {
                setTurnControlsDisabled(false);
                updateCurrentTurn(game);
            }
        } catch (Exception ex) {
            SoundManager.getInstance().playSound(SoundManager.ERROR_POPUP);
            AlertHelper.showError("Error", ex.getMessage());
            robotTurnActive = false;
            setTurnControlsDisabled(false);
        }
    }

    private void scheduleRobotOpponentTurn() {
        if (game == null || game.getCurrent() != game.getOpponent()) {
            setTurnControlsDisabled(false);
            return;
        }

        robotTurnActive = true;
        setTurnControlsDisabled(true);
        updateStatus("Robot opponent is thinking...");

        PauseTransition pause = new PauseTransition(Duration.millis(850));
        pause.setOnFinished(e -> executeTurn(true));
        pause.play();
    }

    private void scheduleRobotOpponentRetry() {
        if (game == null || game.getCurrent() != game.getOpponent()) {
            robotTurnActive = false;
            setTurnControlsDisabled(false);
            return;
        }

        robotTurnActive = true;
        setTurnControlsDisabled(true);

        PauseTransition retryPause = new PauseTransition(Duration.millis(750));
        retryPause.setOnFinished(e -> executeTurn(true));
        retryPause.play();
    }

    private void maybeUseRobotPowerup(Monster robotMonster) throws OutOfEnergyException {
        if (robotMonster == null) {
            return;
        }

        if (robotMonster.getEnergy() >= Constants.POWERUP_COST && Math.random() < 0.35) {
            game.usePowerup();
            SoundManager.getInstance().playSound(SoundManager.POWERUP);
            log("Robot opponent used a powerup.");
            updateStatus("Robot opponent used a powerup!");
        }
    }

    private void setTurnControlsDisabled(boolean disabled) {
        rollDiceButton.setDisable(disabled || tutorialActive);
        powerupButton.setDisable(disabled || tutorialActive);
        rollDiceButton.setOpacity(disabled || tutorialActive ? 0.55 : 1.0);
        powerupButton.setOpacity(disabled || tutorialActive ? 0.55 : (powerupSelected ? 1.0 : 0.78));
    }

    /**
     * Play the appropriate sound based on the cell type the acting monster
     * just landed on. Falls back gracefully if the cell cannot be determined.
     */
    private void playLandingSoundForCell(Game game, int position) {
        try {
            Board board = game.getBoard();
            Cell[][] boardCells = board.getBoardCells();
            Cell cell = getCellFromBoardArray(boardCells, position);

            if (cell == null) {
                SoundManager.getInstance().playSound(SoundManager.MONSTER_MOVE);
                return;
            }

            String cellClass = cell.getClass().getSimpleName();

            switch (cellClass) {
                case "ContaminationSock":
                    SoundManager.getInstance().playSound(SoundManager.CONTAMINATION_SOCK);
                    break;
                case "ConveyorBelt":
                    SoundManager.getInstance().playSound(SoundManager.CONVEYOR_BELT);
                    break;
                case "DoorCell":
                    SoundManager.getInstance().playSound(SoundManager.DOOR);
                    break;
                default:
                    SoundManager.getInstance().playSound(SoundManager.MONSTER_MOVE);
                    break;
            }
        } catch (Exception e) {
            // Non-critical — fall back silently
            SoundManager.getInstance().playSound(SoundManager.MONSTER_MOVE);
        }
    }

    private Monster getOtherMonster(Monster monster) {
        if (monster == game.getPlayer()) {
            return game.getOpponent();
        }

        return game.getPlayer();
    }

    private void logTurnEffects(
            Monster actingMonster,
            Monster otherMonster,
            int actingOldPosition,
            int otherOldPosition,
            int actingOldEnergy,
            int otherOldEnergy,
            String actingOldRole,
            String otherOldRole,
            boolean actingWasShielded,
            boolean otherWasShielded
    ) {
        logMonsterChanges(actingMonster, actingOldPosition, actingOldEnergy, actingOldRole, actingWasShielded);
        logMonsterChanges(otherMonster, otherOldPosition, otherOldEnergy, otherOldRole, otherWasShielded);
    }

    private void logMonsterChanges(
            Monster monster,
            int oldPosition,
            int oldEnergy,
            String oldRole,
            boolean wasShielded
    ) {
        if (monster.getPosition() != oldPosition) {
            log(monster.getName() + " moved from cell " + oldPosition + " to cell " + monster.getPosition() + ".");
        }

        if (monster.getEnergy() != oldEnergy) {
            int change = monster.getEnergy() - oldEnergy;

            if (change > 0) {
                log(monster.getName() + " gained " + change + " energy.");
            } else {
                log(monster.getName() + " lost " + (-change) + " energy.");
            }
        }

        if (wasShielded && monster.getEnergy() == oldEnergy) {
            log(monster.getName() + "'s shield may have blocked an energy loss.");
        }

        if (!String.valueOf(monster.getRole()).equals(oldRole)) {
            log(monster.getName() + " role changed from " + oldRole + " to " + monster.getRole() + ".");
        }
    }

    public void updateCurrentTurn(Game game) {
        try {
            turnLabel.setText("Current Turn: " + game.getCurrent().getName());


        } catch (Exception e) {
            AlertHelper.showError("Turn Update Error", e.getMessage());
        }
    }

    public void updateMonsterPositions(Game game) {
        refreshBoard(game);
    }

    private void updateMonsterInfo(Game game) {
        renderMonsterInfo(game);
        pendingPlayerEnergyDelta = 0;
        pendingOpponentEnergyDelta = 0;
    }

    private void showCardPopup(Card card) {
        SoundManager.getInstance().playSound(SoundManager.CARD_POPUP);
        AlertHelper.showCard(card.getName(), card.getDescription());
        log("Card drawn: " + card.getName() + " - " + card.getDescription());
    }

    private Cell getCellFromBoardArray(Cell[][] boardCells, int index) {
        int row = index / 10;
        int col;

        if (row % 2 == 0) {
            col = index % 10;
        } else {
            col = 9 - (index % 10);
        }

        return boardCells[row][col];
    }

    private int getVisualRow(int index) {
        int engineRow = index / 10;
        return 9 - engineRow;
    }

    private int getVisualColumn(int index) {
        int row = index / 10;

        if (row % 2 == 0) {
            return index % 10;
        } else {
            return 9 - (index % 10);
        }
    }

    public void log(String text) {
        try {
            logArea.appendText(text + "\n");
        } catch (Exception e) {
            AlertHelper.showError("Log Error", "Could not write log");
        }
    }

    public void cheatWin(Game game) {
        try {
            Monster player = game.getPlayer();

            player.setPosition(99);
            player.setEnergy(1000);

            refreshBoard(game);
            updateMonsterInfo(game);
            updateCurrentTurn(game);
            updateLastCardInfo(Board.getLastDrawnCard());

            log("Cheat W pressed: Player wins instantly.");

            endGameAction.accept(player);

        } catch (Exception e) {
            AlertHelper.showError(
                "Cheat Error",
                "Could not activate W cheat:\n" + e.getMessage()
            );
        }
    }

    public void cheatLose(Game game) {
        try {
            Monster opponent = game.getOpponent();

            opponent.setPosition(99);
            opponent.setEnergy(1000);

            refreshBoard(game);
            updateMonsterInfo(game);
            updateCurrentTurn(game);
            updateLastCardInfo(Board.getLastDrawnCard());

            log("Cheat L pressed: Opponent wins instantly.");

            endGameAction.accept(opponent);

        } catch (Exception e) {
            AlertHelper.showError(
                "Cheat Error",
                "Could not activate L cheat:\n" + e.getMessage()
            );
        }
    }

    public void cheatEnergy(Game game) {
        try {
            Monster current = game.getCurrent();

            current.cheatAddEnergy(500);

            if (current == game.getPlayer()) {
                pendingPlayerEnergyDelta = 500;
            } else {
                pendingOpponentEnergyDelta = 500;
            }

            updateMonsterInfo(game);
            updateCurrentTurn(game);
            updateLastCardInfo(Board.getLastDrawnCard());

            log("Cheat E pressed: " + current.getName() + " gained 500 energy.");

        } catch (Exception e) {
            AlertHelper.showError("Cheat Error", "Could not activate E cheat:\n" + e.getMessage());
        }
    }
}