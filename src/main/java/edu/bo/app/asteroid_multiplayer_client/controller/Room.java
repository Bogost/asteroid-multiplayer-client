package edu.bo.app.asteroid_multiplayer_client.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class Room {

    private int yourPosition = -1;
    private Label host = null;
    private ArrayList<Integer> playerList = new ArrayList<>();

    private GridPane roomGridPane;
    private UserStateSwitcher userStateSwitcher;
    private AppStateSwitcher appStateSwitcher;
    private Connector connector;

    public Room(Connector connector, AppStateSwitcher appStateSwitcher, GridPane roomGridPane, UserStateSwitcher userStateSwitcher) {
        this.roomGridPane = roomGridPane;
        this.connector = connector;
        this.appStateSwitcher = appStateSwitcher;
        this.userStateSwitcher = userStateSwitcher;
    }

    public int getYourPosition() {
        return yourPosition;
    }

    public void clean() {
        yourPosition = -1;
        host = null;
        Platform.runLater(() -> {
            roomGridPane.getChildren()
                        .clear();
        });
    }

    private Button generateKickOutButton(int position) {
        Button b = new Button("Kick Out");
        b.getStyleClass()
         .add("kick");
        b.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                try {
                    connector.kickOut(position);
                } catch (IOException e1) {
                    AlertManager.connectionProblemAlert(e1);
                    appStateSwitcher.switchTo(AppState.PRE_ROOM);
                    connector.disconnect();
                }
            }
        });
        return b;
    }

    public void addPlayer(int position, String name) {
        Platform.runLater(() -> {
            playerList.add(position);
            Label l = new Label(name);
            l.getStyleClass()
             .add("descriptor");
            roomGridPane.add(l, 1, position);

            l = new Label("preparing");
            l.getStyleClass()
             .add("descriptor");

            roomGridPane.add(l, 2, position);

            if (yourPosition > -1 && userStateSwitcher.getState() == UserState.HOST) {
                roomGridPane.add(generateKickOutButton(position), 3, position);
            }
        });
    }

    public void removePlayer(int position) {
        if (position == yourPosition) {
            appStateSwitcher.switchTo(AppState.TITLE_SCREEN);
            AlertManager.kickedAlert();
        }
        Platform.runLater(() -> {
            playerList.remove(Integer.valueOf(position));
            Set<Node> deleteNodes = new HashSet<>();
            for (Node n : roomGridPane.getChildren()) {
                if (GridPane.getRowIndex(n) == position) {
                    deleteNodes.add(n);
                }
            }
            roomGridPane.getChildren()
                        .removeAll(deleteNodes);
        });
    }

    private Button generateReadyButton() {
        Button b = new Button("Get Ready");

        b.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                try {
                    connector.sendReady();
                    roomGridPane.getChildren()
                                .removeAll(b);
                } catch (IOException e1) {
                    AlertManager.connectionProblemAlert(e1);
                    appStateSwitcher.switchTo(AppState.PRE_ROOM);
                    connector.disconnect();
                }
            }
        });
        return b;
    }

    public void setYou(int position) {
        Platform.runLater(() -> {
            yourPosition = position;
            for (Node n : roomGridPane.getChildren()) {
                if (GridPane.getRowIndex(n) == position) {
                    n.getStyleClass()
                     .add("you");
                }
            }
            roomGridPane.add(generateReadyButton(), 3, position);
        });
    }

    public void setAsReady(int position) {
        Platform.runLater(() -> {
            for (Node n : roomGridPane.getChildren()) {
                if (GridPane.getRowIndex(n) == position) {
                    n.getStyleClass()
                     .add("ready");
                    if (GridPane.getColumnIndex(n) == 2) {
                        ((Label) n).setText("ready");
                    }
                }
            }
        });
    }

    private void setYouAsHost() {
        for (int i : playerList) {
            if (i == yourPosition) {
                continue;
            }
            roomGridPane.add(generateKickOutButton(i), 3, i);
        }
    }

    // change style and optional promote you to host
    public void setHost(int position) {
        Platform.runLater(() -> {
            if (yourPosition == position) {
                userStateSwitcher.switchTo(UserState.HOST);
                setYouAsHost();
            }
            if (host != null) {
                roomGridPane.getChildren()
                            .removeAll(host);
            }
            host = new Label("host");
            roomGridPane.add(host, 0, position);

            // change style in host row
            for (Node n : roomGridPane.getChildren()) {
                if (GridPane.getRowIndex(n) == position) {
                    n.getStyleClass()
                     .add("host");
                }
            }
        });
    }
}
