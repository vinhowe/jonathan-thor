package vin.howe.thor;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.MessageType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class Thor implements ModInitializer {
    @Override
    public void onInitialize() {

    }

    public static void summonLightning(PlayerEntity player, @Nullable Entity target) {
        BlockPos blockPos = target != null ? target.getBlockPos() : player.getBlockPos();
//            if (this.world.isSkyVisible(blockPos)) {
        LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(player.world);
        assert lightningEntity != null;
        lightningEntity.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(blockPos));
        lightningEntity.setCosmetic(true);
        if (target != null) {
            target.damage(DamageSource.LIGHTNING_BOLT, 1000.0f);
        }
        player.world.spawnEntity(lightningEntity);
        player.playSound(SoundEvents.ITEM_TRIDENT_THUNDER, 5.0F, 1.0F);
//            }
    }

    public static boolean doBlowout(PlayerEntity player, float distance) {
        if (distance < 8 || player.world.isClient || !(player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof AxeItem || player.getStackInHand(Hand.OFF_HAND).getItem() instanceof AxeItem)) {
            return false;
        }

        float multiplier = (float) (3.0 / (1.0 + Math.pow(Math.E, -((0.08d * distance) - 3.0))));

//        player.sendMessage(Text.of(distance + " " + multiplier), false);
        player.getServer().getPlayerManager().broadcastChatMessage(Text.of("FRICK IT'S THOR"), MessageType.GAME_INFO, Util.NIL_UUID);
        player.getHungerManager().add(100, 2.0f);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 500, 30));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 500, 30));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 500, 30));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 500, 30));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 10, (int) (4 * Math.pow(Math.max(multiplier, 1), 2))));
        float f = Math.min(multiplier, 5);
        player.world.createExplosion(player, player.getX(), player.getY(), player.getZ(), (float) 4 * f, true, Explosion.DestructionType.BREAK);
        AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(player.world, player.getX(), player.getY(), player.getZ());
        areaEffectCloudEntity.setRadius(2.5F);
        areaEffectCloudEntity.setRadiusOnUse(-0.5F);
        areaEffectCloudEntity.setWaitTime(10);
        areaEffectCloudEntity.setDuration(areaEffectCloudEntity.getDuration() / 2);
        areaEffectCloudEntity.setRadiusGrowth(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());

        player.world.spawnEntity(areaEffectCloudEntity);
        return true;
    }
}
