package edu.bo.app.asteroid_multiplayer_client.exception;

public class UnknownResponseException extends Exception {

    public final String response;
    public final String[] expectedResponse;

    public UnknownResponseException(String response, String... expectedResponse) {
        super();
        this.response = response;
        this.expectedResponse = expectedResponse;
    }

    @Override
    public String toString() {
        return "Got: " + response + "\nExpected: " + String.join(" or ", expectedResponse);
    }
}
