package insane96mcp.experiencetweaks.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import insane96mcp.experiencetweaks.module.anvil.anvilrepair.AnvilRepair;
import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmiAnvilRepairRecipe extends BasicEmiRecipe {

	private final List<Component> repairTooltip;

	public EmiAnvilRepairRecipe(ResourceLocation id, ItemStack itemToRepair, AnvilRepair.RepairData repairData) {
		super(VanillaEmiRecipeCategories.ANVIL_REPAIRING, id, 125, 18);
		ItemStack item = itemToRepair.copy();
		item.setDamageValue(itemToRepair.getMaxDamage() - 1);
		this.inputs.add(EmiStack.of(item));
		this.inputs.add(EmiIngredient.of(ingredientFromObjTag(repairData.repairMaterial()), 1));
		item = itemToRepair.copy();
		int maxDamage = item.getMaxDamage();
		// Same cap as AnvilHandler: the item can't be repaired past max_repair
		int maxPartialRepairDmg = Mth.ceil(maxDamage * (1f - repairData.maxRepair()));
		int damage = (maxDamage - 1) - (int) (maxDamage / repairData.amountRequired());
		item.setDamageValue(Math.max(maxPartialRepairDmg, damage));
		this.outputs.add(EmiStack.of(item));
		this.repairTooltip = buildRepairTooltip(repairData);
	}

	private static List<Component> buildRepairTooltip(AnvilRepair.RepairData repairData) {
		List<Component> tooltip = new ArrayList<>();
		float amount = repairData.amountRequired();
		float maxRepair = repairData.maxRepair();
		tooltip.add(Component.translatable("emi.experiencetweaks.anvil_repair.per_material", formatNumber(Math.min(100f, 100f / amount))));
		if (maxRepair < 1f) {
			tooltip.add(Component.translatable("emi.experiencetweaks.anvil_repair.max_repair", formatNumber(maxRepair * 100f)).withStyle(ChatFormatting.RED));
			tooltip.add(Component.translatable("emi.experiencetweaks.anvil_repair.materials_to_max", Mth.ceil(amount * maxRepair)).withStyle(ChatFormatting.GRAY));
		}
		else {
			tooltip.add(Component.translatable("emi.experiencetweaks.anvil_repair.materials_full", Mth.ceil(amount)).withStyle(ChatFormatting.GRAY));
		}
		return tooltip;
	}

	private static String formatNumber(float value) {
		if (value == (int) value)
			return String.valueOf((int) value);
		return String.format(Locale.ROOT, "%.1f", value);
	}

	static Ingredient ingredientFromObjTag(ObjTag<Item> objTag) {
		Holder<Item> holder = objTag.asHolder();
		if (holder != null)
			return Ingredient.of(holder.value());
		String str = objTag.toSerializedString();
		return Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse(str.substring(1))));
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.PLUS, 27, 3);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
		widgets.addSlot(inputs.get(0), 0, 0);
		widgets.addSlot(inputs.get(1), 49, 0);
		widgets.addTooltipText(this.repairTooltip, 75, 1, 24, 17);
		var outputSlot = widgets.addSlot(outputs.getFirst(), 107, 0).recipeContext(this);
		for (Component line : this.repairTooltip)
			outputSlot.appendTooltip(line);
	}
}
