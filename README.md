# Blastproof

**Version:** 0.1
**Minecraft:** 1.21.5+

A lightweight Fabric mod that **prevents explosions from damaging blocks** (and optionally creating fire) in your server or single-player world. Configuration is planned, but not yet functional in this version.

---

## Features

* **Block Damage Prevention**
  Stops all explosion sources (TNT, End Crystals, Respawn Anchors, Creepers, Beds, Fireballs, etc.) from breaking blocks.

* **Fire Creation Prevention** *(coming soon)*
  Planned support to control whether certain explosions (e.g. Respawn Anchors) ignite fire. Currently defaults to no fire creation.

* **Easy JSON Config** *(not yet active)*
  A `config/blastproof.json` file will eventually allow you to configure behavior.

---

## Installation

* Download the latest `blastproof-<version>.jar` and place it in your
   `mods/` folder.

---

## Configuration (Coming Soon)

In future versions, the mod will generate `config/blastproof.json` with defaults like:

```json
{
  "blockDamage": {
    "tnt": false,
    "end_crystal": false,
    "respawn_anchor": false,
    "creeper": false,
    "bed": false,
    "fireball": false,
    "other": false
  },
  "fireCreation": {
    "respawn_anchor": true,
    "other": false
  }
}
```

---

## License

[MIT](LICENSE)
© 2025 Barenton
