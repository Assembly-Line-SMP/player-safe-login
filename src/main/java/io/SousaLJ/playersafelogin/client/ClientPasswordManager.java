package io.SousaLJ.playersafelogin.client;

import io.SousaLJ.playersafelogin.PlayerSafeLogin;
import io.SousaLJ.playersafelogin.util.SecurityConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.io.IOException;

/**
 * Responsible for managing the player's password on client side.
 */
public class ClientPasswordManager {

    private static final String FILE_NAME = "player_safelogin.dat";
    private static final String DIR_NAME = "PlayerSafeLogin";
    private static final Path LEGACY_FILE = Minecraft.getInstance().gameDirectory.toPath().resolve(FILE_NAME);
    private static final Path PASSWORD_FILE = resolvePasswordFile();

    /**
     * Picks an instance-independent, writable location. Order: XDG data dir (Flatpak remaps it to
     * a persistent per-app dir), platform default under user.home (also covers Pojav/Mojo on Android,
     * where user.home is the shared launcher home), then the game directory as a last resort.
     */
    private static Path resolvePasswordFile() {
        String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
        java.util.List<Path> bases = new java.util.ArrayList<>();
        String home = System.getProperty("user.home");
        if (os.contains("win")) {
            addBase(bases, System.getenv("APPDATA"));
            if (home != null) bases.add(Path.of(home, "AppData", "Roaming"));
        } else if (os.contains("mac")) {
            if (home != null) bases.add(Path.of(home, "Library", "Application Support"));
        } else {
            addBase(bases, System.getenv("XDG_DATA_HOME"));
            if (home != null) bases.add(Path.of(home, ".local", "share"));
        }
        for (Path base : bases) {
            try {
                Path dir = base.resolve(DIR_NAME);
                Files.createDirectories(dir);
                if (Files.isWritable(dir)) {
                    Path file = dir.resolve(FILE_NAME);
                    migrateLegacy(file);
                    return file;
                }
            } catch (IOException | RuntimeException ignored) {
            }
        }
        PlayerSafeLogin.LOGGER.warn("No instance-independent directory available, using the game directory.");
        return LEGACY_FILE;
    }

    private static void addBase(java.util.List<Path> bases, String value) {
        if (value != null && !value.isBlank()) bases.add(Path.of(value));
    }

    private static void migrateLegacy(Path target) {
        try {
            if (!Files.exists(target) && Files.exists(LEGACY_FILE)) {
                Files.copy(LEGACY_FILE, target);
                Files.deleteIfExists(LEGACY_FILE);
                PlayerSafeLogin.LOGGER.info("Migrated saved password to {}", target);
            }
        } catch (IOException e) {
            PlayerSafeLogin.LOGGER.error("Failed to migrate saved password: ", e);
        }
    }
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Saves the password hash to the local file.
     * @param hash Password hash in hexadecimal format.
     * @param password Original password (for future validation if needed).
     */
    public static boolean savePassword(String hash, String password) {
        try {
            if(validatePassword(password, hash)) {
                Files.writeString(PASSWORD_FILE, hash, StandardCharsets.UTF_8);
                return true;
            } else {
                PlayerSafeLogin.LOGGER.error(Component.translatable("playersafelogin.gui.invalid_password_hash") + ": " + password);
                return false;
            }

        } catch (IOException e) {
            PlayerSafeLogin.LOGGER.error("Failed to save password: ", e);
            return false;
        }
    }

    /**
     * Loads the saved password (hash) from the file.
     * @return Password hash if it exists, or null if it doesn't.
     */
    public static String loadPassword() {
        try {
            if (Files.exists(PASSWORD_FILE)) {
                return Files.readString(PASSWORD_FILE, StandardCharsets.UTF_8).trim();
            }
        } catch (IOException e) {
            PlayerSafeLogin.LOGGER.error("Failed to load password: ", e);
        }
        return null;
    }

    private static boolean validatePassword(String password, String hash) {
        String hashedPassword = hashPassword(password);
        return MessageDigest.isEqual(hashedPassword.getBytes(), hash.getBytes());
    }

    /**
     * Checks if there is a saved password.
     * @return True if the password file exists.
     */
    public static boolean hasSavedPassword() {
        return Files.exists(PASSWORD_FILE);
    }

    /**
     * Generates a random password respecting the defined length limits.
     * @return A secure random password.
     */
    public static String generateRandomPassword() {
        int length = RANDOM.nextInt(SecurityConstants.MAX_PASSWORD_LENGTH - SecurityConstants.MIN_PASSWORD_LENGTH + 1) + SecurityConstants.MIN_PASSWORD_LENGTH;
        StringBuilder sb = new StringBuilder(length);
        String chars = SecurityConstants.ALLOWED_CHARACTERS;
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Generates the SHA-256 hash of a password.
     * @param password Original password.
     * @return SHA-256 hash in hexadecimal format.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available!", e);
        }
    }

    /**
     * Converte bytes para string hexadecimal.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public static void deleteSavedPassword() {
        try {
            Files.deleteIfExists(PASSWORD_FILE);
            Files.deleteIfExists(LEGACY_FILE);
            PlayerSafeLogin.LOGGER.info("Local password deleted.");
        } catch (IOException e) {
            PlayerSafeLogin.LOGGER.error("Failed to delete local password: ", e);
        }
    }
}