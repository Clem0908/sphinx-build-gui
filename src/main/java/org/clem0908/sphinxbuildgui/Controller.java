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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Locale;
import java.util.ResourceBundle;

public class Controller {

    private VBox root = new VBox(10);
    private TextField directoryField = new TextField();
    private TextField templateDirectoryField = new TextField();
    private AnsiTextArea terminalArea = new AnsiTextArea();
    private ComboBox<String> targetSelector = new ComboBox<>();
    private Label versionStatus;
    private Label templateDirVersionStatus;
    private Button checkVersionBtn;
    private Button checkTemplateDirVersionBtn;
    private ResourceBundle messages;

    private Stage stage;

    public Controller(Stage stage) {
	Locale currentLocale;
	currentLocale = Locale.getDefault();
	this.messages = ResourceBundle.getBundle("org.clem0908.sphinxbuildgui.MessagesBundle", currentLocale);
	this.versionStatus = new Label(this.messages.getString("templateVersionUnknownText"));
	this.versionStatus.setVisible(false);
	this.versionStatus.setManaged(false);
	this.templateDirVersionStatus = new Label(this.messages.getString("templateVersionUnknownText"));
	this.templateDirVersionStatus.setVisible(false);
	this.templateDirVersionStatus.setManaged(false);

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

        // Documentation directory selection
        Button browseBtn = new Button(this.getMessages().getString("changeDocumentationDirectoryButton"));
        browseBtn.setOnAction(e -> chooseDirectory());

        directoryField.setPrefWidth(600);

        HBox dirBox = new HBox(10, directoryField, browseBtn);

        // Documentation template check
        checkVersionBtn = new Button(this.getMessages().getString("checkTemplateButton"));
        checkVersionBtn.setOnAction(e -> toggleVersion());

        // Template directory selection
        Button browseTemplateBtn = new Button(this.getMessages().getString("changeTemplateFolderButton"));
        browseTemplateBtn.setOnAction(e -> chooseTemplateDirectory());

        templateDirectoryField.setPrefWidth(600);

        HBox templateDirBox = new HBox(10, templateDirectoryField, browseTemplateBtn);

        // Template directory version check
        checkTemplateDirVersionBtn = new Button(this.getMessages().getString("checkTemplateFolderVersionButton"));
        checkTemplateDirVersionBtn.setOnAction(e -> toggleTemplateDirVersion());

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

        Button openWarningLogBtn = new Button(this.getMessages().getString("openWarningLog"));
        openWarningLogBtn.setOnAction(e -> openFileWithDesktop("warning.log"));

        Button openRstcheckLogBtn = new Button(this.getMessages().getString("openRstcheckLog"));
        openRstcheckLogBtn.setOnAction(e -> openFileWithDesktop("rstcheck.log"));

        Button openDocsPoBtn = new Button(this.getMessages().getString("openDocsPo"));
        openDocsPoBtn.setOnAction(e -> openFileWithDesktop("source/locale/en/LC_MESSAGES/docs.po"));

        Button quitBtn = new Button(this.getMessages().getString("exit"));
        quitBtn.setOnAction(e -> Platform.exit());

        HBox openBox = new HBox(10, openHtmlBtn, openPdfBtn, openWarningLogBtn, openRstcheckLogBtn, openDocsPoBtn, quitBtn);

        root.getChildren().addAll(
                dirBox,
                checkVersionBtn,
                versionStatus,
                templateDirBox,
                checkTemplateDirVersionBtn,
                templateDirVersionStatus,
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

    private void chooseTemplateDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            templateDirectoryField.setText(dir.getAbsolutePath());
        }
    }

    private void toggleVersion() {
        if (versionStatus.isVisible()) {
            versionStatus.setVisible(false);
            versionStatus.setManaged(false);
            checkVersionBtn.setText(this.getMessages().getString("checkTemplateButton"));
        } else {
            String result = VersionChecker.checkTemplateVersion(directoryField.getText());
            versionStatus.setText(result);
            versionStatus.setVisible(true);
            versionStatus.setManaged(true);
            checkVersionBtn.setText(this.getMessages().getString("hideTemplateButton"));
        }
    }

    private void toggleTemplateDirVersion() {
        if (templateDirVersionStatus.isVisible()) {
            templateDirVersionStatus.setVisible(false);
            templateDirVersionStatus.setManaged(false);
            checkTemplateDirVersionBtn.setText(this.getMessages().getString("checkTemplateFolderVersionButton"));
        } else {
            String result = VersionChecker.checkTemplateVersion(templateDirectoryField.getText());
            templateDirVersionStatus.setText(result);
            templateDirVersionStatus.setVisible(true);
            templateDirVersionStatus.setManaged(true);
            checkTemplateDirVersionBtn.setText(this.getMessages().getString("hideTemplateFolderVersionButton"));
        }
    }

    private void build() {
        String dir = directoryField.getText();
        String target = targetSelector.getValue();
        BuildExecutor.executeBuild(dir, target, terminalArea);
    }

private void openHtml() {
    String dir = directoryField.getText();
    if (dir == null || dir.isEmpty()) {
        terminalArea.appendText(this.getMessages().getString("selectDocumentationDirectory"));
        return;
    }

    new Thread(() -> {
        try {
            Path buildDir = Paths.get(dir, "build");
            java.util.List<Path> targets = Files.walk(buildDir, 3)
                .filter(p -> p.getFileName().toString().equals("index.html")
                          && p.getParent().getFileName().toString().equals("html")
                          && p.getParent().getParent().getFileName().toString().matches("[a-z]{2}"))
                .collect(java.util.stream.Collectors.toList());
            if (targets.isEmpty()) {
                Platform.runLater(() -> terminalArea.appendText(
                    this.getMessages().getString("fileNotFound") + " build/*/html/index.html\n"));
                return;
            }
            for (Path t : targets)
                new ProcessBuilder("xdg-open", t.toString())
                    .redirectErrorStream(true).start().waitFor();
        } catch (Exception e) {
            Platform.runLater(() ->
                    terminalArea.appendText(this.getMessages().getString("errorWebBrowser") + e.getMessage() + "\n"));
        }
    }).start();
}

private void openPdf() {
    String dir = directoryField.getText();
    if (dir == null || dir.isEmpty()) {
        terminalArea.appendText(this.getMessages().getString("selectDocumentationDirectory"));
        return;
    }

    new Thread(() -> {
        try {
            Path buildDir = Paths.get(dir, "build");
            java.util.List<Path> targets = Files.walk(buildDir, 3)
                .filter(p -> p.getFileName().toString().endsWith(".pdf")
                          && p.getParent().getFileName().toString().equals("latex")
                          && p.getParent().getParent().getFileName().toString().matches("[a-z]{2}"))
                .collect(java.util.stream.Collectors.toList());
            if (targets.isEmpty()) {
                Platform.runLater(() -> terminalArea.appendText(
                    this.getMessages().getString("fileNotFound") + " build/*/latex/*.pdf\n"));
                return;
            }
            java.util.List<Process> procs = new java.util.ArrayList<>();
            for (Path t : targets)
                procs.add(new ProcessBuilder("xdg-open", t.toString())
                    .redirectErrorStream(true).start());
            for (Process p : procs) p.waitFor();
        } catch (Exception e) {
            Platform.runLater(() ->
                    terminalArea.appendText(this.getMessages().getString("errorPDFViewer") + e.getMessage() + "\n"));
        }
    }).start();
}

private void openFileWithDesktop(String relativePath) {
    String dir = directoryField.getText();
    if (dir == null || dir.isEmpty()) {
        terminalArea.appendText(this.getMessages().getString("selectDocumentationDirectory"));
        return;
    }

    File file = new File(dir, relativePath);
    if (!file.exists()) {
        terminalArea.appendText(this.getMessages().getString("fileNotFound") + " " + file.getAbsolutePath() + "\n");
        return;
    }

    new Thread(() -> {
        try {
            ProcessBuilder pb = new ProcessBuilder("xdg-open", file.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            javafx.application.Platform.runLater(() ->
                terminalArea.appendText(this.getMessages().getString("errorOpeningFile") + " " + e.getMessage() + "\n"));
        }
    }).start();
}

}
