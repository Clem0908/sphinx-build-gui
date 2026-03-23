package org.clem0908.sphinxbuildgui;

import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnsiTextArea extends ScrollPane {

    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[([0-9;]*)([mK])");

    private final TextFlow textFlow = new TextFlow();
    private Color currentFg = Color.web("#f8f8f2");
    private boolean bold = false;

    public AnsiTextArea() {
        textFlow.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 5;");
        setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");
        setContent(textFlow);
        setFitToWidth(true);
        textFlow.heightProperty().addListener((obs, old, val) -> setVvalue(1.0));
    }

    /** No-op: AnsiTextArea is always read-only. */
    public void setEditable(boolean editable) {}

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

    private void addSegment(String content) {
        if (content.isEmpty()) return;
        Text t = new Text(content);
        t.setFill(currentFg);
        t.setFont(Font.font("Monospace", bold ? FontWeight.BOLD : FontWeight.NORMAL, 12));
        textFlow.getChildren().add(t);
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
