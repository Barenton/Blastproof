# Blastproof

**Version:** 0.1.0
**Minecraft:** 1.21.5+

A lightweight Fabric mod that **prevents explosions from damaging blocks** (and optionally creating fire) in your server or single-player world. Configuration is planned, but not yet functional in this version.

---

## Features

* **Block Damage Prevention**
  Stops all explosion sources (TNT, End Crystals, Respawn Anchors, Creepers, Beds, Fireballs, etc.) from breaking blocks.

* **Fire Creation Prevention** *(limited)*
  Set whether you want fire creation after certain explosions (eg. Respawn Anchor). Defaults to no fire creation.
  Currently no individual explosion support. Either fire is created or it isn't!

* **Easy JSON Config**
  A `config/blastproof.json` file allows you to configure behavior.

---

## Installation

* Download the latest `.jar` and place it in your
   `mods/` folder.

---

## Configuration

The mod will generate `config/blastproof.json` with defaults:

```json
{
  "disableBlockDamage": {
    "tnt": true,
    "creeper": false,
    "end_crystal": true,
    "fireball": true,
    "wither": true,
    "wither_skull": true,
    "other": true
  },
  "disableFireCreation": {
    "other": true
  }
}
```

---

## License

[MIT](LICENSE)
© 2025 Barenton
