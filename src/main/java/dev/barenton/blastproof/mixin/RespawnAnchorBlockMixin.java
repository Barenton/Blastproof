package dev.barenton.blastproof.mixin;

import dev.barenton.blastproof.BlastproofExplosionType;
import dev.barenton.blastproof.TypedExplosionDamageCalculator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RespawnAnchorBlock.class)
public abstract class RespawnAnchorBlockMixin {
    /**
     * Marks the anchor explosion explicitly while preserving its custom
     * water-aware damage calculator.
     */
    @Redirect(
            method = "explode(Lnet/minecraft/world/level/block/state/BlockState;"
                    + "Lnet/minecraft/server/level/ServerLevel;"
                    + "Lnet/minecraft/core/BlockPos;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;"
                            + "explode(Lnet/minecraft/world/entity/Entity;"
                            + "Lnet/minecraft/world/damagesource/DamageSource;"
                            + "Lnet/minecraft/world/level/ExplosionDamageCalculator;"
                            + "Lnet/minecraft/world/phys/Vec3;"
                            + "FZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"
            )
    )
    private void redirectAnchorExplosion(
            ServerLevel world,
            Entity source,
            DamageSource damageSource,
            ExplosionDamageCalculator calculator,
            Vec3 pos,
            float radius,
            boolean fire,
            ExplosionInteraction interaction
    ) {
        world.explode(
                source,
                damageSource,
                TypedExplosionDamageCalculator.wrap(calculator, BlastproofExplosionType.RESPAWN_ANCHOR),
                pos,
                radius,
                fire,
                interaction
        );
    }
}
