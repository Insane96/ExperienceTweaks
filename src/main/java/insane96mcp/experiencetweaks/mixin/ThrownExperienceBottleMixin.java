package insane96mcp.experiencetweaks.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.experiencetweaks.module.experience.DroppedExperience;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownExperienceBottle.class)
public class ThrownExperienceBottleMixin {

	@Definition(id = "random", field = "Lnet/minecraft/world/entity/projectile/throwableitemprojectile/ThrownExperienceBottle;random:Lnet/minecraft/util/RandomSource;")
	@Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
	@Expression("? + this.random.nextInt(?) + this.random.nextInt(?)")
	@ModifyExpressionValue(method = "onHit", at = @At("MIXINEXTRAS:EXPRESSION"))
	public int onHit(int original) {
		return DroppedExperience.getXpBottleDroppedExperience((ThrownExperienceBottle) (Object) this, original);
	}
}
