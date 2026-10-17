package insane96mcp.experiencetweaks.mixin;

import insane96mcp.experiencetweaks.module.experience.DroppedExperience;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sheep.class)
public abstract class SheepMixin extends Animal {
	protected SheepMixin(EntityType<? extends Animal> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(method = "shear", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/sheep/Sheep;setSheared(Z)V"))
	private void experiencetweaks$xpOnShear(ServerLevel level, SoundSource soundSource, ItemStack tool, CallbackInfo ci) {
		DroppedExperience.tryGenerateSheepShearXp((Sheep) (Object) this);
	}
}
