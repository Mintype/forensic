# Forensic - Developer Guide

This document explains the internal architecture of the Forensic mod for developers who want to understand, maintain, or extend it.

---

## Overview

Forensic is a server-side Fabric mod that logs player and world actions (block breaks, placements, explosions, container opens) into a local SQLite database. Staff players can then enter "inspect mode" and click blocks to view their recent history.

---

## Project Structure

```
src/main/java/org/mintype/
├── Forensic.java                  # Mod entrypoint
├── command/
│   ├── ForensicCommand.java       # /forensic command tree
│   └── InspectCommand.java        # /forensic inspect subcommand
├── database/
│   ├── Database.java              # Database interface
│   ├── SQLiteDatabase.java        # SQLite implementation
│   ├── BatchWriter.java           # Async batch writer thread
│   └── model/
│       ├── ActionType.java        # Enum of logged action types
│       └── LogEntry.java          # Record representing a single log row
├── event/
│   └── BlockEvents.java           # Fabric event listeners for block actions
├── inspect/
│   ├── InspectManager.java        # Tracks which players are in inspect mode
│   └── InspectListener.java       # Intercepts player interactions in inspect mode
├── logging/
│   ├── LoggerService.java         # Public API for logging actions
│   ├── LogQueue.java              # Thread-safe blocking queue
│   ├── LogFormatter.java          # Formats LogEntry records into chat Components
│   ├── DataBuilder.java           # Helper to build JSON metadata for log entries
│   └── PlayerNameResolver.java    # Resolves UUID to player name
├── mixin/
│   ├── ServerPlayerMixin.java     # Logs container opens via openMenu()
│   ├── ServerExplosionMixin.java  # Logs explosion block destruction
│   └── BlockItemMixin.java        # Logs block placements
└── permission/
    └── ForensicPermissions.java   # Permission definitions

src/main/resources/
├── fabric.mod.json                # Mod metadata and entrypoints
├── forensic.mixins.json           # Server-side mixin config
└── assets/forensic/icon.png       # Mod icon

src/client/resources/
└── forensic.client.mixins.json    # Client-side mixin config (currently empty)

scripts/
├── clear-db.py                    # Python script to clear all logs
└── clear-db.bat                   # Windows wrapper for clear-db.py
```

---

## Architecture

### Data Flow

```
Game Event (block break, explosion, etc.)
        │
        ▼
   LoggerService.log()
        │
        ▼
     LogQueue  (LinkedBlockingQueue)
        │
        ▼
    BatchWriter  (background thread)
        │
        ▼
   Database.insertBatch()
        │
        ▼
     SQLite DB  (config/forensic.db)
```

Events fire on the server thread. `LoggerService` creates a `LogEntry` and enqueues it into the `LogQueue` without blocking. A separate `BatchWriter` thread drains entries in batches (up to 500 every 50ms) and writes them to the database in a single transaction.

### Startup Sequence (`Forensic.onInitialize()`)

1. Opens/creates the SQLite database at `config/forensic.db`
2. Creates the `logs` table if it does not exist
3. Creates a `LogQueue` and wires it to `LoggerService`
4. Starts the `BatchWriter` on a dedicated thread
5. Registers block event listeners via `BlockEvents.register()`
6. Registers a server-stopping hook to flush the writer and close the database
7. Registers the inspect interaction listeners
8. Registers the `/forensic` command tree

---

## Key Components

### `Database` / `SQLiteDatabase`

The `Database` interface (`database/Database.java:7`) defines the contract:

- `init()` - Opens connection, creates the `logs` table
- `insert(LogEntry)` - Single insert
- `insertBatch(List<LogEntry>)` - Batch insert with transaction/rollback
- `getLogs(world, x, y, z, limit)` - Query logs at a position, ordered by newest first
- `close()` - Closes the connection

`SQLiteDatabase` (`database/SQLiteDatabase.java:14`) implements this using JDBC with SQLite. The schema stores: `id`, `player_uuid`, `player_name`, `action`, `world`, `x`, `y`, `z`, `timestamp`, and `data` (JSON text).

### `BatchWriter`

`database/BatchWriter.java:10` runs on its own thread. It:
1. Blocks on `queue.take()` waiting for at least one entry
2. Sleeps 50ms to accumulate more entries
3. Drains up to 499 more entries from the queue
4. Calls `database.insertBatch()` in a single transaction
5. Repeats until `stop()` is called

This batching approach keeps database I/O off the server thread and reduces write overhead.

### `LogQueue`

`logging/LogQueue.java:9` wraps a `LinkedBlockingQueue<LogEntry>`. It provides `enqueue()`, `take()` (blocking), and `drainTo()` (non-blocking bulk drain).

### `LoggerService`

`logging/LoggerService.java:9` is the public API. Call `logger.log(playerUuid, playerName, action, world, x, y, z, data)` to record an event. It constructs a `LogEntry` with the current timestamp and enqueues it.

### `LogEntry`

`database/model/LogEntry.java:7` is a Java record:

```java
record LogEntry(long id, UUID playerUuid, String playerName,
                ActionType action, String world,
                int x, int y, int z, long timestamp,
                JsonObject data)
```

The `data` field is a nullable `JsonObject` used for event-specific metadata (e.g., `{"block": "minecraft:dirt"}` or `{"cause": "minecraft:tnt", "block": "minecraft:stone"}`).

### `ActionType`

`database/model/ActionType.java:3` enum values:

| Value | Description |
|-------|-------------|
| `BLOCK_BREAK` | Player broke a block |
| `BLOCK_PLACE` | Player placed a block |
| `CONTAINER_OPEN` | Player opened a container |
| `CONTAINER_CHANGE` | (Reserved) Container contents changed |
| `ITEM_INSERT` | (Reserved) Item inserted into container |
| `ITEM_REMOVE` | (Reserved) Item removed from container |
| `ENTITY_DAMAGE` | (Reserved) Entity took damage |
| `ENTITY_DEATH` | (Reserved) Entity died |
| `EXPLOSION` | Block destroyed by explosion |
| `FIRE_SPREAD` | (Reserved) Fire spread to a block |

### `DataBuilder`

`logging/DataBuilder.java:7` provides static helpers to create `JsonObject` metadata:

- `DataBuilder.block(blockPath)` - Returns `{"block": "minecraft:oak_planks"}`
- `DataBuilder.explosion(cause, block, source)` - Returns `{"cause": "...", "block": "...", "source_type": "..."}`

---

## Mixins

Mixins inject code into Minecraft classes at runtime. All three server mixins are registered in `forensic.mixins.json`.

### `ServerPlayerMixin`

`mixin/ServerPlayerMixin.java:24` - Injects at the HEAD of `ServerPlayer.openMenu()`. When a player opens a container (chest, barrel, etc.), it logs a `CONTAINER_OPEN` event with the block type.

### `ServerExplosionMixin`

`mixin/ServerExplosionMixin.java:24` - Injects at the HEAD of `ServerExplosion.interactWithBlocks()`. Iterates over every block the explosion will destroy and logs an `EXPLOSION` event per block. Records the explosion cause (e.g., `minecraft:tnt`) and the indirect player source if available.

### `BlockItemMixin`

`mixin/BlockItemMixin.java:15` - Injects at the RETURN of `BlockItem.place()`. If the placement returned `SUCCESS`, logs a `BLOCK_PLACE` event.

---

## Event Listeners

### `BlockEvents`

`event/BlockEvents.java:10` registers Fabric event listeners:

- `PlayerBlockBreakEvents.AFTER` - Logs `BLOCK_BREAK` when a player breaks a block. Includes the block type in the data JSON.

(Block placement was originally handled here but is now done via `BlockItemMixin` instead.)

### `InspectListener`

`inspect/InspectListener.java:13` registers interaction callbacks:

| Event | Behavior in Inspect Mode |
|-------|--------------------------|
| `UseBlockCallback` | Queries and displays the last 5 logs for that block |
| `AttackBlockCallback` | Same as above; returns `FAIL` to prevent block damage |
| `UseItemCallback` | Returns `FAIL` to prevent item use |
| `UseEntityCallback` | Returns `FAIL` to prevent entity interaction |
| `AttackEntityCallback` | Returns `FAIL` to prevent entity attacks |

All callbacks first check `world.isClientSide()` and `inspectManager.isInspecting(player)` before doing anything.

---

## Inspect System

### `InspectManager`

`inspect/InspectManager.java:10` maintains a `ConcurrentHashMap`-backed `Set<UUID>` of players in inspect mode. `toggle(player)` adds or removes the player and returns the new state. `isInspecting(player)` checks membership.

### `InspectCommand`

`command/InspectCommand.java:15` registers the `/forensic inspect` command. Requires GAMEMASTER permission level. Toggles inspect mode for the executing player and sends a confirmation message.

### `LogFormatter`

`logging/LogFormatter.java:10` formats `LogEntry` records into colored Minecraft `Component` messages:

- Shows a header with coordinates
- Each entry shows: time ago, player name, action, and block
- Explosions get special formatting: "block was destroyed by cause"
- Colors: orange (`#FF9900`) for names/objects, gray (`#808080`) for timestamps

---

## Permissions

`permission/ForensicPermissions.java:6` defines:

- `INSPECT` - Requires `PermissionLevel.GAMEMASTERS` (op level 2+)

---

## Build

```bash
./gradlew build        # Build the mod
./gradlew runClient    # Run a development client
./gradlew runServer    # Run a development server
```

Output JAR is at `build/libs/forensic-<version>.jar`.

**Requirements:** Java 25+, Minecraft 26.2, Fabric Loader 0.19.3+

---

## Scripts

### `clear-db.py`

`scripts/clear-db.py:1` - A Python script that wipes all rows from the `logs` table in the development database at `run/config/forensic.db`. Useful for resetting state during testing.

```bash
python scripts/clear-db.py
```

### `clear-db.bat`

`scripts/clear-db.bat:1` - Windows batch wrapper that runs `clear-db.py` with the correct path. Double-click or run from a terminal:

```cmd
scripts\clear-db.bat
```

---

## Adding New Log Events

1. Add a new value to `ActionType` enum if needed
2. Create the event hook (Fabric event callback or new Mixin)
3. Call `Forensic.logger.log(uuid, name, action, world, x, y, z, data)` with appropriate metadata
4. If needed, add a formatter case in `LogFormatter.formatAction()` or `formatEntry()`
