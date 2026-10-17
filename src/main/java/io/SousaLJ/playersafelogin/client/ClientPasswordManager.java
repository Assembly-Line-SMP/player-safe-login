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

    private static final Path PASSWORD_FILE = Minecraft.getInstance().gameDirectory.toPath().resolve("player_safelogin.dat");
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
            PlayerSafeLogin.LOGGER.info("Local password deleted.");
        } catch (IOException e) {
            PlayerSafeLogin.LOGGER.error("Failed to delete local password: ", e);
        }
    }
}