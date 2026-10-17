package insane96mcp.experiencetweaks.module.anvil;

import insane96mcp.experiencetweaks.module.ETModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AnvilCraftEvent;

@LoadFeature(module = ETModules.ANVIL, description = "Removes the XP cost for renaming items and prevents anvil degradation when only renaming.")
public class AnvilRenaming extends Feature {

	@Config(description = "Renaming items in an anvil doesn't cost XP")
	public static Boolean noCost = true;
	@Config(description = "Anvils don't wear down when only used for renaming")
	public static Boolean noBreak = true;

	public static boolean isNoCost() {
		return Feature.isEnabled(AnvilRenaming.class) && noCost;
	}

	//Run after AnvilDegradation, otherwise its break chance overrides this one
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onAnvilCraft(AnvilCraftEvent.Pre event) {
		if (!this.isEnabled() || !noBreak)
			return;
		if (event.getRight().isEmpty() && ItemStack.isSameItem(event.getLeft(), event.getOutput()))
			AnvilHandler.BREAK_CHANCE.set(0f);
	}
}
