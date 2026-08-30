package org.mintype.database;

import org.mintype.database.model.LogEntry;

import java.util.List;

public interface Database {

    /**
     * Opens the database connection and initializes the schema.
     */
    void init();

    /**
     * Inserts a single log entry.
     */
    void insert(LogEntry entry);

    /**
     * Inserts a batch of log entries.
     */
    void insertBatch(List<LogEntry> entries);

    /**
     * Gets logs at a specific block position.
     */
    List<LogEntry> getLogs(
            String world,
            int x,
            int y,
            int z,
            int limit
    );

    /**
     * Searches logs using optional filters.
     *
     * @param world world to search in
     * @param centerX center X position, or null if no range filter
     * @param centerY center Y position, or null if no range filter
     * @param centerZ center Z position, or null if no range filter
     * @param range block radius, or null
     * @param since timestamp to search from, or null
     * @param playerName player name filter, or null
     * @param action action filter, or null
     * @param limit maximum number of results
     */
    List<LogEntry> lookupLogs(
            String world,
            Integer centerX,
            Integer centerY,
            Integer centerZ,
            Integer range,
            Long since,
            String playerName,
            String action,
            int limit
    );

    /**
     * Closes the database connection.
     */
    void close();
}