package dev.barenton.blastproof.mixin;

import dev.barenton.blastproof.BlastproofConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RespawnAnchorBlock.class)
public abstract class RespawnAnchorBlockMixin {
    /**
     * In RespawnAnchorBlock#explode(BlockState, Level, BlockPos),
     * redirect the single call to Level.explode(...) so that
     * block‐breaking is disabled by swapping ExplosionInteraction.BLOCK → NONE.
     */
    @Redirect(
            method = "explode(Lnet/minecraft/world/level/block/state/BlockState;"
                    + "Lnet/minecraft/world/level/Level;"
                    + "Lnet/minecraft/core/BlockPos;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;"
                            + "explode(Lnet/minecraft/world/entity/Entity;"
                            + "Lnet/minecraft/world/damagesource/DamageSource;"
                            + "Lnet/minecraft/world/level/ExplosionDamageCalculator;"
                            + "Lnet/minecraft/world/phys/Vec3;"
                            + "FZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"
            )
    )
    private void redirectAnchorExplosion(
            Level world,
            Entity source,
            DamageSource damageSource,
            ExplosionDamageCalculator calculator,
            Vec3 pos,
            float radius,
            boolean fire,
            ExplosionInteraction interaction
    ) {
        boolean disableAnchor = BlastproofConfig.get(
                BlastproofConfig.SECTION_BLOCK_DAMAGE,
                BlastproofConfig.RESPAWN_ANCHOR_KEY,
                false
        );

        ExplosionInteraction actualInteraction = disableAnchor
                ? ExplosionInteraction.NONE
                : interaction;

        world.explode(
                source,
                damageSource,
                calculator,
                pos,
                radius,
                fire,
                actualInteraction
        );
    }
}