package io.SousaLJ.playersafelogin.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.security.MessageDigest;

public class FileStorage implements StorageProvider {

    private final Path storagePath = Paths.get("./safelogin/passwords/");

    public FileStorage() {
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory", e);
        }
    }

    private Path getPlayerFile(UUID playerId) {
        return storagePath.resolve(playerId + ".dat");
    }

    @Override
    public boolean setupPassword(UUID playerId, String hashedPassword) {
        try {
            Files.writeString(getPlayerFile(playerId), hashedPassword,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean validatePassword(UUID playerId, String hashedPassword) {
        try {
            String storedHash = Files.readString(getPlayerFile(playerId));
            return MessageDigest.isEqual(storedHash.getBytes(), hashedPassword.getBytes());
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean resetPassword(UUID playerId) {
        try {
            return Files.deleteIfExists(getPlayerFile(playerId));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean requiresInitialSetup(UUID playerId) {
        return !Files.exists(getPlayerFile(playerId));
    }
}
