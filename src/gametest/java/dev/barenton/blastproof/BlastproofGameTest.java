package dev.barenton.blastproof;

import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BlastproofGameTest implements CustomTestMethodInvoker {
    @GameTest
    public void configuredExplosionEffects(GameTestHelper helper) {
        Map<String, Boolean> originalMobSettings = captureMobSettings();

        try {
            assertCompleteSourceConfiguration(helper);
            verifyCommandAndBedImmunity(helper);
            verifyActualNetherBedImmunity(helper);
            helper.killAllEntities();
            verifyOtherDoesNotOverrideBed(helper);
            helper.killAllEntities();
            verifyRespawnAnchorImmunity(helper);
            helper.killAllEntities();
            verifyTntRemainsIndependent(helper);
            helper.succeed();
        } finally {
            originalMobSettings.forEach((key, value) ->
                    BlastproofConfig.updateEntry(BlastproofConfig.SECTION_MOB_DAMAGE, key, value)
            );
        }
    }

    private static Map<String, Boolean> captureMobSettings() {
        Map<String, Boolean> settings = new LinkedHashMap<>();
        for (BlastproofExplosionType type : BlastproofExplosionType.values()) {
            settings.put(
                    type.configKey(),
                    BlastproofConfig.get(BlastproofConfig.SECTION_MOB_DAMAGE, type.configKey(), false)
            );
        }
        return settings;
    }

    private static void assertCompleteSourceConfiguration(GameTestHelper helper) {
        for (String section : new String[]{
                BlastproofConfig.SECTION_BLOCK_DAMAGE,
                BlastproofConfig.SECTION_FIRE_CREATION,
                BlastproofConfig.SECTION_MOB_DAMAGE
        }) {
            var configuredKeys = BlastproofConfig.getKeysForSection(section);
            for (BlastproofExplosionType type : BlastproofExplosionType.values()) {
                assertTrue(
                        configuredKeys.contains(type.configKey()),
                        "Missing " + section + " key: " + type.configKey()
                );
            }
        }
    }

    @SuppressWarnings("removal")
    private static void verifyCommandAndBedImmunity(GameTestHelper helper) {
        helper.getLevel().getServer().getCommands().performPrefixedCommand(
                helper.getLevel().getServer().createCommandSourceStack(),
                "blastproof disableMobDamage bed true"
        );
        assertTrue(
                BlastproofConfig.get(BlastproofConfig.SECTION_MOB_DAMAGE, "bed", false),
                "The disableMobDamage command did not persist the bed setting"
        );

        Vec3 center = helper.absoluteVec(new Vec3(2.5, 2.0, 2.5));
        var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, 3.5F, 2.0F, 2.5F);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.setInvulnerable(false);
        Vec3 playerPosition = helper.absoluteVec(new Vec3(1.5, 2.0, 2.5));
        player.teleportTo(playerPosition.x, playerPosition.y, playerPosition.z);

        float zombieHealth = zombie.getHealth();
        Vec3 zombieVelocity = zombie.getDeltaMovement();
        float playerHealth = player.getHealth();

        explode(
                helper.getLevel(),
                null,
                TypedExplosionDamageCalculator.wrap(null, BlastproofExplosionType.BED),
                center
        );

        assertTrue(zombie.getHealth() == zombieHealth, "Protected mob took bed explosion damage");
        assertTrue(
                zombie.getDeltaMovement().equals(zombieVelocity),
                "Protected mob received bed explosion knockback"
        );
        assertTrue(
                player.getHealth() < playerHealth || player.getDeltaMovement().lengthSqr() > 0.0,
                "Player should retain vanilla bed explosion effects"
        );
    }

    private static void verifyActualNetherBedImmunity(GameTestHelper helper) {
        ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        assertTrue(nether != null, "The Nether level was not available for the bed test");

        BlockPos footPos = new BlockPos(0, 80, 0);
        BlockPos headPos = footPos.relative(Direction.EAST);
        var footState = Blocks.BED.red().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.EAST)
                .setValue(BedBlock.PART, BedPart.FOOT);
        var headState = footState.setValue(BedBlock.PART, BedPart.HEAD);
        nether.setBlockAndUpdate(footPos, footState);
        nether.setBlockAndUpdate(headPos, headState);

        var zombie = EntityTypes.ZOMBIE.create(nether, EntitySpawnReason.COMMAND);
        assertTrue(zombie != null, "Failed to create the Nether bed test mob");
        zombie.setNoAi(true);
        zombie.setPos(Vec3.atCenterOf(headPos.relative(Direction.EAST)));
        nether.addFreshEntity(zombie);
        float health = zombie.getHealth();
        Vec3 velocity = zombie.getDeltaMovement();

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(headPos),
                Direction.UP,
                headPos,
                false
        );
        nether.getBlockState(headPos).useWithoutItem(nether, player, hit);

        assertTrue(zombie.getHealth() == health, "Protected mob took actual bed explosion damage");
        assertTrue(
                zombie.getDeltaMovement().equals(velocity),
                "Protected mob received actual bed explosion knockback"
        );
        zombie.discard();
        nether.removeBlock(footPos, false);
        nether.removeBlock(headPos, false);
    }

    private static void verifyOtherDoesNotOverrideBed(GameTestHelper helper) {
        BlastproofConfig.updateEntry(BlastproofConfig.SECTION_MOB_DAMAGE, "bed", false);
        BlastproofConfig.updateEntry(BlastproofConfig.SECTION_MOB_DAMAGE, "other", true);

        Vec3 center = helper.absoluteVec(new Vec3(2.5, 2.0, 2.5));
        var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, 3.5F, 2.0F, 2.5F);
        float health = zombie.getHealth();

        explode(
                helper.getLevel(),
                null,
                TypedExplosionDamageCalculator.wrap(null, BlastproofExplosionType.BED),
                center
        );

        assertTrue(
                zombie.getHealth() < health || zombie.getDeltaMovement().lengthSqr() > 0.0,
                "The other setting incorrectly overrode the explicit bed setting"
        );
    }

    private static void verifyRespawnAnchorImmunity(GameTestHelper helper) {
        BlastproofConfig.updateEntry(BlastproofConfig.SECTION_MOB_DAMAGE, "respawn_anchor", true);

        BlockPos anchorPos = new BlockPos(2, 1, 2);
        helper.setBlock(
                anchorPos,
                Blocks.RESPAWN_ANCHOR.defaultBlockState().setValue(RespawnAnchorBlock.CHARGE, 1)
        );
        var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, 3.5F, 1.0F, 2.5F);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(new Vec3(1.5, 1.0, 2.5)));
        float health = zombie.getHealth();
        Vec3 velocity = zombie.getDeltaMovement();

        helper.useBlock(anchorPos, player);

        assertTrue(zombie.getHealth() == health, "Protected mob took respawn-anchor damage");
        assertTrue(
                zombie.getDeltaMovement().equals(velocity),
                "Protected mob received respawn-anchor knockback"
        );
    }

    private static void verifyTntRemainsIndependent(GameTestHelper helper) {
        BlastproofConfig.updateEntry(BlastproofConfig.SECTION_MOB_DAMAGE, "tnt", false);
        BlastproofConfig.updateEntry(BlastproofConfig.SECTION_MOB_DAMAGE, "other", true);

        Vec3 center = helper.absoluteVec(new Vec3(2.5, 2.0, 2.5));
        var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, 3.5F, 2.0F, 2.5F);
        PrimedTnt tnt = helper.spawn(EntityTypes.TNT, center);
        float health = zombie.getHealth();

        explode(helper.getLevel(), tnt, null, center);

        assertTrue(
                zombie.getHealth() < health || zombie.getDeltaMovement().lengthSqr() > 0.0,
                "The other setting incorrectly overrode the explicit TNT setting"
        );
    }

    private static void explode(
            ServerLevel level,
            Entity source,
            net.minecraft.world.level.ExplosionDamageCalculator calculator,
            Vec3 center
    ) {
        var damageSource = source == null
                ? level.damageSources().badRespawnPointExplosion(center)
                : level.damageSources().explosion(source, source);
        level.explode(
                source,
                damageSource,
                calculator,
                center,
                2.5F,
                false,
                Level.ExplosionInteraction.BLOCK
        );
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    @Override
    public void invokeTestMethod(GameTestHelper helper, Method method) throws ReflectiveOperationException {
        helper.setBlock(0, 0, 0, Blocks.AIR);
        method.invoke(this, helper);
    }
}
