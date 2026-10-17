package io.SousaLJ.playersafelogin.storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.UUID;

public class SQLiteStorage implements StorageProvider {
    private final String connectionUrl;

    public SQLiteStorage(String filePath) {
        Path fullPath = Paths.get(filePath).toAbsolutePath();
        this.connectionUrl = "jdbc:sqlite:" + fullPath;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(connectionUrl)) {
            conn.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS passwords (" +
                            "player_uuid TEXT PRIMARY KEY, " +
                            "password_hash TEXT NOT NULL)"
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize SQLite database", e);
        }
    }

    @Override
    public boolean setupPassword(UUID playerId, String hashedPassword) {
        try (Connection conn = DriverManager.getConnection(connectionUrl);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO passwords VALUES (?, ?)"
             )) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, hashedPassword);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean validatePassword(UUID playerId, String hashedPassword) {
        try (Connection conn = DriverManager.getConnection(connectionUrl);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT password_hash FROM passwords WHERE player_uuid = ?"
             )) {
            stmt.setString(1, playerId.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getString("password_hash").equals(hashedPassword);
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean resetPassword(UUID playerId) {
        try (Connection conn = DriverManager.getConnection(connectionUrl);
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM passwords WHERE player_uuid = ?"
             )) {
            stmt.setString(1, playerId.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean requiresInitialSetup(UUID playerId) {
        try (Connection conn = DriverManager.getConnection(connectionUrl);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM passwords WHERE player_uuid = ?"
             )) {
            stmt.setString(1, playerId.toString());
            return !stmt.executeQuery().next();
        } catch (SQLException e) {
            return true;
        }
    }
}
