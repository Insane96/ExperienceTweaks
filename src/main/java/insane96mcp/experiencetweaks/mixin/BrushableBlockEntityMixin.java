package insane96mcp.experiencetweaks.mixin;

import insane96mcp.experiencetweaks.module.experience.DroppedExperience;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrushableBlockEntity.class)
public class BrushableBlockEntityMixin {
    @Inject(method = "brushingCompleted", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrushableBlockEntity;dropContent(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER))
    public void experiencetweaks$xpOnBrushingCompleted(ServerLevel level, LivingEntity user, ItemStack brush, CallbackInfo ci) {
        DroppedExperience.tryGenerateBrushXp(user);
    }
}
