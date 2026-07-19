package org.mintype.logging;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.mintype.database.model.ActionType;
import org.mintype.database.model.LogEntry;

import java.util.List;

public class LogFormatter {

    private static final int ORANGE = 0xFF9900;
    private static final int GRAY = 0x808080;

//    public static Component format(LogEntry log) {
//
//        MutableComponent message = Component.empty();
//
//        // ----- [Forensic] ----- (x:y:z)
//        message.append(
//                Component.literal("----- ")
//        );
//
//        message.append(
//                Component.literal("[Forensic]")
//                        .withColor(ORANGE)
//        );
//
//        message.append(
//                Component.literal(
//                        " ----- (x:" +
//                                log.x() +
//                                "/y:" +
//                                log.y() +
//                                "/z:" +
//                                log.z() +
//                                ")"
//                ).withColor(GRAY)
//        );
//
//
//        // Time ago
//        message.append(
//                Component.literal(
//                        "\n" + formatTime(log.timestamp()) + " ago - "
//                ).withColor(GRAY)
//        );
//
//
//        // Player
//        if (log.player() != null) {
//            message.append(
//                    Component.literal(
//                            log.player().toString()
//                    ).withColor(ORANGE)
//            );
//        } else {
//            message.append(
//                    Component.literal("Unknown")
//            );
//        }
//
//
//        // Action
//        message.append(
//                Component.literal(
//                        " " + formatAction(log)
//                )
//        );
//
//
//        // Block
//        if (log.data() != null && log.data().has("block")) {
//
//            message.append(
//                    Component.literal(" (")
//            );
//
//            message.append(
//                    Component.literal(
//                            log.data()
//                                    .get("block")
//                                    .getAsString()
//                    ).withColor(ORANGE)
//            );
//
//            message.append(
//                    Component.literal(")")
//            );
//        }
//
//
//        return message;
//    }

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

    public static Component formatLogs(List<LogEntry> logs) {

        MutableComponent message = Component.empty();

        if (logs.isEmpty()) {
            return Component.literal("[Forensic] No logs found.");
        }

        LogEntry first = logs.get(0);

        // Header once
        message.append(
                Component.literal("----- ")
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

//            message.append(
//                    Component.literal(")")
//            );
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

//        message.append(Component.literal("("));

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