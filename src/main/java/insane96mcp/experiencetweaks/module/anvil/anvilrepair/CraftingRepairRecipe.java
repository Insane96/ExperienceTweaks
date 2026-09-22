package insane96mcp.experiencetweaks.module.anvil.anvilrepair;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * Generic, datapack-driven crafting-grid repair recipe: repairs any item matching {@code itemToRepair}
 * using {@code repairMaterial} (both can be a single item or a tag, e.g. {@code #experiencetweaks:...}),
 * consuming up to {@code amountRequired} materials for a full repair from 0 durability. Used by the
 * Amethyst Repair data pack, but not tied to it — any datapack can add its own recipe json.
 */
public class CraftingRepairRecipe extends CustomRecipe {
    private final ObjTag<Item> itemToRepair;
    private final ObjTag<Item> repairMaterial;
    private final float amountRequired;

    public CraftingRepairRecipe(CraftingBookCategory category, ObjTag<Item> itemToRepair, ObjTag<Item> repairMaterial, float amountRequired) {
        super(category);
        this.itemToRepair = itemToRepair;
        this.repairMaterial = repairMaterial;
        this.amountRequired = amountRequired;
    }

    public ObjTag<Item> getItemToRepair() {
        return this.itemToRepair;
    }

    public ObjTag<Item> getRepairMaterial() {
        return this.repairMaterial;
    }

    public float getAmountRequired() {
        return this.amountRequired;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack repairableItem = null;
        int materialSlots = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty())
                continue;

            if (this.repairMaterial.matches(stack.getItem())) {
                materialSlots++;
            } else if (stack.isDamageableItem() && stack.isDamaged() && this.itemToRepair.matches(stack.getItem())) {
                //Don't go further if there's more than 1 repairable item
                if (repairableItem != null)
                    return false;
                repairableItem = stack;
            } else {
                return false;
            }
        }

        return repairableItem != null && materialSlots > 0;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack repairableItem = null;
        int materialSlots = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty())
                continue;

            if (this.repairMaterial.matches(stack.getItem()))
                materialSlots++;
            else if (stack.isDamageableItem())
                repairableItem = stack;
        }
        if (repairableItem == null || materialSlots == 0)
            return ItemStack.EMPTY;

        // Copy every component of the item being repaired (enchantments, custom name, other mods' data, ...) and
        // only ever touch its damage below, so nothing about the item is lost through the repair.
        ItemStack resultStack = repairableItem.copy();

        double repairPerMaterial = resultStack.getMaxDamage() / (double) this.amountRequired;
        int materialsUsed = (int) Math.min(materialSlots, Math.ceil(resultStack.getDamageValue() / repairPerMaterial));
        int newDamage = (int) Math.max(0, resultStack.getDamageValue() - Math.round(materialsUsed * repairPerMaterial));
        resultStack.setDamageValue(newDamage);

        return resultStack;
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnvilBetterRepair.CRAFTING_REPAIR_RECIPE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<CraftingRepairRecipe> {
        private static final Codec<ObjTag<Item>> OBJ_TAG_CODEC = Codec.STRING.comapFlatMap(
                s -> DataResult.success(ObjTag.of(s, Registries.ITEM)),
                ObjTag::toSerializedString
        );
        private static final StreamCodec<RegistryFriendlyByteBuf, ObjTag<Item>> OBJ_TAG_STREAM_CODEC =
                ByteBufCodecs.STRING_UTF8.<ObjTag<Item>>map(s -> ObjTag.of(s, Registries.ITEM), ObjTag::toSerializedString).cast();

        private static final MapCodec<CraftingRepairRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                        OBJ_TAG_CODEC.fieldOf("item_to_repair").forGetter(CraftingRepairRecipe::getItemToRepair),
                        OBJ_TAG_CODEC.fieldOf("repair_material").forGetter(CraftingRepairRecipe::getRepairMaterial),
                        Codec.FLOAT.fieldOf("amount").forGetter(CraftingRepairRecipe::getAmountRequired)
                ).apply(instance, CraftingRepairRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, CraftingRepairRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                OBJ_TAG_STREAM_CODEC, CraftingRepairRecipe::getItemToRepair,
                OBJ_TAG_STREAM_CODEC, CraftingRepairRecipe::getRepairMaterial,
                ByteBufCodecs.FLOAT, CraftingRepairRecipe::getAmountRequired,
                CraftingRepairRecipe::new
        );

        @Override
        public MapCodec<CraftingRepairRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CraftingRepairRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
