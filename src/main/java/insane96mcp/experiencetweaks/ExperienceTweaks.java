package insane96mcp.experiencetweaks;

import com.mojang.logging.LogUtils;
import insane96mcp.experiencetweaks.module.ETModules;
import insane96mcp.experiencetweaks.module.anvil.anvilrepair.AnvilBetterRepair;
import insane96mcp.experiencetweaks.module.anvil.anvilrepair.AnvilRepairReloadListener;
import insane96mcp.experiencetweaks.network.NetworkHandler;
import insane96mcp.insanelib.setup.ILModConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

@Mod(ExperienceTweaks.MOD_ID)
public class ExperienceTweaks {
    public static final String MOD_ID = "experiencetweaks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ILModConfig CONFIG;

    public ExperienceTweaks(IEventBus eventBus, ModContainer modContainer) {
        CONFIG = new ILModConfig(MOD_ID, ModConfig.Type.COMMON, eventBus, ETModules::init, ExperienceTweaks.class.getClassLoader());
        modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec);

        eventBus.addListener(NetworkHandler::register);
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(AnvilRepairReloadListener.INSTANCE));
        AnvilBetterRepair.RECIPE_SERIALIZERS.register(eventBus);
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
