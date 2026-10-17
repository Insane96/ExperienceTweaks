# 1.1.5.1
* Added Mace (0.75 Breeze Rods), Elytra (3 Phantom Membranes) and Wolf Armor (4.5 Armadillo Scutes) better repair
* Fixed Turtle Shell better repair not working (wrong Turtle Scute ID)

# 1.1.5.0
* Fixed renaming an enchanted book in an anvil removing its enchantments, and combining two enchanted books losing the left book's enchantments
* Fixed anvil prior work penalty never being applied to the XP cost
* Fixed players dropping no experience on death when the global experience multiplier is greater than 1
* Fixed experience orbs removed by the global experience multiplier remaining in the world as "ghost" entities
* Global and block experience multipliers now round randomly instead of truncating (e.g. with a 1.5 multiplier, 1 xp has 50% chance to become 2)
* Fixed some experience orbs skipping the global experience multiplier by merging into already multiplied orbs
* Fixed mobs' bonus experience from equipment being applied even when Dropped Experience is disabled, and being given by equipment picked up by mobs
* Fixed Bottle o' Enchanting experience being changed even when Dropped Experience is disabled
* Fixed players dropping no experience on death when 'Dropped experience on death' is set to -1 and Dropped Experience is enabled
* Anvils now respect items' custom enchantment compatibility from other mods
* Fixed crafting grid repair recipes (e.g. Amethyst Repair) consuming more materials than needed
* Fixed anvils sometimes degrading when only renaming, depending on event order
* Anvils can no longer be repaired in adventure mode
* Combining two items with the same over-max enchantment level (e.g. from commands) no longer lowers it to the max level
* Fixed the first material in a standard anvil repair repairing more than the others when the materials required are increased by enchantments
* Fixed a possible concurrency issue with anvil repairs data in singleplayer
* The anvil "Too Expensive" cap shown on the client is now synced from the server config
* Fixed anvil operations rejected by the mod (e.g. too expensive or over a repair's max repair) falling back to the vanilla anvil result
  * "Too Expensive" is now properly shown when the cost exceeds the repair cap
* Crafting grid repair recipes now reject an amount that's not greater than 0
* Fixed experience dropped on death overflowing with huge levels

# 1.1.4.1
* Fixed crash with latest InsaneLib

# 1.1.4.0
* Anvil Better Repair: added Amethyst Repair option, a separate data pack (toggleable on its own) that lets you repair damageable metal tools, weapons and armor with Amethyst Shards, either in the crafting grid (30 shards for a full repair) or in an anvil (20 shards for a full repair)
* Fixed copper equipment having wrong ID with ISO

# 1.1.3.1
* Fixed anvil durability merging being inconsistent when one of the items has its max damage increased (Notably: Rune enchanting's Enduring)

# 1.1.3.0
* Added Copper equipment better repair
* Added Shields+ better repair

# 1.1.2.2
* Fixed beehives not dropping experience if no campfire was under them
* Fixed mobs with equipment dropping more experience than intended

# 1.1.2.1
* Renamed the mod (Insane's Experience Overhaul -> Insane's Experience Tweaks)
  * Since the original idea of overhauling the enchantments would require a huge effort, I've preferred to focus on making a new mod (coming soon) that mimics the enchantment system without experience. This mod is still useful for anvils even if you disable experience.

# 1.1.2.0
* Added Anvil Repair support to EMI
* Renamed Anvil Custom Repairs to Anvil Better Repair

# 1.1.1.1
* Fixed anvil not working for other mod's custom recipes

# 1.1.1.0
* Removed the game rule to disable experience
  * Moved to Rune Enchanting
* Cost label is no longer shown if cost = 0 instead of showing No Cost

# 1.1.0.0-alpha
* Ported Anvil features
  * Repairing items no longer costs more materials if the item is enchanted, but will instead cost 0.5 levels per enchantment level on it
* Fixed mod not required client side

# 1.0.1.0-alpha
* Added back game rule to disable experience

# 1.0.0.0-alpha
* Initial release