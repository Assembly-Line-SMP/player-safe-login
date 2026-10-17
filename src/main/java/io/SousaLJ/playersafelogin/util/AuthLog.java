package io.SousaLJ.playersafelogin.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuthLog {

    private static final Path LOG_FILE = Path.of("./safelogin/", "player_safe_login_failures.log");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logFailedAttempt(String playerName, String uuid, String ip) {
        try {
            Files.createDirectories(LOG_FILE.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(LOG_FILE, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                String timestamp = LocalDateTime.now().format(TIME_FORMAT);
                writer.write("[" + timestamp + "] " +
                        "Nome: " + playerName + " | UUID: " + uuid + " | IP: " + ip);
                writer.newLine();
            }
        } catch (IOException e) {
            // fallback para log padrão do mod
            io.SousaLJ.playersafelogin.PlayerSafeLogin.LOGGER.error("Failed to write login attempt to log: ", e);
        }
    }
}

