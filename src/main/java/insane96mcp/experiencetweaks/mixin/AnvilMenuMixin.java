package insane96mcp.experiencetweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.experiencetweaks.module.anvil.AnvilHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {
	// Cleared before AnvilCraftEvent.Pre is fired, where AnvilDegradation and AnvilRenaming set it
	@Inject(method = "onTake", at = @At("HEAD"))
	private void experiencetweaks$clearBreakChance(Player player, ItemStack carried, CallbackInfo ci) {
		AnvilHandler.BREAK_CHANCE.remove();
	}

	// NeoForge no longer has an event to change the anvil break chance, the vanilla chance is in the lambda passed to ContainerLevelAccess#execute
	@ModifyExpressionValue(method = "lambda$onTake$0", at = @At(value = "CONSTANT", args = "floatValue=0.12F"))
	private static float experiencetweaks$breakChance(float original) {
		Float breakChance = AnvilHandler.BREAK_CHANCE.get();
		return breakChance != null ? breakChance : original;
	}

	@Inject(method = "onTake", at = @At("RETURN"))
	private void experiencetweaks$resetBreakChance(Player player, ItemStack carried, CallbackInfo ci) {
		AnvilHandler.BREAK_CHANCE.remove();
	}
}
