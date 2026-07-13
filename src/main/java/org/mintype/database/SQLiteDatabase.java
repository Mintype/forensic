package org.mintype.database;

import org.mintype.database.model.LogEntry;

import java.nio.file.Path;
import java.sql.*;
import java.util.List;

public class SQLiteDatabase implements Database {

    private final Path databasePath;
    private Connection connection;

    public SQLiteDatabase(Path databasePath) {
        this.databasePath = databasePath;
    }

    @Override
    public void init() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);

            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        player TEXT NOT NULL,
                        action TEXT NOT NULL,
                        world TEXT NOT NULL,
                        x INTEGER NOT NULL,
                        y INTEGER NOT NULL,
                        z INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        data TEXT
                    );
                """);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize SQLite database.", e);
        }
    }

    @Override
    public void insert(LogEntry entry) {
        String sql = """
            INSERT INTO logs
            (player, action, world, x, y, z, timestamp, data)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, entry.player().toString());
            statement.setString(2, entry.action().name());
            statement.setString(3, entry.world());
            statement.setInt(4, entry.x());
            statement.setInt(5, entry.y());
            statement.setInt(6, entry.z());
            statement.setLong(7, entry.timestamp());
            statement.setString(8, entry.data());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert log entry.", e);
        }
    }

    @Override
    public void close() {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close database.", e);
        }
    }
}