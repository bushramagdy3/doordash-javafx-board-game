package game.gui;

import game.engine.Game;
import game.engine.Board;
import game.engine.Role;
import game.engine.monsters.Monster;
import game.gui.utils.AlertHelper;
import game.gui.views.BoardView;
import game.gui.views.EndView;
import game.gui.views.StartView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;
    private StartView startView;
    private BoardView boardView;
    private Game game;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setTitle("DoorDasH");
        showStartScreen();
        primaryStage.show();
        forceMaximized();
    }

    private void showStartScreen() {
        startView = new StartView(primaryStage, selectedRole -> startGame(selectedRole));

        Scene scene = new Scene(startView.getRoot(), 1200, 800);
        addStyle(scene);

        primaryStage.setScene(scene);
        forceMaximized();
    }

    private void startGame(Role selectedRole) {
        try {
            Board.resetCardTracking();
            game = new Game(selectedRole);

            boardView = new BoardView(
                primaryStage,
                () -> showStartScreen(),
                winner -> showEndScreen(winner)
            );

            boardView.renderInitialGame(game);

            Scene scene = new Scene(boardView.getRoot(), 1200, 800);
            addStyle(scene);

            primaryStage.setScene(scene);
            forceMaximized();

            scene.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case W:
                        boardView.cheatWin(game);
                        break;

                    case L:
                        boardView.cheatLose(game);
                        break;

                    case E:
                        boardView.cheatEnergy(game);
                        break;

                    default:
                        break;
                }
            });

        } catch (Exception e) {
            AlertHelper.showError(
                "Game Start Error",
                "Could not start the game:\n" + e.getMessage()
            );
        }
    }

    private void showEndScreen(Monster winner) {
        EndView endView = new EndView(
            primaryStage,
            game,
            winner,
            () -> showStartScreen()
        );

        Scene scene = new Scene(endView.getRoot(), 1200, 800);
        addStyle(scene);

        primaryStage.setScene(scene);
        forceMaximized();
    }

    private void forceMaximized() {
        primaryStage.setIconified(false);
        primaryStage.setMaximized(false);
        primaryStage.show();

        Platform.runLater(() -> {
            primaryStage.setMaximized(true);
            primaryStage.centerOnScreen();
        });
    }

    private void addStyle(Scene scene) {
        try {
            String css = getClass()
            	.getResource("/game/gui/styles/game-style.css")
                .toExternalForm();

            scene.getStylesheets().add(css);

        } catch (Exception e) {
            // If CSS is missing, the game should still run.
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}