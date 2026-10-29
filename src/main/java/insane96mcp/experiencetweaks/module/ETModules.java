package insane96mcp.experiencetweaks.module;

import insane96mcp.experiencetweaks.ExperienceTweaks;
import insane96mcp.insanelib.core.feature.Module;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;

public class ETModules {
	public static final String EXPERIENCE = ExperienceTweaks.MOD_ID + ":experience";
	public static final String ANVIL      = ExperienceTweaks.MOD_ID + ":anvil";

	public static void init(IEventBus eventBus) {
		create(EXPERIENCE, "Experience", eventBus);
		create(ANVIL, "Anvil", eventBus);
	}

	public static void create(String id, String name, IEventBus eventBus) {
		Module.Builder.create(ResourceLocation.parse(id), name, ModConfig.Type.SERVER, eventBus).build();
	}
}
