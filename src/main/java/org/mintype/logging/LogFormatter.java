package org.mintype.logging;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.mintype.database.model.ActionType;
import org.mintype.database.model.LogEntry;

import java.util.List;

public class LogFormatter {

    private static final int ORANGE = 0xFF9900;
    private static final int GRAY   = 0x808080;
    private static final int GREEN  = 0x00FF00;
    private static final int RED    = 0xFF0000;

    private static String formatAction(LogEntry log) {

        return switch (log.action()) {

            case BLOCK_BREAK ->
                    "broke";

            case BLOCK_PLACE ->
                    "placed";

            case EXPLOSION ->
                    "was destroyed by explosion";

            default ->
                    log.action().name().toLowerCase();
        };
    }


    private static String formatTime(long timestamp) {

        long seconds = (System.currentTimeMillis() - timestamp) / 1000;

        if (seconds < 60) {
            return seconds + " second" + (seconds == 1 ? "" : "s");
        }

        long minutes = seconds / 60;

        if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        }

        long hours = minutes / 60;

        if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s");
        }

        long days = hours / 24;

        if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s");
        }

        long weeks = days / 7;

        return weeks + " week" + (weeks == 1 ? "" : "s");
    }

    public static Component noLogsFound() {
        MutableComponent message = Component.empty();
        message.append(
                Component.literal("[Forensic]")
                        .withColor(ORANGE)
        );
        message.append(
                Component.literal(" No logs found.")
                        .withColor(GRAY)
        );

        return message;

    }

    public static Component formatLogs(List<LogEntry> logs) {

        MutableComponent message = Component.empty();

        if (logs.isEmpty()) {
            return noLogsFound();
        }

        LogEntry first = logs.get(0);

        // Header once
        message.append(
                Component.literal("----- ")
                        .withColor(GRAY)
        );

        message.append(
                Component.literal("[Forensic]")
                        .withColor(ORANGE)
        );

        message.append(
                Component.literal(
                        " ----- (x:" +
                                first.x() +
                                "/y:" +
                                first.y() +
                                "/z:" +
                                first.z() +
                                ")"
                ).withColor(GRAY)
        );


        // Each log
        for (LogEntry log : logs) {

            message.append(
                    Component.literal("\n")
            );

            message.append(
                    formatEntry(log)
            );
        }

        return message;
    }

    private static Component formatEntry(LogEntry log) {

        return switch (log.action()) {
            case EXPLOSION -> formatExplosion(log);
            case CONTAINER_OPEN -> formatContainerOpen(log);
            case CONTAINER_CHANGE -> formatContainerChange(log);
            default -> formatNormal(log);
        };
    }

    private static Component formatContainerOpen(LogEntry log) {

        MutableComponent message = Component.empty();

        message.append(
                Component.literal(
                        formatTime(log.timestamp()) + " ago - "
                ).withColor(GRAY)
        );

        if (log.playerName() != null) {
            message.append(
                    Component.literal(log.playerName())
                            .withColor(ORANGE)
            );
        } else {
            message.append(
                    Component.literal("Unknown")
            );
        }

        message.append(
                Component.literal(" opened ")
        );

        if (log.data() != null && log.data().has("block")) {
            message.append(
                    Component.literal(
                            log.data().get("block").getAsString()
                    ).withColor(ORANGE)
            );
        } else {
            message.append(
                    Component.literal("container")
            );
        }

        return message;
    }

    private static Component formatContainerChange(LogEntry log) {

        MutableComponent message = Component.empty();

        message.append(
                Component.literal(
                        formatTime(log.timestamp()) + " ago - "
                ).withColor(GRAY)
        );

        if (log.playerName() != null) {
            message.append(
                    Component.literal(log.playerName())
                            .withColor(ORANGE)
            );
        } else {
            message.append(Component.literal("Unknown"));
        }

        String action = log.data().get("action").getAsString();

        int amountColor = GREEN;

        switch (action) {

            case "inserted" ->
                    message.append(Component.literal(" inserted "));

            case "removed" -> {
                    message.append(Component.literal(" removed "));
                    amountColor = RED;
            }

            case "changed" ->
                    message.append(Component.literal(" changed "));

            default ->
                    message.append(Component.literal(" modified "));
        }

        String item = log.data().get("item").getAsString();

        if (item.contains(":")) {
            item = item.substring(item.indexOf(':') + 1);
        }

        int amount = log.data().get("amount").getAsInt();

        message.append(
                Component.literal(amount + " ")
                        .withColor(amountColor)
        );

        message.append(
                Component.literal(item)
                        .withColor(ORANGE)
        );

        return message;
    }

    private static Component formatNormal(LogEntry log) {

        if (log.action() == ActionType.EXPLOSION) {
            return formatExplosion(log);
        }

        MutableComponent message = Component.empty();

        message.append(
                Component.literal(
                        formatTime(log.timestamp()) + " ago - "
                ).withColor(GRAY)
        );

        if (log.playerName() != null) {
            message.append(
                    Component.literal(
                            log.playerName()
                    ).withColor(ORANGE)
            );
        }

        message.append(
                Component.literal(
                        " " + formatAction(log)
                )
        );

        if (log.data() != null && log.data().has("block")) {

            message.append(
                    Component.literal(" ")
            );

            message.append(
                    Component.literal(
                            log.data()
                                    .get("block")
                                    .getAsString()
                    ).withColor(ORANGE)
            );
        }

        return message;
    }

    private static Component formatExplosion(LogEntry log) {

        MutableComponent message = Component.empty();

        message.append(
                Component.literal(
                        formatTime(log.timestamp()) + " ago - "
                ).withColor(GRAY)
        );

        String block = log.data().get("block").getAsString();

        message.append(
                Component.literal(block)
                        .withColor(ORANGE)
        );

        message.append(Component.literal(" was destroyed by "));

        String cause = log.data().get("cause").getAsString();

        // Show "tnt" instead of "minecraft:tnt"
        if (cause.contains(":")) {
            cause = cause.substring(cause.indexOf(':') + 1);
        }

        message.append(
                Component.literal(cause)
                        .withColor(ORANGE)
        );

        if (log.data().has("playerName")) {

            message.append(
                    Component.literal(" caused by ")
            );

            message.append(
                    Component.literal(
                            log.data().get("playerName").getAsString()
                    ).withColor(ORANGE)
            );
        }

        return message;
    }
}