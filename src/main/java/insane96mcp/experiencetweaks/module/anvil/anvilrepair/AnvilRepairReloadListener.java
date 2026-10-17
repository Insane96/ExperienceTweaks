package insane96mcp.experiencetweaks.module.anvil.anvilrepair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import insane96mcp.experiencetweaks.ExperienceTweaks;
import insane96mcp.experiencetweaks.network.message.AnvilRepairSyncMessage;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = ExperienceTweaks.MOD_ID)
public class AnvilRepairReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
    // Always replaced with a new unmodifiable map instead of being modified in place, since in singleplayer the client
    // (on sync) and the integrated server share this field from different threads
    public static volatile Map<Identifier, AnvilRepair> REPAIRS = Map.of();
    public static final AnvilRepairReloadListener INSTANCE;
    private static final Gson GSON = new GsonBuilder().create();

    static {
        INSTANCE = new AnvilRepairReloadListener();
    }

    public AnvilRepairReloadListener() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("anvil_repairs"));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        HashMap<Identifier, AnvilRepair> repairs = new HashMap<>();
        for (var entry : map.entrySet()) {
            try {
                AnvilRepair anvilRepair = GSON.fromJson(entry.getValue(), AnvilRepair.class);
                repairs.put(entry.getKey(), anvilRepair);
            }
            catch (JsonSyntaxException e) {
                ExperienceTweaks.LOGGER.error("Parsing error loading Anvil Repair {}: {}", entry.getKey(), e.getMessage());
            }
            catch (Exception e) {
                ExperienceTweaks.LOGGER.error("Failed loading Anvil Repair {}: {}", entry.getKey(), e.getMessage());
            }
        }
        setRepairs(repairs);
        ExperienceTweaks.LOGGER.info("Loaded {} Anvil Repairs", REPAIRS.size());
    }

    public static void setRepairs(Map<Identifier, AnvilRepair> repairs) {
        // Not Map.copyOf, to keep a stable iteration order between runs (the first matching repair is used)
        REPAIRS = Collections.unmodifiableMap(new HashMap<>(repairs));
    }

    @SubscribeEvent
    public static void onDataPackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            event.getPlayerList().getPlayers().forEach(player -> AnvilRepairSyncMessage.sync(REPAIRS, player));
        }
        else {
            AnvilRepairSyncMessage.sync(REPAIRS, event.getPlayer());
        }
    }
}
