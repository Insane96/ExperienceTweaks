package insane96mcp.experiencetweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.experiencetweaks.module.experience.PlayerExperience;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @Shadow
    public int experienceLevel;

    @ModifyReturnValue(method = "getXpNeededForNextLevel", at = @At("RETURN"))
    private int experiencetweaks$xpForNextLevel(int original) {
        int exp = PlayerExperience.getBetterScalingLevel(this.experienceLevel);
		return exp != -1 ? exp : original;
	}

    @ModifyReturnValue(method = "getBaseExperienceReward", at = @At(value = "RETURN", ordinal = 0))
    private int experiencetweaks$getExperiencePoints(int original) {
        int exp = PlayerExperience.getExperienceOnDeath((Player) (Object) this, original, false);
		return exp != -1 ? exp : original;
	}
}
