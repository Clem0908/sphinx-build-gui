package com.example.sphinxgui;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Controller controller = new Controller(stage);
        Scene scene = new Scene(controller.getRoot(), 1000, 700);

        stage.setTitle("Sphinx Documentation Builder GUI");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
