package insane96mcp.experiencetweaks.module.anvil.anvilrepair;

import insane96mcp.experiencetweaks.ExperienceTweaks;
import insane96mcp.experiencetweaks.module.ETModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.IntegratedPack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

@LoadFeature(module = ETModules.ANVIL, description = "Makes repairing items cost less materials instead of an arbitrary 4. Allows custom anvil repair recipes defined via datapacks in data/<modid>/anvil_repairs/.")
public class AnvilBetterRepair extends Feature {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, ExperienceTweaks.MOD_ID);
    /**
     * Generic datapack-driven crafting-grid repair recipe (see {@link CraftingRepairRecipe}), e.g. used by
     * the Amethyst Repair data pack, but any datapack can add its own recipe json under
     * {@code data/<modid>/recipe/} with {@code "type": "experiencetweaks:crafting_special_repair"}.
     */
    public static final DeferredHolder<RecipeSerializer<?>, CraftingRepairRecipe.Serializer> CRAFTING_REPAIR_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("crafting_special_repair", CraftingRepairRecipe.Serializer::new);

    @Config(description = "Enables a data pack that lets you repair damageable metal tools, weapons and armor with Amethyst Shards, either in the crafting grid or in an anvil. The amounts needed for a full repair are set in the data pack itself (data/experiencetweaks/anvil_repairs/amethyst_repair.json for the anvil, data/experiencetweaks/recipe/amethyst_repairing.json for the crafting grid).")
    public static Boolean amethystRepair = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        IntegratedPack.addServerPack(ExperienceTweaks.MOD_ID, "amethyst_repair", "Insane's Experience Tweaks Amethyst Repair", () -> this.isEnabled() && amethystRepair);
    }

    public static Optional<AnvilRepair.RepairData> getCustomAnvilRepair(ItemStack left, ItemStack right) {
        if (!Feature.isEnabled(AnvilBetterRepair.class))
            return Optional.empty();
        for (AnvilRepair repair : AnvilRepairReloadListener.REPAIRS.values()) {
            if (repair.isItemToRepair(left)) {
                Optional<AnvilRepair.RepairData> data = repair.getRepairDataFromMaterial(right);
                if (data.isPresent())
                    return data;
            }
        }
        return Optional.empty();
    }
}
