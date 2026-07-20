# Forensic

Forensic is a lightweight server-side Fabric mod that logs player and world actions for later inspection. It is designed for moderation, grief investigation, and rollback assistance.

## Features

### Logging

- [x] Block placement
- [x] Block breaking
- [x] Explosion block destruction
- [x] Explosion cause tracking (TNT, creeper, etc.)
- [x] Explosion player attribution (when available)
- [x] SQLite storage
- [x] Batch database writes
- [x] JSON metadata storage

### Inspection

- [x] `/inspect` command
- [x] Toggle inspect mode
- [x] Click blocks to inspect history
- [x] Human-readable log formatting
- [x] Prevent interactions while inspecting

---

## Roadmap

### Logging

- [x] Container access (chests, barrels, shulkers, hoppers, etc.)
- [ ] Item insert/remove logging
- [ ] Door, trapdoor, and fence gate interaction
- [ ] Lever and button interaction
- [ ] Pressure plate activation
- [ ] Redstone changes
- [ ] Sign edits
- [ ] Item frame interaction
- [ ] Armor stand interaction
- [ ] Painting interaction
- [ ] Entity kills
- [ ] Mob spawning
- [ ] Bucket usage
- [ ] Fire placement and spread
- [ ] Lava and water flow
- [ ] Block burns
- [ ] Piston movement
- [ ] Crop planting and harvesting
- [ ] Tree growth
- [ ] Natural block changes (grass spread, snow, etc.)

### Commands

- [ ] `/forensic help`
- [x] `/forensic inspect`
- [ ] `/forensic lookup`
- [ ] `/forensic rollback`
- [ ] `/forensic preview`
- [ ] `/forensic purge`
- [ ] `/forensic reload`
- [ ] `/forensic status`
- [ ] `/forensic history`
- [ ] `/forensic about`

### Rollbacks

- [ ] Roll back by player
- [ ] Roll back by time
- [ ] Roll back by radius
- [ ] Roll back specific action types
- [ ] Roll back explosions
- [ ] Undo previous rollback
- [ ] Rollback preview mode
- [ ] Progress reporting for large rollbacks

### Inspection

- [ ] Pagination
- [ ] Filter by action type
- [ ] Filter by player
- [ ] Filter by time
- [ ] Radius lookup
- [ ] Entity inspection
- [ ] Container history lookup

### Database

- [ ] Database indexes
- [ ] Automatic schema migrations
- [ ] Configurable data retention
- [ ] Purge old logs
- [ ] Database statistics
- [ ] Export logs
- [ ] MySQL support
- [ ] PostgreSQL support

### Configuration

- [ ] Configuration file
- [ ] Enable/disable log categories
- [ ] Configurable history limits
- [ ] Permission configuration
- [ ] Configurable database batching
- [ ] Configurable rollback limits

### Documentation

- [ ] Wiki
- [ ] Installation guide
- [ ] Command reference
- [ ] Permission reference
- [ ] API documentation
- [ ] Developer guide

## Building

```bash
./gradlew build
```

The compiled mod will be located in:

```
build/libs/
```

## License

MIT
