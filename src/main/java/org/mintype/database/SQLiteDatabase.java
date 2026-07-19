package org.mintype.database;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mintype.database.model.ActionType;
import org.mintype.database.model.LogEntry;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                        player_uuid TEXT,
                        player_name TEXT,
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
            (player_uuid, player_name, action, world, x, y, z, timestamp, data)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    entry.playerUuid() == null
                            ? null
                            : entry.playerUuid().toString()
            );

            statement.setString(
                    2,
                    entry.playerName()
            );
            statement.setString(3, entry.action().name());
            statement.setString(4, entry.world());
            statement.setInt(5, entry.x());
            statement.setInt(6, entry.y());
            statement.setInt(7, entry.z());
            statement.setLong(8, entry.timestamp());
            statement.setString(9, entry.data() == null ? null : entry.data().toString());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert log entry.", e);
        }
    }

    @Override
    public void insertBatch(List<LogEntry> entries) {

        String sql = """
            INSERT INTO logs
            (player_uuid, player_name, action, world, x, y, z, timestamp, data)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                for (LogEntry entry : entries) {

                    statement.setString(
                            1,
                            entry.playerUuid() == null
                                    ? null
                                    : entry.playerUuid().toString()
                    );

                    statement.setString(
                            2,
                            entry.playerName()
                    );
                    statement.setString(3, entry.action().name());
                    statement.setString(4, entry.world());
                    statement.setInt(5, entry.x());
                    statement.setInt(6, entry.y());
                    statement.setInt(7, entry.z());
                    statement.setLong(8, entry.timestamp());
                    statement.setString(9, entry.data() == null ? null : entry.data().toString());

                    statement.addBatch();
                }

                statement.executeBatch();
            }

            connection.commit();

        } catch (SQLException e) {

            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                rollbackError.printStackTrace();
            }

            throw new RuntimeException("Failed to insert batch.", e);

        } finally {

            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<LogEntry> getLogs(
            String world,
            int x,
            int y,
            int z,
            int limit
    ) {

        List<LogEntry> logs = new ArrayList<>();

        String sql = """
        SELECT *
        FROM logs
        WHERE world = ?
          AND x = ?
          AND y = ?
          AND z = ?
        ORDER BY timestamp DESC
        LIMIT ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, world);
            statement.setInt(2, x);
            statement.setInt(3, y);
            statement.setInt(4, z);
            statement.setInt(5, limit);

            ResultSet result = statement.executeQuery();

            while (result.next()) {

                String playerId = result.getString("player_uuid");

                String playerName = result.getString("player_name");

                UUID player = playerId == null
                        ? null
                        : UUID.fromString(playerId);

                String dataString = result.getString("data");

                JsonObject jsonData = null;

                if (dataString != null) {
                    jsonData = JsonParser.parseString(dataString)
                            .getAsJsonObject();
                }

                logs.add(new LogEntry(
                        result.getLong("id"),
                        player,
                        playerName,
                        ActionType.valueOf(result.getString("action")),
                        result.getString("world"),
                        result.getInt("x"),
                        result.getInt("y"),
                        result.getInt("z"),
                        result.getLong("timestamp"),
                        jsonData
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to query logs.", e);
        }

        return logs;
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