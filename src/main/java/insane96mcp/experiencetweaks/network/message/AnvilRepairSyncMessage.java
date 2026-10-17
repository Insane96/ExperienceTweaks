package insane96mcp.experiencetweaks.network.message;

import insane96mcp.experiencetweaks.ExperienceTweaks;
import insane96mcp.experiencetweaks.module.anvil.anvilrepair.AnvilRepair;
import insane96mcp.experiencetweaks.module.anvil.anvilrepair.AnvilRepairReloadListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record AnvilRepairSyncMessage(Map<ResourceLocation, AnvilRepair> repairs) implements CustomPacketPayload {
    public static final Type<AnvilRepairSyncMessage> TYPE =
            new Type<>(ExperienceTweaks.location("sync_anvil_repairs"));

    public static final StreamCodec<FriendlyByteBuf, AnvilRepairSyncMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> buf.writeMap(msg.repairs(), FriendlyByteBuf::writeResourceLocation, (b, repair) -> repair.toNetwork(b)),
            buf -> new AnvilRepairSyncMessage(buf.readMap(FriendlyByteBuf::readResourceLocation, AnvilRepair::fromNetwork))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final AnvilRepairSyncMessage payload, final IPayloadContext context) {
        context.enqueueWork(() -> AnvilRepairReloadListener.setRepairs(payload.repairs()));
    }

    public static void sync(Map<ResourceLocation, AnvilRepair> repairs, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new AnvilRepairSyncMessage(new HashMap<>(repairs)));
    }
}
