# Blastproof

**Control whether explosions damage blocks, start fires, or affect mobs in your Fabric server or single-player world.**

Blastproof ensures explosions from TNT, creepers, end crystals, and other sources won't damage your hard work. Whether you're building cities or simply protecting your creations, Blastproof helps maintain the integrity of your world.

---

## 💡 Why I Created Blastproof

> I developed Blastproof because I wanted to run a modded Fabric server but couldn't find an up-to-date mod to disable block damage from blocks not covered through vanilla gamerules (beds, respawn anchors, etc.), which can potentially ruin builds in the overworld. I also wanted to have TNT enabled (normally disabled with the `tntExplodes` gamerule set to `false`), but didn't want to break anything! Blastproof addresses this gap, providing easy control over explosion damage and fire creation. This mod is ideal for players who want to preserve all features that `mobGriefing false` disables (such as endermen picking up blocks), but still prevent explosions from damaging builds.


## 🚀 Features

* **Compatibility with Gamerules:** Gamerules such as `mobGriefing` won't conflict with Blastproof but rather serve as an additional layer of protection.
* **Mob Explosion Control:** Optionally make mobs fully ignore selected explosion sources while players retain vanilla explosion behavior.
* **Block Damage Control:** Disable block damage from TNT, Creepers, End Crystals, Wither Skulls, Fireballs, Withers, and custom "other" sources.
* **Fire Prevention:** Stop explosions from causing wildfires.
* **Auto-Generated Configuration:** A user-friendly config file is created on the first run at `config/blastproof.json` with _blastproofed_ default settings.
* **In-Game Commands:** Use `/blastproof` to easily:

  * **Check settings:**

    ```mc
    /blastproof disableBlockDamage tnt
    ```
  * **Toggle options:**

    ```mc
    /blastproof disableBlockDamage tnt false
    ```
  * **Protect mobs from an explosion source:**

    ```mc
    /blastproof disableMobDamage bed true
    ```
* **Universal Compatibility:** Works seamlessly on both dedicated servers and integrated single-player worlds.
* **Lightweight:** Less than 20 KB with no additional dependencies (besides Fabric API).




## 📝 Disclaimer

This is my first Minecraft mod, Java project, and GitHub repository. As such, certain aspects of the repository and files might not be perfect, and I'm open to suggestions and improvements!

---

## ⚙️ Configuration

After your first launch, edit `config/blastproof.json` to adjust settings.
Each source is independent. The `other` key applies only to explosions whose source Blastproof cannot identify.

> **Upgrading from 0.2.x:** Beds and respawn anchors previously fell back to `other`. If you relied on that behavior, set their explicit `bed` and `respawn_anchor` keys to the values you want after upgrading.

When a `disableMobDamage` entry is enabled, matching `Mob` entities ignore the entire explosion hit: health damage, knockback, and explosion-hit callbacks are all suppressed. Players, armor stands, and other non-mob entities are unaffected by this setting.

```jsonc
{
  "disableBlockDamage": {
    "tnt": true,
    "creeper": true,
    "end_crystal": true,
    "fireball": true,
    "wither": true,
    "wither_skull": true,
    "respawn_anchor": true,
    "bed": true,
    "other": true
  },
  "disableFireCreation": {
    "tnt": true,
    "creeper": true,
    "end_crystal": true,
    "fireball": true,
    "wither": true,
    "wither_skull": true,
    "respawn_anchor": true,
    "bed": true,
    "other": true
  },
  "disableMobDamage": {
    "tnt": false,
    "creeper": false,
    "end_crystal": false,
    "fireball": false,
    "wither": false,
    "wither_skull": false,
    "respawn_anchor": false,
    "bed": false,
    "other": false
  }
}
```
