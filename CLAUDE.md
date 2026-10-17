# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the mod jar
./gradlew build

# Run Minecraft client for testing
./gradlew runClient

# Run Minecraft server for testing
./gradlew runServer

# Generate data (recipes, loot tables, etc.)
./gradlew runData

# Clean build outputs
./gradlew clean
```

There is no test suite; manual in-game testing via `runClient`/`runServer` is the standard approach.

## Architecture Overview

This is a **NeoForge mod** for Minecraft 26.1.2 (branch `26.1.2`; the 1.21.1 version lives on branch `1.21.1`), currently named **"Insane's Experience Tweaks"** (mod id `experiencetweaks`, Java package `insane96mcp.experiencetweaks` — renamed from "Insane's Experience Overhaul" in 1.1.2.1; the repo folder name is a leftover from the old name). It uses **InsaneLib** (a custom library by the same author) for configuration management and the modular feature system.

The mod was originally scoped to overhaul the whole enchantment system on top of experience, but that direction was dropped as too large an effort (see `changelog.md` 1.1.2.1) in favor of a separate, not-yet-released mod. This mod's focus going forward is XP tweaks plus the anvil rework; the `enchanting` module is a currently-unused stub (registered in `EOModules` but has no features).

### Entry Point

`ExperienceTweaks.java` — the `@Mod` class. Creates the InsaneLib config, initializes modules via `EOModules::init`, registers the network handler, and registers `AnvilRepairReloadListener` as a datapack reload listener.

### Module System (InsaneLib)

Features are organized into modules defined in `EOModules.java`. Each module is a class extending InsaneLib's `Feature` base, with config fields annotated with `@Config` (bounds, description — supports markdown). Modules are:

- `experiencetweaks:experience` — `PlayerExperience` (XP-per-level formula override, death XP drop percentage, faster XP pickup) and `DroppedExperience` (global/block/mob XP multipliers, split by spawner vs. natural spawn, bonus XP for mob equipment, honey/shear/brush/XP-bottle drops).
- `experiencetweaks:anvil` — fully implemented, not a stub. Features: `AnvilXpCost` (repair cap, cost multiplier, merge-cost-based-on-result toggle, per-enchant-level material repair cost), `AnvilMaterialRepair` (durability-merge behavior, enchantment-scaled material cost), `AnvilRenaming` (free/no-durability-loss renaming), `AnvilDegradation` (anvil break chance, repairing chipped/damaged anvils via the `experiencetweaks:repairs_anvil` item tag), and `anvilrepair/AnvilBetterRepair` (datapack-driven custom repair recipes, see below). All the actual anvil math lives in `AnvilHandler.onAnvilUpdate` (an `AnvilUpdateEvent` listener), which the other Anvil feature classes only configure. On 26.1.2 the event fires *after* the vanilla result is computed: `AnvilHandler` skips if another mod already changed the result (compared to `getVanillaResult()`), and shows "Too Expensive" by setting an empty output with the cost instead of canceling (canceling resets the cost to 0).
- `experiencetweaks:enchanting` — stub, registered but empty.

### Datapack-driven Anvil Repairs

`AnvilBetterRepair` lets datapacks define, per item, which materials repair it and how much durability/damage they restore, instead of vanilla's fixed "4 items = full-ish repair". Recipes live under `data/experiencetweaks/anvil_repairs/*.json` (one file per vanilla tool/armor piece is already shipped) and are loaded by `AnvilRepairReloadListener` into `AnvilRepair` instances. `AnvilHandler` consults these via `AnvilBetterRepair.getCustomAnvilRepair(...)` before falling back to vanilla repair-item logic.

### EMI Integration

**Currently disabled on 26.1.2**: EMI has no 26.1.2 version, so `emi/**` is excluded from the source set and the dependency is commented out in `build.gradle` (marked with TODOs). When enabled, `emi/ETEmiPlugin.java` (an `@EmiEntrypoint`, only active when the optional EMI mod is present) registers the datapack-defined anvil repair recipes as EMI recipes (`EmiAnvilRepairRecipe`) and hides EMI's vanilla anvil-repair/enchanting recipe pages so they don't conflict.

### Mixin System

All game behavior hooks live in `mixin/`. Uses standard Mixin 0.8+ plus **MixinExtras** (bundled with NeoForge, 0.5.5) for expression-based injection patterns. Key mixins:

- `PlayerMixin` — overrides `getXpNeededForNextLevel` (via `PlayerExperience.getBetterScalingLevel`) and `getBaseExperienceReward` for death XP drop (via `PlayerExperience.getExperienceOnDeath`).
- `MobMixin` — overrides `Mob.getBaseExperienceReward` to return the (possibly config/equipment-adjusted) `xpReward` field instead of vanilla's value, when `DroppedExperience` is enabled.
- `accessor.MobAccessor` — exposes get/set on `Mob`'s protected `xpReward` field; used by `DroppedExperience.fixEquipmentExperience` to add bonus XP for a dying mob's equipment (vanilla drops loot before checking equipment-based XP bonus, which this mixin fixes by adjusting `xpReward` in a `LivingDeathEvent` listener before drops happen).
- `BeehiveBlockMixin`, `SheepMixin`, `BrushableBlockEntityMixin` — award XP for honey harvest, shearing, and archaeology brushing.
- `ThrownExperienceBottleMixin` — modifies XP bottle drop amounts via MixinExtras `@Expression`.
- `AnvilMenuMixin` — NeoForge 26.1.2 no longer exposes the anvil break chance (the old `AnvilRepairEvent` is gone), so `AnvilDegradation`/`AnvilRenaming` set `AnvilHandler.BREAK_CHANCE` (a ThreadLocal) on `AnvilCraftEvent.Pre` and this mixin replaces vanilla's `0.12F` in `AnvilMenu.lambda$onTake$0` with it.
- `client.AnvilScreenMixin` (listed under `client` in the mixin config) — overrides the anvil screen's hardcoded "too expensive" level-40 cap (in `extractLabels`) to reflect `AnvilXpCost.repairCap`.

Mixin config: `src/main/resources/experiencetweaks.mixins.json`.

### Networking

`NetworkHandler` registers two server→client messages, both sent on `OnDatapackSyncEvent` (join and `/reload`): `AnvilRepairSyncMessage` syncs the datapack anvil repairs (used by EMI), and `AnvilConfigSyncMessage` syncs the server's anvil `repairCap` (the config is COMMON, so NeoForge doesn't sync it) so the client-side anvil screen (see `AnvilScreenMixin`) shows the right "Too Expensive" cap. Uses NeoForge's `CustomPacketPayload` + `StreamCodec` pattern. The mod is otherwise server-side only.

### Configuration Pattern

Config fields on Feature classes use InsaneLib's `@Config` annotation with `min`/`max` bounds and `description`. Features can be toggled via `@LoadFeature(module = ..., description = ...)`.

## Key Dependencies

| Dependency | Version | Notes |
|---|---|---|
| NeoForge | 26.1.2.116 | Mod loader (Java 25) |
| InsaneLib | 3.0.0.0-beta+ | Required — config/module system |
| MixinExtras | 0.5.5 | Bundled with NeoForge |
| EMI | 1.1.24 | Optional — currently disabled on 26.1.2 (no EMI version available) |

## Vanilla XP Formula Reference

The mod overrides these vanilla level-up XP costs (used in `PlayerExperience`/`PlayerMixin`):
- Levels 0–15: `7 + level * 2`
- Levels 15–30: `37 + (level - 15) * 5`
- Levels 30+: `112 + (level - 30) * 9`

With `linearLevelUpFormula` enabled (default: on, 40 XP/level), every level costs a fixed amount instead.

## Related Repositories

This mod is split off from the 1.20.1-era mods `C:\Users\delvi\source\repos\Insane96\IguanaTweaksReborn\` and `C:\Users\delvi\source\repos\Insane96\IguanaTweaksExpanded\`. Other mods by the same author, including InsaneLib, live under `C:\Users\delvi\source\repos\Insane96\`.

## Working Conventions

- Don't write code unless prompted or explicitly confirmed.

## Minecraft/NeoForge Sources

The decompiled Java sources for Minecraft/NeoForge (`net.neoforged.moddev` plugin) are already extracted to `C:\Users\delvi\.gradle\mc-sources\26.1.2-neoforge\` (1.21.1: `1.21.1-neoforge\`) (normal package layout, e.g. `net/minecraft/world/entity/LivingEntity.java`) — read directly from there with Read/Grep/Glob instead of asking the user.

If missing or needing regeneration, the source jar is in the NeoForm cache at `~/.gradle/caches/neoformruntime/intermediate_results/mergeWithSources_*_output.jar` (pick the most recent by date) — extract it with `unzip` into the folder above, discarding the `.class` files.
