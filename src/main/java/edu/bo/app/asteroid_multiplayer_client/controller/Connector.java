package edu.bo.app.asteroid_multiplayer_client.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.bo.app.asteroid_multiplayer_client.exception.UnknownResponseException;
import edu.bo.app.asteroid_multiplayer_common.XmlLoader;
import javafx.application.Platform;

public class Connector {

    private static final String CONFIG_PATH = "config/config.xml";

    private final String address;
    private final int gamePort;
    private final int chatPort;

    private AppController appController;

    private Socket gameSocket;
    private DataInputStream gameInStream;
    private DataOutputStream gameOutStream;

    private Socket chatSocket;
    private DataInputStream chatInStream;
    private DataOutputStream chatOutStream;

    private SoundManager soundManager;

    volatile public boolean isConnected = false;
    boolean inGame = false;

    public Connector(AppController appController) throws ParserConfigurationException, SAXException, IOException {
        this.appController = appController;
        XmlLoader xmlLoader = new XmlLoader(Connector.CONFIG_PATH);
        address = xmlLoader.getValue("server", "address");
        gamePort = Integer.parseInt(xmlLoader.getValue("server", "ports", "game"));
        chatPort = Integer.parseInt(xmlLoader.getValue("server", "ports", "chat"));
        soundManager = new SoundManager(this);
    }

    public void gameConnect() throws UnknownHostException, IOException {
        gameSocket = new Socket(address, gamePort);
        gameInStream = new DataInputStream(new BufferedInputStream(gameSocket.getInputStream()));
        gameOutStream = new DataOutputStream(new BufferedOutputStream(gameSocket.getOutputStream()));
        isConnected = true;
        System.out.print("connected\naddress: " + address + "\ngame port: " + gamePort + "\n");
    }

    private void chatConnect() throws UnknownHostException, IOException {
        chatSocket = new Socket(address, chatPort);
        chatInStream = new DataInputStream(new BufferedInputStream(chatSocket.getInputStream()));
        chatOutStream = new DataOutputStream(new BufferedOutputStream(chatSocket.getOutputStream()));
    }

    private void endGameProcedure() {
        inGame = false;
        Platform.runLater(() -> {
            appController.getFramePainter()
                         .cleanCanvas();
        });
        appController.getScoreRoom()
                     .cleanGread();
    }

    public void disconnect() {
        try {
            isConnected = false;
            if (inGame) {
                endGameProcedure();
            }
            if (soundManager.isRunning()) {
                soundManager.stop();
            }
            if (gameSocket != null) {
                int position = appController.getRoom()
                                            .getYourPosition();
                if (position != -1) {
                    kickOut(position);
                }
                gameInStream.close();
                gameOutStream.close();
                gameSocket.close();
                gameSocket = null;
            }
            if (chatSocket != null) {
                chatInStream.close();
                chatOutStream.close();
                chatSocket.close();
                chatSocket = null;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean connectToRoom(boolean host, String name, String roomName, boolean chat) throws IOException, UnknownResponseException {
        System.out.println("sending invitation message");

        gameOutStream.writeInt(host ? 1 : 2);
        gameOutStream.writeUTF(name);
        gameOutStream.writeUTF(roomName);
        gameOutStream.writeInt(chat ? 1 : 0);
        gameOutStream.flush();

        int response = gameInStream.readInt();

        System.out.println("geting response: " + response);

        if (response == 10) {
            if (chat) {
                chatConnect();
                boolean isChatConnected = connectToChatRoom(name, roomName);
                if (isChatConnected) {
                    soundManager.start(chatInStream, chatOutStream);
                }
                return isChatConnected;
            }
            return true;
        }
        if (response == 0) {
            return false;
        }

        throw new UnknownResponseException("" + response, "0", "10");
    }

    private boolean connectToChatRoom(String name, String roomName) throws IOException, UnknownResponseException {
        chatOutStream.writeInt(1);
        chatOutStream.writeUTF(name);
        chatOutStream.writeUTF(roomName);
        chatOutStream.flush();

        int response = chatInStream.readInt();

        if (response == 10) {
            return true;
        }
        if (response == 0) {
            return false;
        }

        throw new UnknownResponseException("" + response, "0", "10");
    }

    public void startListening() {
        Thread th = new Thread("listen gameSocket") {

            @Override
            public void run() {
                int command, position, points, pointsPosition, time;
                String name;
                double x, y, rotation;
                while (true) {
                    try {
                        command = gameInStream.readInt();
                        // System.out.println("command: " + command);
                        // place player
                        if (command == 1) {
                            position = gameInStream.readInt();
                            name = gameInStream.readUTF();
                            System.out.println("add player: " + name + " on position: " + position);
                            appController.getRoom()
                                         .addPlayer(position, name);
                        } // remove player
                        else if (command == 2) {
                            position = gameInStream.readInt();
                            appController.getRoom()
                                         .removePlayer(position);
                        } // player is ready
                        else if (command == 3) {
                            position = gameInStream.readInt();
                            appController.getRoom()
                                         .setAsReady(position);
                        } // start game
                        else if (command == 4) {
                            Platform.runLater(() -> {
                                appController.getAppStateSwitcher()
                                             .switchTo(AppState.GAME);
                            });
                            inGame = true;
                        } // mark host
                        else if (command == 5) {
                            position = gameInStream.readInt();
                            appController.getRoom()
                                         .setHost(position);
                        } // mark you
                        else if (command == 6) {
                            position = gameInStream.readInt();
                            appController.getRoom()
                                         .setYou(position);
                        } // add end scores
                        else if (command == 7) {
                            name = gameInStream.readUTF();
                            position = gameInStream.readInt();
                            points = gameInStream.readInt();
                            appController.getScoreRoom()
                                         .addScore(name, position, points);
                        }

                        // paint player
                        else if (command == -1) {
                            position = gameInStream.readInt();
                            x = gameInStream.readDouble();
                            y = gameInStream.readDouble();
                            rotation = gameInStream.readDouble();
                            // determine if its you by room number
                            if (position != appController.getRoom()
                                                         .getYourPosition()) {
                                appController.getFramePainter()
                                             .addRemotePlayer(x, y, rotation);
                            } else {
                                appController.getFramePainter()
                                             .addPlayer(x, y, rotation);
                            }
                        } // paint player bullet
                        else if (command == -2) {

                            position = gameInStream.readInt();
                            x = gameInStream.readDouble();
                            y = gameInStream.readDouble();
                            System.out.println("paint bullet at: " + x + " " + y);
                            // determine if its yours by room number
                            if (position != appController.getRoom()
                                                         .getYourPosition()) {
                                appController.getFramePainter()
                                             .addRemotePlayerBullet(x, y);
                            } else {
                                appController.getFramePainter()
                                             .addPlayerBullet(x, y);
                            }
                        } // paint small ufo
                        else if (command == -6) {
                            x = gameInStream.readDouble();
                            y = gameInStream.readDouble();
                            appController.getFramePainter()
                                         .addSmallUfo(x, y);
                        } // paint big ufo
                        else if (command == -7) {
                            x = gameInStream.readDouble();
                            y = gameInStream.readDouble();
                            appController.getFramePainter()
                                         .addBigUfo(x, y);
                        }
                        // paint counter
                        else if (command == -8) {
                            time = gameInStream.readInt();
                            appController.getFramePainter()
                                         .addCounter(time);
                        }
                        // paint frame
                        else if (command == -9) {
                            Platform.runLater(() -> {
                                appController.getFramePainter()
                                             .paint();
                            });
                        } // end game
                        else if (command == -10) {
                            Platform.runLater(() -> {
                                appController.getAppStateSwitcher()
                                             .switchTo(AppState.POST_GAME);
                            });
                            endGameProcedure();
                        } // actualize points
                        else if (command == -11) {
                            pointsPosition = gameInStream.readInt();
                            points = gameInStream.readInt();
                            if (pointsPosition != appController.getRoom()
                                                               .getYourPosition()) {
                                appController.getFramePainter()
                                             .addRemotePoints(points, pointsPosition);
                                System.out.println("points: " + points);
                            } else {
                                appController.getFramePainter()
                                             .addYouPoints(points, pointsPosition);
                                System.out.println("points: " + points);
                            }
                        }
                    } catch (IOException e) {
                        if (isConnected) {
                            AlertManager.connectionProblemAlert(e);
                            disconnect();
                            e.printStackTrace();
                        }
                        appController.getAppStateSwitcher()
                                     .switchTo(AppState.TITLE_SCREEN);
                        return;
                    }
                }
            }
        };
        th.start();
    }

    public void kickOut(int position) throws IOException {
        gameOutStream.writeInt(4);
        gameOutStream.writeInt(position);
        gameOutStream.flush();
    }

    public void sendReady() throws IOException {
        gameOutStream.writeInt(3);
        gameOutStream.flush();
    }

    public void sendStartGame() throws IOException {
        gameOutStream.writeInt(5);
        gameOutStream.flush();
    }

    public void accelerate() throws IOException {
        gameOutStream.writeInt(-1);
        gameOutStream.flush();
    }

    public void decelerate() throws IOException {
        gameOutStream.writeInt(-2);
        gameOutStream.flush();
    }

    public void rotateRight() throws IOException {
        gameOutStream.writeInt(-3);
        gameOutStream.flush();
    }

    public void rotateLeft() throws IOException {
        gameOutStream.writeInt(-4);
        gameOutStream.flush();
    }

    public void fire() throws IOException {
        gameOutStream.writeInt(-5);
        gameOutStream.flush();
    }
}
