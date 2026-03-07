package org.clem0908.sphinxbuildgui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Locale;
import java.util.ResourceBundle;

public class Controller {

    private VBox root = new VBox(10);
    private TextField directoryField = new TextField();
    private TextArea terminalArea = new TextArea();
    private ComboBox<String> targetSelector = new ComboBox<>();
    private Label versionStatus;
    private ResourceBundle messages;

    private Stage stage;

    public Controller(Stage stage) {
	Locale currentLocale;
	currentLocale = Locale.getDefault();
	this.messages = ResourceBundle.getBundle("org.clem0908.sphinxbuildgui.MessagesBundle", currentLocale);
	this.versionStatus = new Label(this.messages.getString("templateVersionUnknownText"));

        this.stage = stage;
        buildUI();
    }

    public Parent getRoot() {
        return root;
    }

    private ResourceBundle getMessages() {
	return this.messages;
    }

    private void buildUI() {

        root.setPadding(new Insets(10));

        // Directory selection
        Button browseBtn = new Button(this.getMessages().getString("changeDocumentationDirectoryButton"));
        browseBtn.setOnAction(e -> chooseDirectory());

        directoryField.setPrefWidth(600);

        HBox dirBox = new HBox(10, directoryField, browseBtn);

        // Template check
        Button checkVersionBtn = new Button(this.getMessages().getString("checkTemplateButton"));
        checkVersionBtn.setOnAction(e -> checkVersion());

        // Build targets
	targetSelector.getItems().addAll(
		"html-fr",
		"html-en",
		"pdf-fr",
		"pdf-en",
		"pdf-fr-fast",
		"pdf-en-fast",
		"pot",
		"po"
	);
	targetSelector.setValue("html-fr");	

        Button buildBtn = new Button(this.getMessages().getString("buildButton"));
        buildBtn.setOnAction(e -> build());

	HBox buildBox = new HBox(10,
		new Label(this.getMessages().getString("action")), targetSelector,
		buildBtn);
        // Terminal
        terminalArea.setEditable(false);
        terminalArea.setPrefHeight(350);

        // Open buttons
        Button openHtmlBtn = new Button(this.getMessages().getString("openHTML"));
        openHtmlBtn.setOnAction(e -> openHtml());

        Button openPdfBtn = new Button(this.getMessages().getString("openPDF"));
        openPdfBtn.setOnAction(e -> openPdf());

        Button quitBtn = new Button(this.getMessages().getString("exit"));
        quitBtn.setOnAction(e -> Platform.exit());

        HBox openBox = new HBox(10, openHtmlBtn, openPdfBtn, quitBtn);

        root.getChildren().addAll(
                dirBox,
                checkVersionBtn,
                versionStatus,
                buildBox,
                terminalArea,
                openBox
        );
    }

    private void chooseDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            directoryField.setText(dir.getAbsolutePath());
        }
    }

    private void checkVersion() {
        String dir = directoryField.getText();
        String result = VersionChecker.checkTemplateVersion(dir);
	versionStatus.setText(result);
    }

    private void build() {
        String dir = directoryField.getText();
        String target = targetSelector.getValue();
        BuildExecutor.executeBuild(dir, target, terminalArea);
    }

private void openHtml() {
    String dir = directoryField.getText();
    if (dir == null || dir.isEmpty()) {
        terminalArea.appendText("Please select a documentation directory.\n");
        return;
    }

    new Thread(() -> {
        try {
            ProcessBuilder pb = new ProcessBuilder("make", "open-html");
            pb.directory(new File(dir));
            pb.redirectErrorStream(true); // fusion stdout/stderr
            Process process = pb.start();

            // Lire la sortie du process et l’afficher dans le terminal
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String msg = line + "\n";
                    javafx.application.Platform.runLater(() -> terminalArea.appendText(msg));
                }
            }

            process.waitFor();
            javafx.application.Platform.runLater(() ->
                    terminalArea.appendText("Finished: make open-html\n"));

        } catch (Exception e) {
            javafx.application.Platform.runLater(() ->
                    terminalArea.appendText("Error running make open-html: " + e.getMessage() + "\n"));
        }
    }).start();
}

private void openPdf() {
    String dir = directoryField.getText();
    if (dir == null || dir.isEmpty()) {
        terminalArea.appendText("Please select a documentation directory.\n");
        return;
    }

    new Thread(() -> {
        try {
            ProcessBuilder pb = new ProcessBuilder("make", "open-pdf");
            pb.directory(new File(dir));
            pb.redirectErrorStream(true); // fusion stdout/stderr
            Process process = pb.start();

            // Lire la sortie du process et l’afficher dans le terminal
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String msg = line + "\n";
                    javafx.application.Platform.runLater(() -> terminalArea.appendText(msg));
                }
            }

            process.waitFor();
            javafx.application.Platform.runLater(() ->
                    terminalArea.appendText("Finished: make open-pdf\n"));

        } catch (Exception e) {
            javafx.application.Platform.runLater(() ->
                    terminalArea.appendText("Error running make open-pdf: " + e.getMessage() + "\n"));
        }
    }).start();
}

}
