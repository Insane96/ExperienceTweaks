package insane96mcp.experiencetweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.experiencetweaks.module.anvil.AnvilHandler;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {
	// A canceled AnvilUpdateEvent resets the cost to 0, set it back when AnvilHandler canceled it for being too expensive so the "Too Expensive" label shows up
	@WrapOperation(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/CommonHooks;onAnvilChange(Lnet/minecraft/world/inventory/AnvilMenu;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/Container;Ljava/lang/String;JLnet/minecraft/world/entity/player/Player;)Z"))
	private boolean experiencetweaks$keepTooExpensiveCost(AnvilMenu menu, ItemStack left, ItemStack right, Container outputSlot, String name, long baseCost, Player player, Operation<Boolean> original) {
		AnvilHandler.TOO_EXPENSIVE_COST.remove();
		boolean result = original.call(menu, left, right, outputSlot, name, baseCost, player);
		Integer tooExpensiveCost = AnvilHandler.TOO_EXPENSIVE_COST.get();
		if (tooExpensiveCost != null) {
			AnvilHandler.TOO_EXPENSIVE_COST.remove();
			menu.setMaximumCost(tooExpensiveCost);
		}
		return result;
	}
}
