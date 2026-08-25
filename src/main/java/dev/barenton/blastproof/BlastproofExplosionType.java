package dev.barenton.blastproof;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;

/**
 * Canonical explosion source categories shared by every Blastproof feature.
 */
public enum BlastproofExplosionType {
    TNT("tnt"),
    CREEPER("creeper"),
    END_CRYSTAL("end_crystal"),
    FIREBALL("fireball"),
    WITHER("wither"),
    WITHER_SKULL("wither_skull"),
    RESPAWN_ANCHOR("respawn_anchor"),
    BED("bed"),
    OTHER("other");

    private final String configKey;

    BlastproofExplosionType(String configKey) {
        this.configKey = configKey;
    }

    public String configKey() {
        return configKey;
    }

    /**
     * Resolves an explosion type, preferring an explicit block-provided type over
     * source entity inference.
     */
    public static BlastproofExplosionType resolve(
            ExplosionDamageCalculator calculator,
            Entity source
    ) {
        if (calculator instanceof TypedExplosionDamageCalculator typedCalculator) {
            return typedCalculator.explosionType();
        }

        if (source == null) {
            return OTHER;
        }

        String path = BuiltInRegistries.ENTITY_TYPE.getKey(source.getType()).getPath();
        return switch (path) {
            case "tnt" -> TNT;
            case "creeper" -> CREEPER;
            case "end_crystal" -> END_CRYSTAL;
            case "small_fireball", "fireball" -> FIREBALL;
            case "wither" -> WITHER;
            case "wither_skull" -> WITHER_SKULL;
            default -> OTHER;
        };
    }
}
