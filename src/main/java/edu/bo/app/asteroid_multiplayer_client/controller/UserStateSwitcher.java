package edu.bo.app.asteroid_multiplayer_client.controller;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class UserStateSwitcher {

    private UserState state = UserState.NONE;
    private Label subtitle;
    private Button start;

    public UserStateSwitcher(Label subtitle, Button start) {
        this.subtitle = subtitle;
        this.start = start;
    };

    public UserState getState() {
        return state;
    }

    public void switchTo(UserState userState) {
        state = userState;
        switch (userState) {
            case HOST:
                subtitle.setText("Create Room");
                start.setVisible(true);
                break;
            case GUEST:
                subtitle.setText("Connect To Room");
                start.setVisible(false);
                break;
        }
    }
}
