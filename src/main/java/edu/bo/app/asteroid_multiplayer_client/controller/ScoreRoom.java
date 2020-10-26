package edu.bo.app.asteroid_multiplayer_client.controller;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class ScoreRoom {

    private GridPane scoreGrid;
    private AppController appController;
    private volatile int scorePosition;

    public ScoreRoom(AppController appController, GridPane scoreGrid) {
        this.appController = appController;
        this.scoreGrid = scoreGrid;
        scorePosition = 0;
    }

    public void cleanGread() {
        Platform.runLater(() -> {
            scoreGrid.getChildren()
                     .clear();
        });
        scorePosition = 0;
    }

    public void addScore(String name, int points, int position) {
        final int p = scorePosition;
        scorePosition++;
        Label lName = new Label(name);
        Label score = new Label("" + points);
        String style;
        if (p != 0) {
            style = "descriptior";
            lName.getStyleClass()
                 .add(style);
            score.getStyleClass()
                 .add(style);
        } else {
            style = "highscore";
            lName.getStyleClass()
                 .add(style);
            score.getStyleClass()
                 .add(style);
        }
        if (position == appController.getRoom()
                                     .getYourPosition()) {
            style = "youScore";
            lName.getStyleClass()
                 .add(style);
            score.getStyleClass()
                 .add(style);
        }

        Platform.runLater(() -> {

            scoreGrid.add(lName, 0, p);
            scoreGrid.add(score, 1, p);
        });
        System.out.println("name: " + name + " position: " + scorePosition + " points: " + points);
    }

}
