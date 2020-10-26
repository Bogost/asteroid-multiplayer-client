package edu.bo.app.asteroid_multiplayer_client.controller;

public enum AppState {

    GAME(-1),
    TITLE_SCREEN(0),
    PRE_ROOM(1),
    ROOM(2),
    POST_GAME(3);

    private final int value;

    private AppState(int i) {
        this.value = i;
    }

    public int getValue() {
        return value;
    }
}
