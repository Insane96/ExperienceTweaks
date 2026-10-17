package insane96mcp.experiencetweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import insane96mcp.experiencetweaks.module.experience.DroppedExperience;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ExperienceOrb.class)
public class ExperienceOrbMixin {
	// Apply the global multiplier before the amount is split into orbs, otherwise new orbs could merge into already multiplied ones
	// (ExperienceOrb#tryMergeToExisting compares the existing orb's value with the raw amount) and skip the multiplier
	@WrapMethod(method = "award")
	private static void experiencetweaks$applyGlobalMultiplierOnAward(ServerLevel level, Vec3 pos, int amount, Operation<Void> original) {
		if (!DroppedExperience.isGlobalMultiplierActive() || DroppedExperience.awardingMultipliedExperience) {
			original.call(level, pos, amount);
			return;
		}

		DroppedExperience.awardingMultipliedExperience = true;
		try {
			original.call(level, pos, DroppedExperience.applyGlobalMultiplier(amount, level.getRandom()));
		}
		finally {
			DroppedExperience.awardingMultipliedExperience = false;
		}
	}
}
