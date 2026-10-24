package gg.itzkatze.katze.lobby.cosmetics;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum BlockCosmetic implements Cosmetic {

    BRICKS("bricks", Material.BRICKS, "Bricks", Rarity.COMMON),
    CHERRY_PLANKS("cherry_planks", Material.CHERRY_PLANKS, "Cherry Planks", Rarity.COMMON),
    PALE_OAK_PLANKS("pale_oak_planks", Material.PALE_OAK_PLANKS, "Pale Oak Planks", Rarity.COMMON),
    BAMBOO_PLANKS("bamboo_planks", Material.BAMBOO_PLANKS, "Bamboo Planks", Rarity.COMMON),
    PURPUR_BLOCK("purpur_block", Material.PURPUR_BLOCK, "Purpur Block", Rarity.COMMON),
    QUARTZ_BLOCK("quartz_block", Material.QUARTZ_BLOCK, "Quartz Block", Rarity.COMMON),
    TUFF_BRICKS("tuff_bricks", Material.TUFF_BRICKS, "Tuff Bricks", Rarity.UNCOMMON),
    MAGMA_BLOCK("magma_block", Material.MAGMA_BLOCK, "Magma Block", Rarity.UNCOMMON),
    SEA_LANTERN("sea_lantern", Material.SEA_LANTERN, "Sea Lantern", Rarity.UNCOMMON),
    SHROOMLIGHT("shroomlight", Material.SHROOMLIGHT, "Shroomlight", Rarity.RARE),
    WAXED_BLOCK_OF_COPPER("waxed_block_of_copper", Material.WAXED_COPPER_BLOCK, "Waxed Copper Block", Rarity.RARE),
    WAXED_CUT_COPPER("waxed_cut_copper", Material.WAXED_CUT_COPPER, "Waxed Cut Copper", Rarity.RARE),
    RED_MUSHROOM_BLOCK("red_mushroom_block", Material.RED_MUSHROOM_BLOCK, "Red Mushroom Block", Rarity.RARE),
    BROWN_MUSHROOM_BLOCK("brown_mushroom_block", Material.BROWN_MUSHROOM_BLOCK, "Brown Mushroom Block", Rarity.RARE),
    TUBE_CORAL_BLOCK("tube_coral_block", Material.TUBE_CORAL_BLOCK, "Tube Coral Block", Rarity.RARE),
    BRAIN_CORAL_BLOCK("brain_coral_block", Material.BRAIN_CORAL_BLOCK, "Brain Coral Block", Rarity.RARE),
    BUBBLE_CORAL_BLOCK("bubble_coral_block", Material.BUBBLE_CORAL_BLOCK, "Bubble Coral Block", Rarity.RARE),
    FIRE_CORAL_BLOCK("fire_coral_block", Material.FIRE_CORAL_BLOCK, "Fire Coral Block", Rarity.RARE),
    HORN_CORAL_BLOCK("horn_coral_block", Material.HORN_CORAL_BLOCK, "Horn Coral Block", Rarity.RARE),
    AMETHYST_BLOCK("amethyst_block", Material.AMETHYST_BLOCK, "Amethyst Block", Rarity.RARE),
    EMERALD_ORE("emerald_ore", Material.EMERALD_ORE, "Emerald Ore", Rarity.RARE),
    REDSTONE_ORE("redstone_ore", Material.REDSTONE_ORE, "Redstone Ore", Rarity.RARE),
    GOLD_ORE("gold_ore", Material.GOLD_ORE, "Gold Ore", Rarity.RARE),
    DIAMOND_ORE("diamond_ore", Material.DIAMOND_ORE, "Diamond Ore", Rarity.RARE),
    BLOCK_OF_RAW_GOLD("block_of_raw_gold", Material.RAW_GOLD_BLOCK, "Block Of Raw Gold", Rarity.RARE),
    BLOCK_OF_RESIN("block_of_resin", Material.RESIN_BLOCK, "Block Of Resin", Rarity.RARE),
    GILDED_BLACKSTONE("gilded_blackstone", Material.GILDED_BLACKSTONE, "Gilded Blackstone", Rarity.RARE),
    NETHERITE_BLOCK("netherite_block", Material.NETHERITE_BLOCK, "Netherite Block", Rarity.EPIC),
    HONEYCOMB_BLOCK("honeycomb_block", Material.HONEYCOMB_BLOCK, "Honeycomb Block", Rarity.EPIC),
    VERDANT_FROGLIGHT("verdant_froglight", Material.VERDANT_FROGLIGHT, "Verdant Froglight", Rarity.LEGENDARY),
    OCHRE_FROGLIGHT("ochre_froglight", Material.OCHRE_FROGLIGHT, "Ochre Froglight", Rarity.LEGENDARY),
    PEARLESCENT_FROGLIGHT("pearlescent_froglight", Material.PEARLESCENT_FROGLIGHT, "Pearlescent Froglight", Rarity.LEGENDARY),
    BEACON("beacon", Material.BEACON, "Beacon", Rarity.MYTHIC),

    JACK_O_LANTERN("jack_o_lantern", Material.JACK_O_LANTERN, "Jack o'Lantern", Rarity.RARE, Season.HALLOWEEN),
    FRESH_SNOW("fresh_snow", Material.SNOW_BLOCK, "Fresh Snow", Rarity.RARE, Season.CHRISTMAS),
    MELON("melon", Material.MELON, "Melon", Rarity.RARE, Season.SUMMER),
    ;

    private final String id;
    private final Material material;
    private final String displayName;
    private final Rarity rarity;
    private final @Nullable Season season;

    BlockCosmetic(String id, Material material, String displayName, Rarity rarity) {
        this(id, material, displayName, rarity, null);
    }

    BlockCosmetic(String id, Material material, String displayName, Rarity rarity, @Nullable Season season) {
        this.id = id;
        this.material = material;
        this.displayName = displayName;
        this.rarity = rarity;
        this.season = season;
    }

    @Override
    public @NotNull String id() {
        return id;
    }

    @Override
    public @NotNull CosmeticCategory category() {
        return CosmeticCategory.BLOCK;
    }

    @Override
    public @NotNull String displayName() {
        return displayName;
    }

    @Override
    public @NotNull Rarity rarity() {
        return rarity;
    }

    @Override
    public @NotNull Material icon() {
        return material;
    }

    @Override
    public @Nullable Season season() {
        return season;
    }

    public @NotNull Material material() {
        return material;
    }
}
