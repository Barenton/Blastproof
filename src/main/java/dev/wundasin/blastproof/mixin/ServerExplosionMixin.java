package dev.wundasin.blastproof.mixin;

import com.google.common.collect.ImmutableMap;
import dev.wundasin.blastproof.BlastproofConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/**
 * Mixin into ServerExplosion to conditionally cancel block damage
 * and fire creation based on our BlastproofConfig settings.
 */
@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
    /** The entity that caused the explosion (can be null for block sources). */
    @Shadow
    @Final
    private Entity source;

    /**
     * Immutable map of raw entity names (the "path" after the colon)
     * → config keys. Defaults to "other" if not present.
     */
    @Unique
    private static final Map<String, String> TYPE_KEY_MAP = ImmutableMap.<String, String>builder()
            .put("tnt",             "tnt")
            .put("end_crystal",     "end_crystal")
            .put("creeper",         "creeper")
            .put("bed",             "bed")
            .put("fireball",        "fireball")
            .put("respawn_anchor",  "respawn_anchor")
            .build();

    /**
     * Intercept the block‐damage phase. If blockDamage[type] is false, cancel it.
     */
    @Inject(method = "interactWithBlocks", at = @At("HEAD"), cancellable = true)
    private void onInteractWithBlocks(List<?> blocks, CallbackInfo ci) {
        String key = determineTypeKey();
        if (!BlastproofConfig.blockDamage.getOrDefault(key, false)) {
            ci.cancel();
        }
    }

    /**
     * Intercept the fire‐creation phase. If fireCreation[type] is false, cancel it.
     */
    @Inject(method = "createFire", at = @At("HEAD"), cancellable = true)
    private void onCreateFire(List<?> blocks, CallbackInfo ci) {
        String key = determineTypeKey();
        if (!BlastproofConfig.fireCreation.getOrDefault(key, false)) {
            ci.cancel();
        }
    }

    /**
     * Figure out which config key to use for this explosion:
     * 1. If source==null → “other” (block‐triggered explosion).
     * 2. Otherwise, take source.getType().toString(), which yields
     *    "namespace:path" (e.g. "minecraft:tnt").
     * 3. Split at the colon and grab the path part (e.g. "tnt").
     * 4. Look up in TYPE_KEY_MAP; default to "other" if missing.
     * This avoids any direct Registry calls that may not exist in your mappings.
     */
    @Unique
    private String determineTypeKey() {
        // No entity → treat as "other"
        if (source == null) {
            return "other";
        }

        // getType().toString() is typically "namespace:path"
        String full = source.getType().toString();
        int colon = full.indexOf(':');
        // extract the part after ':' if present
        String name = (colon >= 0 && colon < full.length() - 1)
                ? full.substring(colon + 1)
                : full;

        // map to our config key, or "other" if it's unrecognized
        return TYPE_KEY_MAP.getOrDefault(name, "other");
    }
}
