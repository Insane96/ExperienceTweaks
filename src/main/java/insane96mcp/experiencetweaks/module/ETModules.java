package insane96mcp.experiencetweaks.module;

import insane96mcp.experiencetweaks.ExperienceTweaks;
import insane96mcp.insanelib.core.feature.Module;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ETModules {
	public static final String EXPERIENCE = ExperienceTweaks.MOD_ID + ":experience";
	public static final String ANVIL      = ExperienceTweaks.MOD_ID + ":anvil";
	public static final String ENCHANTING = ExperienceTweaks.MOD_ID + ":enchanting";

	public static void init(IEventBus eventBus, ModConfigSpec.Builder builder) {
		create(EXPERIENCE, "Experience", eventBus, builder);
		create(ANVIL, "Anvil", eventBus, builder);
		create(ENCHANTING, "Enchanting", eventBus, builder);
	}

	public static void create(String id, String name, IEventBus eventBus, ModConfigSpec.Builder builder) {
		Module.Builder.create(Identifier.parse(id), name, ModConfig.Type.COMMON, builder, eventBus).build();
	}
}
