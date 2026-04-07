package org.clem0908.sphinxbuildgui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
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

        // Left column: directory controls + template version controls
        VBox leftBox = new VBox(10,
            dirBox,
            checkVersionBtn,
            versionStatus,
            templateDirBox,
            checkTemplateDirVersionBtn,
            templateDirVersionStatus
        );

        // Zone FR: html-fr, pdf-fr, pdf-fr-fast
        ToggleGroup frGroup = new ToggleGroup();
        RadioButton frHtml = new RadioButton("html-fr");
        RadioButton frPdf = new RadioButton("pdf-fr");
        RadioButton frPdfFast = new RadioButton("pdf-fr-fast");
        frHtml.setToggleGroup(frGroup);
        frPdf.setToggleGroup(frGroup);
        frPdfFast.setToggleGroup(frGroup);
        frHtml.setSelected(true);
        Button frBuildBtn = new Button(this.getMessages().getString("buildButton"));
        frBuildBtn.setOnAction(e -> {
            RadioButton sel = (RadioButton) frGroup.getSelectedToggle();
            if (sel != null) buildWithTarget(sel.getText());
        });
        Button openHtmlFrBtn = new Button(this.getMessages().getString("openHTMLFr"));
        openHtmlFrBtn.setOnAction(e -> openHtmlLocale("fr"));
        Button openPdfFrBtn = new Button(this.getMessages().getString("openPDFFr"));
        openPdfFrBtn.setOnAction(e -> openPdfLocale("fr"));
        VBox frContent = new VBox(6,
            new HBox(10, new Label(this.getMessages().getString("action")), frHtml, frPdf, frPdfFast, frBuildBtn),
            openHtmlFrBtn,
            openPdfFrBtn
        );
        TitledPane frPane = new TitledPane(this.getMessages().getString("zoneFrTitle"), frContent);
        frPane.setCollapsible(false);

        // Zone PO/POT: pot, po
        ToggleGroup poGroup = new ToggleGroup();
        RadioButton poPot = new RadioButton("pot");
        RadioButton poPo = new RadioButton("po");
        poPot.setToggleGroup(poGroup);
        poPo.setToggleGroup(poGroup);
        poPot.setSelected(true);
        Button poBuildBtn = new Button(this.getMessages().getString("buildButton"));
        poBuildBtn.setOnAction(e -> {
            RadioButton sel = (RadioButton) poGroup.getSelectedToggle();
            if (sel != null) buildWithTarget(sel.getText());
        });
        Button openDocsPoBtn = new Button(this.getMessages().getString("openDocsPo"));
        openDocsPoBtn.setOnAction(e -> openFileWithDesktop("source/locale/en/LC_MESSAGES/docs.po"));
        VBox poContent = new VBox(6,
            new HBox(10, new Label(this.getMessages().getString("action")), poPot, poPo, poBuildBtn),
            openDocsPoBtn
        );
        TitledPane poPane = new TitledPane(this.getMessages().getString("zonePoTitle"), poContent);
        poPane.setCollapsible(false);

        // Zone EN: html-en, pdf-en, pdf-en-fast
        ToggleGroup enGroup = new ToggleGroup();
        RadioButton enHtml = new RadioButton("html-en");
        RadioButton enPdf = new RadioButton("pdf-en");
        RadioButton enPdfFast = new RadioButton("pdf-en-fast");
        enHtml.setToggleGroup(enGroup);
        enPdf.setToggleGroup(enGroup);
        enPdfFast.setToggleGroup(enGroup);
        enHtml.setSelected(true);
        Button enBuildBtn = new Button(this.getMessages().getString("buildButton"));
        enBuildBtn.setOnAction(e -> {
            RadioButton sel = (RadioButton) enGroup.getSelectedToggle();
            if (sel != null) buildWithTarget(sel.getText());
        });
        Button openHtmlEnBtn = new Button(this.getMessages().getString("openHTMLEn"));
        openHtmlEnBtn.setOnAction(e -> openHtmlLocale("en"));
        Button openPdfEnBtn = new Button(this.getMessages().getString("openPDFEn"));
        openPdfEnBtn.setOnAction(e -> openPdfLocale("en"));
        VBox enContent = new VBox(6,
            new HBox(10, new Label(this.getMessages().getString("action")), enHtml, enPdf, enPdfFast, enBuildBtn),
            openHtmlEnBtn,
            openPdfEnBtn
        );
        TitledPane enPane = new TitledPane(this.getMessages().getString("zoneEnTitle"), enContent);
        enPane.setCollapsible(false);

        VBox rightBox = new VBox(10, frPane, poPane, enPane);

        HBox mainContent = new HBox(20, leftBox, rightBox);

        // Terminal
        terminalArea.setEditable(false);
        terminalArea.setPrefHeight(350);

        // Bottom bar
        Button openHtmlBtn = new Button(this.getMessages().getString("openHTML"));
        openHtmlBtn.setOnAction(e -> openHtml());

        Button openPdfBtn = new Button(this.getMessages().getString("openPDF"));
        openPdfBtn.setOnAction(e -> openPdf());

        Button openWarningLogBtn = new Button(this.getMessages().getString("openWarningLog"));
        openWarningLogBtn.setOnAction(e -> openFileWithDesktop("warning.log"));

        Button openRstcheckLogBtn = new Button(this.getMessages().getString("openRstcheckLog"));
        openRstcheckLogBtn.setOnAction(e -> openFileWithDesktop("rstcheck.log"));

        Button quitBtn = new Button(this.getMessages().getString("exit"));
        quitBtn.setOnAction(e -> Platform.exit());

        HBox openBox = new HBox(10, openHtmlBtn, openPdfBtn, openWarningLogBtn, openRstcheckLogBtn, quitBtn);

        root.getChildren().addAll(
            mainContent,
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

    private void buildWithTarget(String target) {
        String dir = directoryField.getText();
        BuildExecutor.executeBuild(dir, target, terminalArea);
    }

    private void openHtmlLocale(String locale) {
        String dir = directoryField.getText();
        if (dir == null || dir.isEmpty()) {
            terminalArea.appendText(this.getMessages().getString("selectDocumentationDirectory"));
            return;
        }
        new Thread(() -> {
            Path target = Paths.get(dir, "build", locale, "html", "index.html");
            if (!Files.exists(target)) {
                Platform.runLater(() -> terminalArea.appendText(
                    this.getMessages().getString("fileNotFound") + " " + target + "\n"));
                return;
            }
            try {
                new ProcessBuilder("xdg-open", target.toString())
                    .redirectErrorStream(true).start().waitFor();
            } catch (Exception e) {
                Platform.runLater(() ->
                    terminalArea.appendText(this.getMessages().getString("errorWebBrowser") + e.getMessage() + "\n"));
            }
        }).start();
    }

    private void openPdfLocale(String locale) {
        String dir = directoryField.getText();
        if (dir == null || dir.isEmpty()) {
            terminalArea.appendText(this.getMessages().getString("selectDocumentationDirectory"));
            return;
        }
        new Thread(() -> {
            try {
                Path latexDir = Paths.get(dir, "build", locale, "latex");
                java.util.List<Path> targets = Files.walk(latexDir, 1)
                    .filter(p -> p.getFileName().toString().endsWith(".pdf"))
                    .collect(java.util.stream.Collectors.toList());
                if (targets.isEmpty()) {
                    Platform.runLater(() -> terminalArea.appendText(
                        this.getMessages().getString("fileNotFound") + " build/" + locale + "/latex/*.pdf\n"));
                    return;
                }
                for (Path t : targets)
                    new ProcessBuilder("xdg-open", t.toString())
                        .redirectErrorStream(true).start().waitFor();
            } catch (Exception e) {
                Platform.runLater(() ->
                    terminalArea.appendText(this.getMessages().getString("errorPDFViewer") + e.getMessage() + "\n"));
            }
        }).start();
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
