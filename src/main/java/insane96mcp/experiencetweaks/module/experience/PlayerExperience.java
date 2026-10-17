package insane96mcp.experiencetweaks.module.experience;

import insane96mcp.experiencetweaks.module.ETModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@LoadFeature(module = ETModules.EXPERIENCE, description = "Changes the experience lost on death and xp per level required.")
public class PlayerExperience extends Feature {

	@Config(min = -1, description = """
						The experience required to level up will be this xp each level.
						Set to -1 to disable.
						Incompatible with Allurement's 'Remove level Scaling' or any other mod that does something similar.""")
	public static Integer linearLevelUpFormula = 40;
	@Config(min = -1d, max = 1d, description = """
						On death, players will drop this percentage of experience instead of max 7 levels. Setting to -1 will disable this.
						Due to Minecraft limitations this is incompatible with other mods that change the level scaling (e.g. Allurement's 'Remove level Scaling').""")
	public static Double droppedExperienceOnDeath = 0.8d;
	@Config
	public static Boolean pickUpExperienceFaster = true;

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Pre event) {
		if (!this.isEnabled()
				|| !pickUpExperienceFaster)
			return;

		if (event.getEntity().takeXpDelay > 0)
			event.getEntity().takeXpDelay--;
	}

	/**
	 * Returns -1 when the module/feature is not enabled, otherwise the calculated experience required to level up
	 */
	public static int getBetterScalingLevel(int currentLevel) {
		if (!isEnabled(PlayerExperience.class))
			return -1;

		return linearLevelUpFormula;
	}

	//Instead of using experienceTotal, calculate the xp from the xp bar and level since experienceTotal doesn't get updated on level consumption
	private static int getTotalExperience(Player player) {
		double totalExp = getExperienceForLevel(player.experienceLevel);
		totalExp += player.getXpNeededForNextLevel() * player.experienceProgress;
		//Prevent overflowing with huge levels (e.g. given via commands)
		return (int) Math.min(totalExp, Integer.MAX_VALUE);
	}

	/**
	 * Total experience required to go from level 0 to the specified level. Uses closed formulas instead of summing each level, since levels can go up to Integer.MAX_VALUE
	 */
	private static double getExperienceForLevel(int level) {
		int betterScalingXp = getBetterScalingLevel(level);
		if (betterScalingXp != -1)
			return (double) level * betterScalingXp;
		//Sums of the vanilla xp per level: 7 + level * 2 (0-15), 37 + (level - 15) * 5 (15-30), 112 + (level - 30) * 9 (30+)
		double l = level;
		if (level <= 16)
			return l * l + 6 * l;
		else if (level <= 31)
			return 2.5 * l * l - 40.5 * l + 360;
		else
			return 4.5 * l * l - 162.5 * l + 2220;
	}

	//Take into account global experience to prevent XP duping
	private static int compensateGlobalXPMultiplier(int experience) {
		if (!Feature.isEnabled(DroppedExperience.class) || DroppedExperience.globalMultiplier == 1d)
			return experience;
		//With a 0 multiplier all the dropped experience is discarded anyway
		if (DroppedExperience.globalMultiplier == 0d)
			return 0;
		return (int) (experience / DroppedExperience.globalMultiplier);
	}

	/**
	 * Returns -1 when the module/feature is not enabled, otherwise the experience dropped on death.
	 * When droppedExperienceOnDeath is disabled, the vanilla experience (vanillaExperience) is used, still compensated for the global multiplier
	 */
	public static int getExperienceOnDeath(Player player, int vanillaExperience, boolean ignoreGlobalXPMultiplier) {
		if (!isEnabled(PlayerExperience.class)
				|| (droppedExperienceOnDeath < 0 && !DroppedExperience.isEnabled(DroppedExperience.class))
				|| player.level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.KEEP_INVENTORY)
				|| player.isSpectator())
			return -1;

		int experience = droppedExperienceOnDeath < 0 ? vanillaExperience : (int) (getTotalExperience(player) * droppedExperienceOnDeath);
		if (!ignoreGlobalXPMultiplier)
			experience = compensateGlobalXPMultiplier(experience);
		return experience;
	}
}