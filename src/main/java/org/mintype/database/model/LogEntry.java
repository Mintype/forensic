package org.mintype.database.model;

import com.google.gson.JsonObject;

import java.util.UUID;

public record LogEntry(
        long id,
        UUID player,
        ActionType action,
        String world,
        int x,
        int y,
        int z,
        long timestamp,
        JsonObject data
) {}