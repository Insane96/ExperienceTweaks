package insane96mcp.experiencetweaks.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.experiencetweaks.module.anvil.AnvilXpCost;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@OnlyIn(Dist.CLIENT)
@Mixin(value = AnvilScreen.class, priority = 1001)
public class AnvilScreenMixin {
    @ModifyExpressionValue(method = "renderLabels", at = @At(value = "CONSTANT", args = "intValue=40"))
    private int experiencetweaks$tooExpensiveCap(int cap) {
        return AnvilXpCost.getClientRepairCap();
    }
}
