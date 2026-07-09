package org.clem0908.sphinxbuildgui;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnsiTextArea extends ScrollPane {

    private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[([0-9;]*)([mK])");

    private final TextFlow textFlow = new TextFlow();
    private final Path selectionHighlight = new Path();
    private final StringBuilder plainText = new StringBuilder();
    private Color currentFg = Color.web("#f8f8f2");
    private boolean bold = false;

    private int selAnchor = -1;
    private int selStart = -1;
    private int selEnd = -1;

    public AnsiTextArea() {
        ResourceBundle messages = ResourceBundle.getBundle("org.clem0908.sphinxbuildgui.MessagesBundle", Locale.getDefault());

        textFlow.setStyle("-fx-background-color: transparent; -fx-padding: 5;");

        selectionHighlight.setFill(Color.web("#264f78"));
        selectionHighlight.setStroke(null);
        selectionHighlight.setManaged(false);

        StackPane stack = new StackPane(selectionHighlight, textFlow);
        stack.setAlignment(Pos.TOP_LEFT);
        stack.setStyle("-fx-background-color: #1e1e1e;");
        stack.setCursor(Cursor.TEXT);

        setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");
        setContent(stack);
        setFitToWidth(true);
        setFocusTraversable(true);
        textFlow.heightProperty().addListener((obs, old, val) -> setVvalue(1.0));
        // Re-anchor the highlight when the text reflows (window resize)
        textFlow.layoutBoundsProperty().addListener((obs, old, val) -> updateHighlight());

        stack.setOnMousePressed(e -> {
            requestFocus();
            if (e.getButton() == MouseButton.PRIMARY) {
                selAnchor = textFlow.hitTest(new Point2D(e.getX(), e.getY())).getInsertionIndex();
                setSelection(selAnchor, selAnchor);
            }
        });
        stack.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY && selAnchor >= 0) {
                int idx = textFlow.hitTest(new Point2D(e.getX(), e.getY())).getInsertionIndex();
                setSelection(Math.min(selAnchor, idx), Math.max(selAnchor, idx));
            }
        });

        MenuItem copyItem = new MenuItem(messages.getString("copy"));
        copyItem.setOnAction(e -> copySelection());
        ContextMenu contextMenu = new ContextMenu(copyItem);
        stack.setOnContextMenuRequested(e -> {
            copyItem.setDisable(selEnd <= selStart);
            contextMenu.show(stack, e.getScreenX(), e.getScreenY());
        });

        KeyCombination copyCombo = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);
        setOnKeyPressed(e -> {
            if (copyCombo.match(e)) copySelection();
        });
    }

    /** No-op: AnsiTextArea is always read-only. */
    public void setEditable(boolean editable) {}

    public void clear() {
        textFlow.getChildren().clear();
        plainText.setLength(0);
        selAnchor = -1;
        setSelection(-1, -1);
        currentFg = Color.web("#f8f8f2");
        bold = false;
    }

    public void appendText(String raw) {
        Matcher m = ANSI_PATTERN.matcher(raw);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) {
                addSegment(raw.substring(last, m.start()));
            }
            if ("m".equals(m.group(2))) {
                applyCode(m.group(1));
            }
            last = m.end();
        }
        if (last < raw.length()) {
            addSegment(raw.substring(last));
        }
    }

    private void setSelection(int start, int end) {
        selStart = start;
        selEnd = end;
        updateHighlight();
    }

    private void updateHighlight() {
        if (selEnd > selStart && selStart >= 0) {
            selectionHighlight.getElements().setAll(textFlow.rangeShape(selStart, selEnd));
        } else {
            selectionHighlight.getElements().clear();
        }
    }

    private void copySelection() {
        if (selEnd > selStart && selEnd <= plainText.length()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(plainText.substring(selStart, selEnd));
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    private void addSegment(String content) {
        if (content.isEmpty()) return;
        Text t = new Text(content);
        t.setFill(currentFg);
        t.setFont(Font.font("Monospace", bold ? FontWeight.BOLD : FontWeight.NORMAL, 12));
        textFlow.getChildren().add(t);
        plainText.append(content);
    }

    private void applyCode(String codes) {
        if (codes.isEmpty() || "0".equals(codes)) {
            currentFg = Color.web("#f8f8f2");
            bold = false;
            return;
        }
        for (String part : codes.split(";")) {
            int code;
            try { code = Integer.parseInt(part.trim()); } catch (NumberFormatException e) { continue; }
            switch (code) {
                case 0:  currentFg = Color.web("#f8f8f2"); bold = false; break;
                case 1:  bold = true;  break;
                case 22: bold = false; break;
                case 30: currentFg = Color.web("#555555"); break;
                case 31: currentFg = Color.web("#ff5555"); break;
                case 32: currentFg = Color.web("#50fa7b"); break;
                case 33: currentFg = Color.web("#f1fa8c"); break;
                case 34: currentFg = Color.web("#6272a4"); break;
                case 35: currentFg = Color.web("#ff79c6"); break;
                case 36: currentFg = Color.web("#8be9fd"); break;
                case 37: currentFg = Color.web("#f8f8f2"); break;
                case 90: currentFg = Color.web("#6272a4"); break;
                case 91: currentFg = Color.web("#ff6e6e"); break;
                case 92: currentFg = Color.web("#69ff94"); break;
                case 93: currentFg = Color.web("#ffffa5"); break;
                case 94: currentFg = Color.web("#d6acff"); break;
                case 95: currentFg = Color.web("#ff92df"); break;
                case 96: currentFg = Color.web("#a4ffff"); break;
                case 97: currentFg = Color.web("#ffffff"); break;
            }
        }
    }
}
