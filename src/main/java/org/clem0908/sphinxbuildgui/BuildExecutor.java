package org.clem0908.sphinxbuildgui;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.*;

public class BuildExecutor {

    public static void executeBuild(String dir, String target, TextArea terminal) {

        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("make", target);
                pb.directory(new File(dir));
                pb.redirectErrorStream(true);
                Process process = pb.start();

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(() ->
                            terminal.appendText(finalLine + "\n"));
                }

                process.waitFor();

            } catch (Exception e) {
                Platform.runLater(() ->
                        terminal.appendText("Build failed: " + e.getMessage() + "\n"));
            }
        }).start();
    }
}
