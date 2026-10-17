package insane96mcp.experiencetweaks.network.message;

import insane96mcp.experiencetweaks.ExperienceTweaks;
import insane96mcp.experiencetweaks.module.anvil.AnvilXpCost;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Syncs the server's anvil config values that the client needs (the config is COMMON, so it's not synced by NeoForge)
 */
public record AnvilConfigSyncMessage(int repairCap) implements CustomPacketPayload {
    public static final Type<AnvilConfigSyncMessage> TYPE =
            new Type<>(ExperienceTweaks.location("sync_anvil_config"));

    public static final StreamCodec<ByteBuf, AnvilConfigSyncMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AnvilConfigSyncMessage::repairCap,
            AnvilConfigSyncMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final AnvilConfigSyncMessage payload, final IPayloadContext context) {
        context.enqueueWork(() -> AnvilXpCost.syncedRepairCap = payload.repairCap());
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new AnvilConfigSyncMessage(AnvilXpCost.getRepairCap()));
    }
}
