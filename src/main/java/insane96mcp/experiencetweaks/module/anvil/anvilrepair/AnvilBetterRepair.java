package insane96mcp.experiencetweaks.module.anvil.anvilrepair;

import insane96mcp.experiencetweaks.ExperienceTweaks;
import insane96mcp.experiencetweaks.module.ETModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
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
