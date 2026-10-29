package insane96mcp.experiencetweaks.module.anvil.anvilrepair;

import insane96mcp.experiencetweaks.ExperienceTweaks;
import insane96mcp.experiencetweaks.module.ETModules;
import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.util.IntegratedPack;
import net.neoforged.fml.config.ModConfig;

//COMMON as integrated packs are evaluated before the SERVER config is loaded
@ConfigType(ModConfig.Type.COMMON)
@LoadFeature(module = ETModules.ANVIL, description = "Enables a data pack that lets you repair damageable metal tools, weapons and armor with Amethyst Shards, either in the crafting grid or in an anvil (the anvil one requires Anvil Better Repair). The amounts needed for a full repair are set in the data pack itself (data/experiencetweaks/anvil_repairs/amethyst_repair.json for the anvil, data/experiencetweaks/recipe/amethyst_repairing.json for the crafting grid). Requires re-joining the world.")
public class AmethystRepair extends Feature {
    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        IntegratedPack.addServerPack(ExperienceTweaks.MOD_ID, "amethyst_repair", "Insane's Experience Tweaks Amethyst Repair", this::isEnabled);
    }
}
