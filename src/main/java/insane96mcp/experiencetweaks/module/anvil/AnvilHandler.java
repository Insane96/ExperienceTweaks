package insane96mcp.experiencetweaks.module.anvil;

import insane96mcp.experiencetweaks.ExperienceTweaks;
import insane96mcp.experiencetweaks.module.anvil.anvilrepair.AnvilBetterRepair;
import insane96mcp.experiencetweaks.module.anvil.anvilrepair.AnvilRepair;
import insane96mcp.insanelib.core.feature.Feature;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = ExperienceTweaks.MOD_ID)
public class AnvilHandler {

    private static final int COST_RENAME = 1;

    /**
     * Anvil break chance of the anvil operation being taken, set on AnvilCraftEvent.Pre and read by mixin.AnvilMenuMixin. Null to use the vanilla chance.
     * ThreadLocal since in singleplayer both the client and the integrated server take the anvil result.
     */
    public static final ThreadLocal<Float> BREAK_CHANCE = new ThreadLocal<>();

    // Rejections cancel the event, which empties the output and resets the cost

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        boolean renamingNoCost = AnvilRenaming.isNoCost();
        if (!Feature.isEnabled(AnvilXpCost.class)
                && !Feature.isEnabled(AnvilMaterialRepair.class)
                && !Feature.isEnabled(AnvilBetterRepair.class)
                && !renamingNoCost)
            return;
        // The event is fired after the vanilla result is computed, if another mod has changed it, leave it as is
        AnvilUpdateEvent.VanillaResult vanillaResult = event.getVanillaResult();
        if (!ItemStack.matches(event.getOutput(), vanillaResult.output())
                || event.getXpCost() != vanillaResult.xpCost()
                || event.getMaterialCost() != vanillaResult.materialCost())
            return;

        ItemStack left = event.getLeft();
        if (left.isEmpty()) return;
        if (!EnchantmentHelper.canStoreEnchantments(left)) return;

        Player player = event.getPlayer();
        ItemStack right = event.getRight();
        String itemName = event.getName() != null ? event.getName() : "";

        ItemStack resultStack = left.copy();
        // Enchanted books keep their enchantments in STORED_ENCHANTMENTS, not ENCHANTMENTS
        ItemEnchantments leftEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(resultStack);

        Map<Holder<Enchantment>, Integer> resultEnchantMap = new HashMap<>();
        for (Holder<Enchantment> holder : leftEnchantments.keySet()) {
            resultEnchantMap.put(holder, leftEnchantments.getLevel(holder));
        }

        int mergeCost = 0;
        int baseCost = left.getOrDefault(DataComponents.REPAIR_COST, 0) + right.getOrDefault(DataComponents.REPAIR_COST, 0);
        int repairItemCountCost = 0;
        boolean isRenaming = false;
        boolean isMaterialRepair = false;

        if (!right.isEmpty()) {
            boolean isEnchantedBook = right.is(Items.ENCHANTED_BOOK)
                    && !right.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();

            Optional<AnvilRepair.RepairData> oRepairData = AnvilBetterRepair.getCustomAnvilRepair(left, right);

            if (resultStack.isDamageableItem() && (left.isValidRepairItem(right) ||oRepairData.isPresent())) {
                isMaterialRepair = true;
                if (oRepairData.isPresent()) {
                    // Custom material repair
                    AnvilRepair.RepairData repairData = oRepairData.get();
                    int maxPartialRepairDmg = Mth.ceil(resultStack.getMaxDamage() * (1f - repairData.maxRepair()));
                    float amountRequired = repairData.amountRequired();

                    float increaseMultiplier = AnvilMaterialRepair.getIncreaseMaterialsRequiredWithEnchantments();
                    if (increaseMultiplier > 0f && left.isEnchanted()) {
                        float increase = 0f;
                        for (Holder<Enchantment> holder : leftEnchantments.keySet())
                            increase += increaseMultiplier * leftEnchantments.getLevel(holder);
                        amountRequired *= 1f + increase;
                    }
                    float increaseFlat = AnvilMaterialRepair.getIncreaseMaterialsRequiredWithEnchantmentsFlat();
                    if (increaseFlat > 0f && left.isEnchanted()) {
                        float increase = 0f;
                        for (Holder<Enchantment> holder : leftEnchantments.keySet())
                            increase += increaseFlat * leftEnchantments.getLevel(holder);
                        amountRequired += increase * repairData.costMultiplier();
                    }

                    float repairSteps = Math.min(resultStack.getDamageValue(), resultStack.getMaxDamage() / amountRequired);
                    if (repairSteps <= 0 || resultStack.getDamageValue() <= maxPartialRepairDmg) {
                        event.setCanceled(true);
                        return;
                    }
                    float damageValue = resultStack.getDamageValue();
                    for (repairItemCountCost = 0; repairSteps > 0 && repairItemCountCost < right.getCount() && damageValue > maxPartialRepairDmg; ++repairItemCountCost) {
                        damageValue -= repairSteps;
                        ++mergeCost;
                        repairSteps = Math.min(damageValue, resultStack.getMaxDamage() / amountRequired);
                    }
                    resultStack.setDamageValue((int) Math.max(maxPartialRepairDmg, damageValue));
                } else {
                    // Standard material repair
                    float amountRequired = 4f;
                    float increaseMultiplier = AnvilMaterialRepair.getIncreaseMaterialsRequiredWithEnchantments();
                    if (increaseMultiplier > 0f && left.isEnchanted()) {
                        float increase = 0f;
                        for (Holder<Enchantment> holder : leftEnchantments.keySet())
                            increase += increaseMultiplier * leftEnchantments.getLevel(holder);
                        amountRequired *= 1f + increase;
                    }
                    float increaseFlat = AnvilMaterialRepair.getIncreaseMaterialsRequiredWithEnchantmentsFlat();
                    if (increaseFlat > 0f && left.isEnchanted()) {
                        float increase = 0f;
                        for (Holder<Enchantment> holder : leftEnchantments.keySet())
                            increase += increaseFlat * leftEnchantments.getLevel(holder);
                        amountRequired += increase;
                    }
                    int repairSteps = (int) Math.min(resultStack.getDamageValue(), resultStack.getMaxDamage() / amountRequired);
                    if (repairSteps <= 0) {
                        event.setCanceled(true);
                        return;
                    }
                    for (repairItemCountCost = 0; repairSteps > 0 && repairItemCountCost < right.getCount(); ++repairItemCountCost) {
                        resultStack.setDamageValue(resultStack.getDamageValue() - repairSteps);
                        ++mergeCost;
                        repairSteps = (int) Math.min(resultStack.getDamageValue(), resultStack.getMaxDamage() / amountRequired);
                    }
                }
            } else {
                // Merging / enchantment book application
                if (!isEnchantedBook && (!resultStack.is(right.getItem()) || !resultStack.isDamageableItem())) {
                    event.setCanceled(true);
                    return;
                }

                if (resultStack.isDamageableItem() && !isEnchantedBook
                        && AnvilMaterialRepair.isAllowMergingItems()) {
                    int leftDurabilityLeft = left.getMaxDamage() - left.getDamageValue();
                    int rightMaxDamage = right.getMaxDamage();
                    float rightDurabilityLeft = rightMaxDamage - right.getDamageValue();
                    // Scale to the result's max damage scale, since right's max damage (e.g. from a rune/enchantment
                    // that only left has) can differ from left/result's, making raw durability points not directly comparable.
                    if (rightMaxDamage > 0 && rightMaxDamage != resultStack.getMaxDamage())
                        rightDurabilityLeft = rightDurabilityLeft / rightMaxDamage * resultStack.getMaxDamage();
                    if (left.isEnchanted() && AnvilMaterialRepair.decreaseRepairAmountWithEnchantments > 0f) {
                        float mult = 1f - AnvilMaterialRepair.decreaseRepairAmountWithEnchantments.floatValue();
                        for (Holder<Enchantment> holder : leftEnchantments.keySet())
                            rightDurabilityLeft *= (float) Math.pow(mult, leftEnchantments.getLevel(holder));
                    }
                    float resultDurabilityLeft = leftDurabilityLeft + rightDurabilityLeft;
                    resultDurabilityLeft += resultDurabilityLeft * (AnvilMaterialRepair.mergingRepairBonus / 100f);
                    int damageValue = Math.max(0, resultStack.getMaxDamage() - (int) resultDurabilityLeft);
                    if (damageValue < resultStack.getDamageValue()) {
                        resultStack.setDamageValue(damageValue);
                        boolean costBasedOffResult = AnvilXpCost.isMergingCostBasedOffResult();
                        if (!costBasedOffResult)
                            mergeCost += 2;
                    }
                }

                ItemEnchantments rightEnchantments = isEnchantedBook
                        ? right.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                        : right.getEnchantments();
                boolean canEnchant = false;
                boolean cannotEnchant = false;

                for (Holder<Enchantment> rightHolder : rightEnchantments.keySet()) {
                    int leftLvl = resultEnchantMap.getOrDefault(rightHolder, 0);
                    int rightLvl = rightEnchantments.getLevel(rightHolder);
                    // Combining two equal levels never goes above the max level, while over-max levels (e.g. from commands) are kept
                    rightLvl = (leftLvl == rightLvl && rightLvl < rightHolder.value().getMaxLevel()) ? rightLvl + 1 : Math.max(rightLvl, leftLvl);

                    // Like NeoForge, respect IItemExtension#supportsEnchantment, which also handles Enchanted Books
                    boolean canEnchant2 = left.supportsEnchantment(rightHolder);
                    if (player.getAbilities().instabuild)
                        canEnchant2 = true;

                    for (Holder<Enchantment> leftHolder : resultEnchantMap.keySet()) {
                        if (!leftHolder.equals(rightHolder) && !Enchantment.areCompatible(rightHolder, leftHolder)) {
                            canEnchant2 = false;
                            if (!AnvilXpCost.isMergingCostBasedOffResult())
                                ++mergeCost;
                        }
                    }

                    if (!canEnchant2) {
                        cannotEnchant = true;
                    } else {
                        canEnchant = true;
                        resultEnchantMap.put(rightHolder, rightLvl);

                        int enchantCost = rightHolder.value().getAnvilCost();
                        if (isEnchantedBook)
                            enchantCost = Math.max(1, enchantCost / 2);
                        if (!AnvilXpCost.isMergingCostBasedOffResult())
                            mergeCost += enchantCost * rightLvl;
                    }
                }

                if (AnvilXpCost.isMergingCostBasedOffResult()) {
                    for (Map.Entry<Holder<Enchantment>, Integer> entry : resultEnchantMap.entrySet()) {
                        int enchantCost = entry.getKey().value().getAnvilCost();
                        if (isEnchantedBook)
                            enchantCost = Math.max(1, enchantCost / 2);
                        mergeCost += enchantCost * entry.getValue();
                    }
                }

                // Pure durability merge with no enchantments — no XP cost
                if (!right.isEnchanted() && !right.is(Items.ENCHANTED_BOOK))
                    mergeCost = 0;

                if (cannotEnchant && !canEnchant) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        // Renaming
        if (StringUtils.isBlank(itemName)) {
            if (left.has(DataComponents.CUSTOM_NAME)) {
                isRenaming = true;
                resultStack.remove(DataComponents.CUSTOM_NAME);
            }
        } else if (!itemName.equals(left.getHoverName().getString())) {
            isRenaming = true;
            resultStack.set(DataComponents.CUSTOM_NAME, Component.literal(itemName));
        }

        // Final cost
        double xpPerEnchantLevel = AnvilXpCost.getXpCostPerEnchantmentLevel();
        int finalCost;
        if (isMaterialRepair && xpPerEnchantLevel >= 0) {
            int totalEnchantLevels = 0;
            for (Holder<Enchantment> holder : leftEnchantments.keySet())
                totalEnchantLevels += leftEnchantments.getLevel(holder);
            float percentRepaired = (float)(left.getDamageValue() - resultStack.getDamageValue()) / left.getMaxDamage();
            finalCost = (int) Math.round(totalEnchantLevels * xpPerEnchantLevel * percentRepaired);
        } else {
            double multiplier = AnvilXpCost.getMultiplier();
            finalCost = (int) Math.round((baseCost + mergeCost) * multiplier);
        }

        if (isRenaming && !AnvilRenaming.isNoCost())
            finalCost += COST_RENAME;
        if (isRenaming && AnvilRenaming.isNoCost() && mergeCost <= 0)
            finalCost = 0;

        if (!isRenaming && right.isEmpty()) {
            event.setCanceled(true);
            return;
        }

        int repairCap = AnvilXpCost.getRepairCap();
        if (finalCost >= repairCap && !player.getAbilities().instabuild) {
            // Not canceled, otherwise the cost is reset to 0 and "Too Expensive" isn't shown
            event.setOutput(ItemStack.EMPTY);
            event.setXpCost(finalCost);
            event.setMaterialCost(0);
            return;
        }

        // Update repair cost component and enchantments on result
        double multiplier = isMaterialRepair && xpPerEnchantLevel >= 0 ? 0.0 : AnvilXpCost.getMultiplier();
        if (multiplier != 0) {
            int toolRepairCost = resultStack.getOrDefault(DataComponents.REPAIR_COST, 0);
            if (!right.isEmpty() && toolRepairCost < right.getOrDefault(DataComponents.REPAIR_COST, 0))
                toolRepairCost = right.getOrDefault(DataComponents.REPAIR_COST, 0);
            if (mergeCost >= 1)
                toolRepairCost = AnvilMenu.calculateIncreasedRepairCost(toolRepairCost);
            resultStack.set(DataComponents.REPAIR_COST, toolRepairCost);
        }

        ItemEnchantments.Mutable mutableEnchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Map.Entry<Holder<Enchantment>, Integer> e : resultEnchantMap.entrySet()) {
            mutableEnchants.set(e.getKey(), e.getValue());
        }
        EnchantmentHelper.setEnchantments(resultStack, mutableEnchants.toImmutable());

        event.setOutput(resultStack);
        event.setXpCost(finalCost);
        event.setMaterialCost(repairItemCountCost);
    }
}
