package insane96mcp.experiencetweaks.module.anvil;

import insane96mcp.experiencetweaks.module.ETModules;
import insane96mcp.experiencetweaks.network.message.AnvilConfigSyncMessage;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@LoadFeature(module = ETModules.ANVIL, description = "Modifies the XP cost for repairing and merging items in an anvil.")
public class AnvilXpCost extends Feature {

	@Config(min = 0, description = "Cap for the XP level cost when repairing or merging items in an anvil. Vanilla is 40.")
	public static Integer repairCap = 1024;
	@Config(min = 0d, description = "Multiplier for the XP levels required to repair or merge an item. Set to 0 for no cost.")
	public static Double multiplier = 0.0d;
	@Config(description = "The XP cost when merging two items is calculated from the resulting item's enchantments rather than the vanilla formula.")
	public static Boolean mergingCostIsBasedOffResult = true;
	@Config(min = 0d, description = "XP level cost per total enchantment level on the item when repairing with materials. Unenchanted items cost nothing. Overrides the multiplier for material repairs.")
	public static Double xpCostPerEnchantmentLevel = 0.5d;

	/**
	 * Client side only, the server's repair cap (see {@link AnvilConfigSyncMessage}). -1 if not yet received.
	 */
	public static int syncedRepairCap = -1;

	public static boolean isMergingCostBasedOffResult() {
		return Feature.isEnabled(AnvilXpCost.class) && mergingCostIsBasedOffResult;
	}

	public static double getMultiplier() {
		return Feature.isEnabled(AnvilXpCost.class) ? multiplier : 1.0;
	}

	public static double getXpCostPerEnchantmentLevel() {
		return Feature.isEnabled(AnvilXpCost.class) ? xpCostPerEnchantmentLevel : -1.0;
	}

	public static int getRepairCap() {
		return Feature.isEnabled(AnvilXpCost.class) ? repairCap : 40;
	}

	public static int getClientRepairCap() {
		return syncedRepairCap >= 0 ? syncedRepairCap : getRepairCap();
	}

	//Sent regardless of the feature being enabled, so the client gets the vanilla cap when it's disabled on the server
	@SubscribeEvent
	public void onDataPackSync(OnDatapackSyncEvent event) {
		if (event.getPlayer() == null)
			event.getPlayerList().getPlayers().forEach(AnvilConfigSyncMessage::sync);
		else
			AnvilConfigSyncMessage.sync(event.getPlayer());
	}
}
