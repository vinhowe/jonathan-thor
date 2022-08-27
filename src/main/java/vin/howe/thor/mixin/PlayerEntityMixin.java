package vin.howe.thor.mixin;


import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vin.howe.thor.Thor;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow
    public abstract void sendMessage(Text message, boolean actionBar);

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void prependAttack(Entity target, CallbackInfo ci) {
        if (!target.isAttackable()) {
            return;
        }
        ItemStack itemStack = this.getStackInHand(Hand.MAIN_HAND);
        if (itemStack.getItem() instanceof AxeItem) {
            Thor.doBlowout((PlayerEntity) (Object) this, (float) this.getVelocity().lengthSquared() * 10f);
            Thor.summonLightning((PlayerEntity) (Object) this, target);
        }
    }

    @Redirect(
            method = "handleFallDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;handleFallDamage(FF)Z"
            )
    )
    private boolean redirectSuperFallDamage(LivingEntity livingEntity, float fallDistance, float damageMultiplier) {
        if (Thor.doBlowout((PlayerEntity) (Object) (this), fallDistance)) {
            Thor.summonLightning((PlayerEntity) (Object) this, null);
            return false;
        }
        return super.handleFallDamage(fallDistance, damageMultiplier);
    }
}
