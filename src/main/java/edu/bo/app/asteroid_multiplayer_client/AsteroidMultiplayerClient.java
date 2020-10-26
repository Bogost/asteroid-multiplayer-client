package edu.bo.app.asteroid_multiplayer_client;

import edu.bo.app.asteroid_multiplayer_client.controller.AppController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AsteroidMultiplayerClient extends Application {

    AppController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/App.fxml"));
            Parent root = (Parent) loader.load();
            Scene scene = new Scene(root);

            controller = (AppController) loader.getController();
            controller.afterSceneInitialize(scene);

            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        controller.getConnector()
                  .disconnect();
    }

}
