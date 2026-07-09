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

import java.util.prefs.Preferences;

public class Controller {

    private VBox root = new VBox(10);
    private TextField directoryField = new TextField();
    private TextField templateDirectoryField = new TextField();
    private AnsiTextArea terminalArea = new AnsiTextArea();
    private TextArea versionStatus;
    private TextArea templateDirVersionStatus;
    private Button checkVersionBtn;
    private Button checkTemplateDirVersionBtn;
    private Button openRstcheckLogBtn;
    private ResourceBundle messages;

    // Gestion des derniers dossiers ouverts
    private static final String PREF_DOC_DIR = "documentationDirectory";
    private static final String PREF_TEMPLATE_DIR = "templateDirectory";
    private final Preferences prefs = Preferences.userNodeForPackage(Controller.class);

    private Stage stage;

    public Controller(Stage stage) {
	Locale currentLocale;
	currentLocale = Locale.getDefault();
	this.messages = ResourceBundle.getBundle("org.clem0908.sphinxbuildgui.MessagesBundle", currentLocale);
	this.versionStatus = new TextArea();
	this.versionStatus.setEditable(false);
	this.versionStatus.setWrapText(false);
	this.versionStatus.setPrefHeight(150);
	this.versionStatus.setVisible(false);
	this.versionStatus.setManaged(false);
	this.templateDirVersionStatus = new TextArea();
	this.templateDirVersionStatus.setEditable(false);
	this.templateDirVersionStatus.setWrapText(false);
	this.templateDirVersionStatus.setPrefHeight(150);
	this.templateDirVersionStatus.setVisible(false);
	this.templateDirVersionStatus.setManaged(false);

        this.stage = stage;

	directoryField.setText(prefs.get(PREF_DOC_DIR, ""));
	templateDirectoryField.setText(prefs.get(PREF_TEMPLATE_DIR, ""));

        buildUI();
    }

    public Parent getRoot() {
        return root;
    }

    private ResourceBundle getMessages() {
	return this.messages;
    }

    private void buildUI() {

        root.setPadding(Insets.EMPTY);

        // Header
        String version = Main.class.getPackage().getImplementationVersion();
        Label headerLabel = new Label("Sphinx Build GUI" + (version != null ? "  " + version : ""));
        headerLabel.getStyleClass().add("app-header");
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        HBox header = new HBox(headerLabel);
        header.getStyleClass().add("app-header-bar");
        HBox.setHgrow(headerLabel, javafx.scene.layout.Priority.ALWAYS);

        // Documentation directory selection
        Button browseBtn = new Button(this.getMessages().getString("changeDocumentationDirectoryButton"));
        browseBtn.setOnAction(e -> chooseDirectory());
        browseBtn.setMinWidth(Region.USE_PREF_SIZE);
        directoryField.setPrefWidth(600);
        HBox dirBox = new HBox(10, directoryField, browseBtn);
        HBox.setHgrow(directoryField, Priority.ALWAYS);

        // Documentation template check
        checkVersionBtn = new Button(this.getMessages().getString("checkTemplateButton"));
        checkVersionBtn.setOnAction(e -> toggleVersion());

        // Template directory selection
        Button browseTemplateBtn = new Button(this.getMessages().getString("changeTemplateFolderButton"));
        browseTemplateBtn.setOnAction(e -> chooseTemplateDirectory());
        browseTemplateBtn.setMinWidth(Region.USE_PREF_SIZE);
        templateDirectoryField.setPrefWidth(600);
        HBox templateDirBox = new HBox(10, templateDirectoryField, browseTemplateBtn);
        HBox.setHgrow(templateDirectoryField, Priority.ALWAYS);

        // Template directory version check
        checkTemplateDirVersionBtn = new Button(this.getMessages().getString("checkTemplateFolderVersionButton"));
        checkTemplateDirVersionBtn.setOnAction(e -> toggleTemplateDirVersion());

        // Zone Documentation directory
        VBox docContent = new VBox(6, dirBox, checkVersionBtn, versionStatus);
        TitledPane docPane = new TitledPane(this.getMessages().getString("zoneDocTitle"), docContent);
        docPane.setCollapsible(false);

        // Zone Template folder
        VBox templateContent = new VBox(6, templateDirBox, checkTemplateDirVersionBtn, templateDirVersionStatus);
        TitledPane templatePane = new TitledPane(this.getMessages().getString("zoneTemplateTitle"), templateContent);
        templatePane.setCollapsible(false);

        // Left column: directory controls + template version controls
        VBox leftBox = new VBox(10, docPane, templatePane);

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
            new HBox(10, openHtmlFrBtn, openPdfFrBtn)
        );
        TitledPane frPane = new TitledPane(this.getMessages().getString("zoneFrTitle"), frContent);
        frPane.setCollapsible(false);

        // Zone Debugging FR
        Button openWarningHtmlFrLogBtn = new Button(this.getMessages().getString("openWarningHtmlFrLog"));
        openWarningHtmlFrLogBtn.setOnAction(e -> makeAndOpenWarningLog("warning-html-fr.log"));

        Button openWarningPdfFrLogBtn = new Button(this.getMessages().getString("openWarningPdfFrLog"));
        openWarningPdfFrLogBtn.setOnAction(e -> makeAndOpenWarningLog("warning-pdf-fr.log"));

        openRstcheckLogBtn = new Button(this.getMessages().getString("openRstcheckLog"));
        openRstcheckLogBtn.setOnAction(e -> openFileWithDesktop("rstcheck.log"));
        openRstcheckLogBtn.setDisable(true);

        VBox frDebugContent = new VBox(6, openWarningHtmlFrLogBtn, openWarningPdfFrLogBtn, openRstcheckLogBtn);
        TitledPane frDebugPane = new TitledPane(this.getMessages().getString("zoneDebugTitle"), frDebugContent);
        frDebugPane.setCollapsible(false);

        // Zone PO/POT: pot, po
        ToggleGroup poGroup = new ToggleGroup();
        RadioButton poPot = new RadioButton("pot");
        RadioButton poPo = new RadioButton("po");
        poPot.setToggleGroup(poGroup);
        poPo.setToggleGroup(poGroup);
        poPot.setSelected(true);
        Button poBuildBtn = new Button(this.getMessages().getString("generateButton"));
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

        // Zone Debugging PO/POT
        Button openWarningPotLogBtn = new Button(this.getMessages().getString("openWarningPotLog"));
        openWarningPotLogBtn.setOnAction(e -> makeAndOpenWarningLog("warning-pot.log"));

        Button checkPoBtn = new Button(this.getMessages().getString("checkPoButton"));
        checkPoBtn.setOnAction(e -> buildWithTarget("check-po"));

        VBox poDebugContent = new VBox(6, openWarningPotLogBtn, checkPoBtn);
        TitledPane poDebugPane = new TitledPane(this.getMessages().getString("zoneDebugTitle"), poDebugContent);
        poDebugPane.setCollapsible(false);

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
            new HBox(10, openHtmlEnBtn, openPdfEnBtn)
        );
        TitledPane enPane = new TitledPane(this.getMessages().getString("zoneEnTitle"), enContent);
        enPane.setCollapsible(false);

        // Zone Debugging EN
        Button openWarningHtmlEnLogBtn = new Button(this.getMessages().getString("openWarningHtmlEnLog"));
        openWarningHtmlEnLogBtn.setOnAction(e -> makeAndOpenWarningLog("warning-html-en.log"));

        Button openWarningPdfEnLogBtn = new Button(this.getMessages().getString("openWarningPdfEnLog"));
        openWarningPdfEnLogBtn.setOnAction(e -> makeAndOpenWarningLog("warning-pdf-en.log"));

        VBox enDebugContent = new VBox(6, openWarningHtmlEnLogBtn, openWarningPdfEnLogBtn);
        TitledPane enDebugPane = new TitledPane(this.getMessages().getString("zoneDebugTitle"), enDebugContent);
        enDebugPane.setCollapsible(false);

        // Right column: publication panes (col 0) + debug panes (col 1), aligned per row
        GridPane rightBox = new GridPane();
        rightBox.setHgap(10);
        rightBox.setVgap(10);
        frPane.setMaxWidth(Double.MAX_VALUE);
        poPane.setMaxWidth(Double.MAX_VALUE);
        enPane.setMaxWidth(Double.MAX_VALUE);
        frDebugPane.setMaxWidth(Double.MAX_VALUE);
        poDebugPane.setMaxWidth(Double.MAX_VALUE);
        enDebugPane.setMaxWidth(Double.MAX_VALUE);
        rightBox.add(frPane, 0, 0);
        rightBox.add(frDebugPane, 1, 0);
        rightBox.add(poPane, 0, 1);
        rightBox.add(poDebugPane, 1, 1);
        rightBox.add(enPane, 0, 2);
        rightBox.add(enDebugPane, 1, 2);

        HBox mainContent = new HBox(20, leftBox, rightBox);

        // Terminal
        terminalArea.setEditable(false);
        terminalArea.setPrefHeight(350);

        // Bottom bar
        Button quitBtn = new Button(this.getMessages().getString("exit"));
        quitBtn.setOnAction(e -> Platform.exit());

        HBox openBox = new HBox(10, quitBtn);

        VBox body = new VBox(10, mainContent, terminalArea, openBox);
        body.setPadding(new Insets(10));

        root.getChildren().addAll(header, body);
    }

    private void refreshDebugButtons() {
        String dir = directoryField.getText();
        openRstcheckLogBtn.setDisable(!isNonEmpty(dir, "rstcheck.log"));
    }

    private boolean isNonEmpty(String dir, String relativePath) {
        if (dir == null || dir.isEmpty()) return false;
        File f = new File(dir, relativePath);
        return f.exists() && f.length() > 0;
    }

    private void chooseDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
	if (!prefs.get(PREF_DOC_DIR, "").isEmpty()) {
		File f = new File(prefs.get(PREF_DOC_DIR, ""));
		if (f.isDirectory()) chooser.setInitialDirectory(f);
	}
        chooser.setTitle(this.getMessages().getString("changeDocumentationDirectoryButton"));
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            directoryField.setText(dir.getAbsolutePath());
	    prefs.put(PREF_DOC_DIR, dir.getAbsolutePath());
        }
    }

    private void chooseTemplateDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
	if (!prefs.get(PREF_TEMPLATE_DIR, "").isEmpty()) {
		File f = new File(prefs.get(PREF_TEMPLATE_DIR, ""));
		if (f.isDirectory()) chooser.setInitialDirectory(f);
	}
        chooser.setTitle(this.getMessages().getString("changeTemplateFolderButton"));
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            templateDirectoryField.setText(dir.getAbsolutePath());
	    prefs.put(PREF_TEMPLATE_DIR, dir.getAbsolutePath());
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
        BuildExecutor.executeBuild(dir, target, terminalArea, this::refreshDebugButtons);
    }

    private void makeAndOpenWarningLog(String logName) {
        String dir = directoryField.getText();
        if (dir == null || dir.isEmpty()) {
            terminalArea.appendText(this.getMessages().getString("selectDocumentationDirectory"));
            return;
        }
        BuildExecutor.executeBuild(dir, logName, terminalArea, () -> {
            refreshDebugButtons();
            openFileWithDesktop(logName);
        });
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
