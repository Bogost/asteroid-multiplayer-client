package edu.bo.app.asteroid_multiplayer_client.controller;

import javafx.scene.layout.VBox;

public class AppStateSwitcher {

    public AppState appState;

    VBox[] vBoxes;

    public AppStateSwitcher(AppState as, VBox... vBoxes) {
        this.vBoxes = vBoxes;
        appState = as;
        for (int i = 0; i < vBoxes.length; i++) {
            if (i != appState.getValue()) {
                this.vBoxes[i].setVisible(false);
            }
        }
    }

    public void switchTo(AppState as) {
        if (appState != AppState.GAME) {
            vBoxes[appState.getValue()].setVisible(false);
        }
        appState = as;
        if (appState != AppState.GAME) {
            vBoxes[appState.getValue()].setVisible(true);
        }
    }
}
