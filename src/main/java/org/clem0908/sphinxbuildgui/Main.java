package org.clem0908.sphinxbuildgui;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Controller controller = new Controller(stage);
        Scene scene = new Scene(controller.getRoot(), 1000, 700);

        String version = Main.class.getPackage().getImplementationVersion();
        stage.setTitle("Sphinx Build GUI" + (version != null ? " " + version : ""));
        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
