package insane96mcp.experiencetweaks.module.experience;

import insane96mcp.experiencetweaks.mixin.accessor.MobAccessor;
import insane96mcp.experiencetweaks.module.ETModules;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import insane96mcp.insanelib.module.base.NbtTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

@LoadFeature(module = ETModules.EXPERIENCE, description = "Various changes to dropped experience.")
public class DroppedExperience extends Feature {
	public static ResourceLocation XP_PROCESSED;
	//public static final TagKey<Block> NO_BLOCK_XP_MULTIPLIER = ISOBlockTagsProvider.create("no_xp_multiplier");
	//public static final TagKey<EntityType<?>> NO_ENTITY_XP_MULTIPLIER = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.location("no_xp_multiplier"));

	@Config(min = 0d, max = 128d, description = "ALL Experience dropped will be multiplied by this value, regardless if affected by another multiplier.")
	public static Double globalMultiplier = 1d;

	@Config(min = 0d, max = 128d, description = "Experience dropped by blocks (Ores and Spawners) will be multiplied by this multiplier. Experience dropped by blocks are still affected by 'Global Experience Multiplier'\nCan be set to 0 to make blocks drop no experience")
	public static Double blockMultiplier = 1d;
	@Config(min = 0, max = 128d, name = "Mobs.Multiplier: Spawners", description = """
						Experience dropped from mobs that come from spawners will be multiplied by this multiplier.
						Experience dropped by mobs from spawners are still affected by 'Global Experience Multiplier'
						Can be set to 0 to disable experience drop from mob that come from spawners.""")
	public static Double mobs$multiplierSpawner = 1d;

	@Config(min = 0, max = 128d, name = "Mobs.Multiplier: Natural", description = """
						Experience dropped from mobs that DON'T come from spawners will be multiplied by this multiplier.
						Experience dropped from mobs that DON'T come from spawners is still affected by 'Global Experience Multiplier'
						Can be set to 0 to disable experience drop from mob that DON'T come from spawners.""")
	public static Double mobs$multiplierNatural = 1d;

	@Config(min = 0, description = "Vanilla mobs drop 1~4 xp per equipment they have.")
	public static Integer mobs$bonusExperiencePerEquipment = 2;
	@Config(min = 0, description = "This is added to 'Bonus experience per equipment'.")
	public static Integer mobs$bonusExperiencePerEnchantedEquipment = 3;

	@Config(min = 0, max = 512, name = "Bottle o' Enchanting XP", description = "Bottle o' enchanting will drop this amount of experience. Can be set to 0 to make Bottle o' enchanting drop no experience")
	public static Integer xpBottleDroppedXp = 40;

	@Config(min = 0, description = "Experience gained from harvesting Honey or Honeycombs from beehives")
	public static MinMaxConfig beehiveHarvestExperience = new MinMaxConfig(3, 5);
	//@Config(min = 0, description = "Experience obtained when cows or mooshrooms are milked or stewed. This only works if Fluid Cooldown is enabled.")
	//public static MinMaxConfig milkXp = new MinMaxConfig(3, 5);
	@Config(min = 0, description = "Experience obtained when shearing sheep.")
	public static MinMaxConfig shearXp = new MinMaxConfig(2, 3);
	@Config(min = 0, description = "Experience obtained when brushing blocks.")
	public static MinMaxConfig brushingXp = new MinMaxConfig(6, 10);

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		XP_PROCESSED = this.createDataKey("xp_processed");
	}

	/*public static void tryGenerateMilkXp(Entity entity) {
		if (milkXp.min > 0 || milkXp.max > 0)
			entity.level().addFreshEntity(new ExperienceOrb(entity.level(), entity.getX(), entity.getY(), entity.getZ(), milkXp.getIntRandBetween(entity.level().random)));
	}*/

	public static void tryGenerateBeehiveXp(Entity entity) {
		if (!Feature.isEnabled(DroppedExperience.class)
				|| (beehiveHarvestExperience.min == 0 && beehiveHarvestExperience.max == 0))
			return;
		entity.level().addFreshEntity(new ExperienceOrb(entity.level(), entity.getX(), entity.getY(), entity.getZ(), beehiveHarvestExperience.getIntRandBetween(entity.level().random)));
	}

	public static void tryGenerateBrushXp(Entity entity) {
		if (!Feature.isEnabled(DroppedExperience.class)
				|| (brushingXp.min == 0 && brushingXp.max == 0))
			return;
		entity.level().addFreshEntity(new ExperienceOrb(entity.level(), entity.getX(), entity.getY(), entity.getZ(), brushingXp.getIntRandBetween(entity.level().random)));
	}

	public static void tryGenerateSheepShearXp(Entity entity) {
		if (!Feature.isEnabled(DroppedExperience.class)
				|| (shearXp.min == 0 && shearXp.max == 0))
			return;
		entity.level().addFreshEntity(new ExperienceOrb(entity.level(), entity.getX(), entity.getY(), entity.getZ(), shearXp.getIntRandBetween(entity.level().random)));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled())
			return;
		if (event.getEntity() instanceof ExperienceOrb xpOrb)
			handleGlobalExperience(xpOrb);
		handleMobsMultiplier(event);
	}

	private static void handleGlobalExperience(ExperienceOrb xpOrb) {
		if (globalMultiplier == 1d
				|| ModNBTData.get(xpOrb, XP_PROCESSED, Boolean.class)
				|| xpOrb.level().isClientSide)
			return;

		if (globalMultiplier == 0d)
			xpOrb.remove(Entity.RemovalReason.KILLED);
		else
			xpOrb.value *= globalMultiplier;

		ModNBTData.put(xpOrb, XP_PROCESSED, true);
		if (xpOrb.value <= 0d)
			xpOrb.remove(Entity.RemovalReason.KILLED);
	}

	public static void handleMobsMultiplier(EntityJoinLevelEvent event) {
		if ((mobs$multiplierSpawner == 1d && mobs$multiplierNatural == 1d)
				|| !(event.getEntity() instanceof Mob mob)
				/*|| mob.getType().is(NO_ENTITY_XP_MULTIPLIER)*/)
			return;

		if (mob.getSpawnType() == MobSpawnType.SPAWNER)
			NbtTags.setExperienceMultiplier(mobs$multiplierSpawner, mob);
		else
			NbtTags.setExperienceMultiplier(mobs$multiplierNatural, mob);
	}

	//Run before smartness
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBlockXPDrop(BlockDropsEvent event) {
		if (!this.isEnabled()
				/*|| event.getState().is(NO_BLOCK_XP_MULTIPLIER)*/)
			return;

		//handleBlockDrop(event);
		handleMultiplier(event);
	}

	/*private static void handleBlockDrop(BlockEvent.BreakEvent event) {
		int silkTouchLevel = event.getPlayer().getMainHandItem().getEnchantmentLevel(Enchantments.SILK_TOUCH);
		if (silkTouchLevel > 0)
			return;
		for (BlockDefinition blockDefinition : BlockDefinitionReloadListener.DEFINITIONS) {
			if (blockDefinition.matches(event.getState())) {
				int expDropped = blockDefinition.getStateExperienceDropped(event.getLevel().getRandom());
				if (expDropped > -1)
					event.setExpToDrop(expDropped);
			}
		}
	}*/

	private static void handleMultiplier(BlockDropsEvent event) {
		if (blockMultiplier == 1d)
			return;
		event.setDroppedExperience((int) (event.getDroppedExperience() * blockMultiplier));
	}

	// In vanilla, mobs drop loot before checking if they should drop more experience due to gear, this makes them never drop more experience if they drop equipment
	// This sets the xp reward before the loot drops (also changes the xp reward from config options)
	@SubscribeEvent
	public void fixEquipmentExperience(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof Mob mob)
				|| ((MobAccessor) mob).getXpReward() <= 0)
			return;

		int xpReward = ((MobAccessor) mob).getXpReward();
		for (ItemStack stack : mob.getArmorSlots()) {
			if (!stack.isEmpty()) {
				xpReward += mobs$bonusExperiencePerEquipment;
				if (stack.isEnchanted())
					xpReward += mobs$bonusExperiencePerEnchantedEquipment;
			}
		}
		for (ItemStack stack : mob.getHandSlots()) {
			if (!stack.isEmpty()) {
				xpReward += mobs$bonusExperiencePerEquipment;
				if (stack.isEnchanted())
					xpReward += mobs$bonusExperiencePerEnchantedEquipment;
			}
		}
		if (!mob.getBodyArmorItem().isEmpty()) {
			xpReward += mobs$bonusExperiencePerEquipment;
			if (mob.getBodyArmorItem().isEnchanted())
				xpReward += mobs$bonusExperiencePerEnchantedEquipment;
		}

		((MobAccessor) mob).setXpReward(xpReward);
	}

	public static int getXpBottleDroppedExperience(ThrownExperienceBottle thrownExperienceBottle) {
		return xpBottleDroppedXp;
	}
}