package insane96mcp.experiencetweaks.module.anvil;

import insane96mcp.experiencetweaks.ExperienceTweaks;
import insane96mcp.experiencetweaks.module.ETModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@LoadFeature(module = ETModules.ANVIL, description = "Changes the chance for anvils to degrade on use and allows repairing anvils with items in the experiencetweaks:repairs_anvil item tag.")
public class AnvilDegradation extends Feature {

	public static final TagKey<Item> REPAIRS_ANVIL = TagKey.create(Registries.ITEM, ExperienceTweaks.location("repairs_anvil"));

	@Config(min = 0d, max = 1d, description = "Chance for an anvil to become chipped/damaged/break on use. Vanilla is 12%")
	public static Double degradationChance = 0.02d;
	@Config(description = "If true, chipped and damaged anvils can be repaired by right-clicking with an item from the experiencetweaks:repairs_anvil item tag (iron block by default)")
	public static Boolean fixAnvils = true;

	@SubscribeEvent
	public void onAnvilRepair(AnvilRepairEvent event) {
		if (!this.isEnabled())
			return;

		event.setBreakChance(degradationChance.floatValue());
	}

	@SubscribeEvent
	public void onRightClickAnvil(PlayerInteractEvent.RightClickBlock event) {
		if (!this.isEnabled()
				|| !fixAnvils
				|| !event.getItemStack().is(REPAIRS_ANVIL)
				|| !event.getEntity().mayBuild())
			return;
		BlockState state = event.getLevel().getBlockState(event.getPos());
		if (!state.is(BlockTags.ANVIL))
			return;

		Direction direction = state.getValue(AnvilBlock.FACING);
		boolean isChipped = state.is(Blocks.CHIPPED_ANVIL);
		boolean isDamaged = state.is(Blocks.DAMAGED_ANVIL);
		if (!isChipped && !isDamaged)
			return;

		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		//Trigger the advancement with the stack before it's consumed
		if (event.getEntity() instanceof ServerPlayer serverPlayer)
			CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, event.getPos(), event.getItemStack().copy());
		if (!event.getEntity().getAbilities().instabuild)
			event.getItemStack().shrink(1);
		BlockState repairedState = isChipped
				? Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, direction)
				: Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, direction);
		event.getLevel().setBlockAndUpdate(event.getPos(), repairedState);
		event.getLevel().playSound(event.getEntity(), event.getPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1f, 1.5f);
	}
}
