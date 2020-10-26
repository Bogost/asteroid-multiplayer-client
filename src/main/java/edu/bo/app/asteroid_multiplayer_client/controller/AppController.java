package edu.bo.app.asteroid_multiplayer_client.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.bo.app.asteroid_multiplayer_client.exception.UnknownResponseException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AppController implements Initializable {

    private FramePainter framePainter;
    private AppStateSwitcher appStateSwitcher;
    private UserStateSwitcher userStateSwitcher;
    private Connector connector;
    private Room room;
    private ScoreRoom scoreRoom;

    @FXML
    Canvas canvas;

    @FXML
    VBox titleBox;

    @FXML
    VBox preRoomBox;

    @FXML
    VBox roomBox;

    @FXML
    VBox postGameBox;

    @FXML
    CheckBox voiceChat;

    @FXML
    TextField yourName;

    @FXML
    TextField roomName;

    @FXML
    Label subtitle;

    @FXML
    Label roomNameLabel;

    @FXML
    GridPane roomGridPane;

    @FXML
    GridPane scoreGrid;

    @FXML
    Button startButton;

    @FXML
    private void startGame(ActionEvent e) {
        try {
            connector.sendStartGame();
        } catch (IOException e1) {
            e1.printStackTrace();
            AlertManager.connectionProblemAlert(e1);
            connector.disconnect();
        }
    }

    @FXML
    private void goToRoom(ActionEvent e) {
        System.out.println("goToRoom");
        appStateSwitcher.switchTo(AppState.PRE_ROOM);
        userStateSwitcher.switchTo(UserState.GUEST);
    }

    @FXML
    private void createRoom(ActionEvent e) {
        System.out.println("createRoom");

        appStateSwitcher.switchTo(AppState.PRE_ROOM);
        userStateSwitcher.switchTo(UserState.HOST);
    }

    @FXML
    private void returnToRoom(ActionEvent e) {

    }

    @FXML
    private void exit(ActionEvent e) {
        System.out.println("exit");
        Platform.exit();
    }

    @FXML
    private void connect(ActionEvent e) {
        boolean connected;
        System.out.println("connect");
        try {
            connector.gameConnect();
            boolean isHost = userStateSwitcher.getState() == UserState.HOST;
            String name = yourName.getText();
            String rName = roomName.getText();
            boolean isVoceChat = voiceChat.isSelected();
            connected = connector.connectToRoom(isHost, name, rName, isVoceChat);
            if (connected) {
                roomNameLabel.setText(rName);
                room.clean();
                connector.startListening();
                appStateSwitcher.switchTo(AppState.ROOM);
            } else {
                String headerMessage = userStateSwitcher.getState() == UserState.HOST ? "Room already exist" : "No room of given name";
                AlertManager.standardAlert(headerMessage, "try diffrent name");
                connector.disconnect();
            }
        } catch (IOException | UnknownResponseException e1) {
            e1.printStackTrace();
            if (e1 instanceof IOException) {
                AlertManager.connectionProblemAlert(e1);
            } else if (e1 instanceof UnknownResponseException) {
                AlertManager.exceptionAlert("Connection problem", "" + e1, e1);
            }
            connector.disconnect();
        }
    }

    @FXML
    private void exitToTitle(ActionEvent e) {
        System.out.println("exit to title");
        appStateSwitcher.switchTo(AppState.TITLE_SCREEN);
        userStateSwitcher.switchTo(UserState.NONE);
        connector.disconnect();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        framePainter = new FramePainter(canvas);
        framePainter.cleanCanvas();

        appStateSwitcher = new AppStateSwitcher(AppState.TITLE_SCREEN, titleBox, preRoomBox, roomBox, postGameBox);
        userStateSwitcher = new UserStateSwitcher(subtitle, startButton);

        try {
            connector = new Connector(this);
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            AlertManager.exceptionAlert("Configuration file broken", "file config/config.xml is broken", e);
            Platform.exit();
        } catch (IOException e) {
            e.printStackTrace();
            AlertManager.exceptionAlert("Configuration file not found", "No file in directory config/config.xml", e);
            Platform.exit();
        }

        room = new Room(connector, appStateSwitcher, roomGridPane, userStateSwitcher);
        scoreRoom = new ScoreRoom(this, scoreGrid);

    }

    public ScoreRoom getScoreRoom() {
        return scoreRoom;
    }

    private void initializeInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            try {
                if (e.getCode() == KeyCode.DOWN) {
                    connector.decelerate();
                } else if (e.getCode() == KeyCode.UP) {
                    connector.accelerate();
                } else if (e.getCode() == KeyCode.LEFT) {
                    connector.rotateLeft();
                } else if (e.getCode() == KeyCode.RIGHT) {
                    connector.rotateRight();
                } else if (e.getCode() == KeyCode.SPACE) {
                    connector.fire();
                }
            } catch (IOException e1) {
                AlertManager.connectionProblemAlert(e1);
                connector.disconnect();
                appStateSwitcher.switchTo(AppState.TITLE_SCREEN);
            }
        });
    }

    public void afterSceneInitialize(Scene scene) {
        // scene.setCursor(Cursor.NONE);
        initializeInput(scene);
    }

    public AppStateSwitcher getAppStateSwitcher() {
        return appStateSwitcher;
    }

    public Room getRoom() {
        return room;
    }

    public Connector getConnector() {
        return connector;
    }

    public FramePainter getFramePainter() {
        return framePainter;
    }
}
