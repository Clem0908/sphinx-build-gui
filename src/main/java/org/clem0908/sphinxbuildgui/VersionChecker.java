package org.clem0908.sphinxbuildgui;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class VersionChecker {

    private static final String[] FILES = {
            "Makefile",
            "source/conf-fr.py",
            "source/conf-en.py",
            "source/pyproject.toml",
            "source/_static/style.css",
            "source/_static/css/theme.css",
            "source/_static/js/add_language_selector.js",
            "source/_static/js/default_dark.js",
            "source/_static/js/default_light.js",
            "source/_static/js/theme_switcher.js",
            "source/_templates/footer.html"
    };

    public static String checkTemplateVersion(String baseDir) {

        Set<String> versions = new HashSet<>();

        for (String path : FILES) {
            try {
                Path p = Paths.get(baseDir, path);
                List<String> lines = Files.readAllLines(p);
                if (!lines.isEmpty()) {
                    versions.add(String.join(": ", path, lines.get(0).trim()));
                }
            } catch (Exception e) {
                return "Missing or unreadable: " + path;
            }
        }

        return String.join("\n", versions);
    }
}
