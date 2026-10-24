package gg.itzkatze.katze.lobby.cosmetics;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum SoundCosmetic implements Cosmetic {

    PLING("pling", "block.note_block.pling", "Pling", Rarity.COMMON, Material.NOTE_BLOCK),
    HARP("harp", "block.note_block.harp", "Harp", Rarity.COMMON, Material.NOTE_BLOCK),
    CHIME("chime", "block.note_block.chime", "Chime", Rarity.COMMON, Material.NOTE_BLOCK),
    BELL("bell", "block.note_block.bell", "Bell", Rarity.COMMON, Material.BELL),
    XYLOPHONE("xylophone", "block.note_block.xylophone", "Xylophone", Rarity.COMMON, Material.NOTE_BLOCK),
    BIT("bit", "block.note_block.bit", "Bit", Rarity.COMMON, Material.NOTE_BLOCK),
    BANJO("banjo", "block.note_block.banjo", "Banjo", Rarity.COMMON, Material.NOTE_BLOCK),
    COW_BELL("cow_bell", "block.note_block.cow_bell", "Cow Bell", Rarity.COMMON, Material.NOTE_BLOCK),
    IRON_XYLOPHONE("iron_xylophone", "block.note_block.iron_xylophone", "Iron Xylophone", Rarity.COMMON, Material.IRON_INGOT),
    PLING_LOW("pling_low", "block.note_block.pling", "Low Pling", Rarity.COMMON, Material.NOTE_BLOCK),

    AMETHYST_CHIME("amethyst_chime", "block.amethyst_block.chime", "Amethyst Chime", Rarity.UNCOMMON, Material.AMETHYST_SHARD),
    AMETHYST_HIT("amethyst_hit", "block.amethyst_block.hit", "Amethyst Hit", Rarity.UNCOMMON, Material.AMETHYST_SHARD),
    EXPERIENCE_ORB("experience_orb", "entity.experience_orb.pickup", "Experience Orb", Rarity.UNCOMMON, Material.EXPERIENCE_BOTTLE),
    ITEM_PICKUP("item_pickup", "entity.item.pickup", "Item Pickup", Rarity.UNCOMMON, Material.HOPPER),
    BUTTON_CLICK("button_click", "ui.button.click", "Button Click", Rarity.UNCOMMON, Material.STONE_BUTTON),
    LEVER_CLICK("lever_click", "block.lever.click", "Lever Click", Rarity.UNCOMMON, Material.LEVER),
    ARMOR_EQUIP("armor_equip", "item.armor.equip_iron", "Armor Equip", Rarity.UNCOMMON, Material.IRON_CHESTPLATE),

    SNOW_BLOCK("snow_block", "block.snow.hit", "Schneeblock", Rarity.UNCOMMON, Material.SNOW_BLOCK),
    FOX("fox", "entity.fox.ambient", "Fuchs", Rarity.UNCOMMON, Material.SWEET_BERRIES),
    SNOW_GOLEM("snow_golem", "entity.snow_golem.ambient", "Snowgolem", Rarity.UNCOMMON, Material.CARVED_PUMPKIN),
    FROG("frog", "entity.frog.ambient", "Frosch Quark", Rarity.UNCOMMON, Material.FROGSPAWN),

    LEVEL_UP("level_up", "entity.player.levelup", "Level Up", Rarity.UNCOMMON, Material.EXPERIENCE_BOTTLE),
    FIREWORK_TWINKLE("firework_twinkle", "entity.firework_rocket.twinkle", "Firework Twinkle", Rarity.UNCOMMON, Material.FIREWORK_ROCKET),
    VILLAGER_YES("villager_yes", "entity.villager.yes", "Villager Yes", Rarity.UNCOMMON, Material.EMERALD),
    EXPERIENCE_BOTTLE("experience_bottle", "entity.experience_bottle.throw", "Experience Bottle", Rarity.UNCOMMON, Material.EXPERIENCE_BOTTLE),
    CHICKEN_POP("chicken_pop", "entity.chicken.egg", "Chicken Pop", Rarity.UNCOMMON, Material.EGG),
    NOTE_BLOCK_BASS("note_block_bass", "block.note_block.bass", "Bass Note", Rarity.UNCOMMON, Material.NOTE_BLOCK),
    NOTE_BLOCK_GUITAR("note_block_guitar", "block.note_block.guitar", "Guitar Note", Rarity.UNCOMMON, Material.NOTE_BLOCK),
    NOTE_BLOCK_DIDGERIDOO("note_block_didgeridoo", "block.note_block.didgeridoo", "Didgeridoo", Rarity.UNCOMMON, Material.NOTE_BLOCK),
    NOTE_BLOCK_COW_BELL("note_block_cow_bell", "block.note_block.cow_bell", "Cowbell Note", Rarity.UNCOMMON, Material.NOTE_BLOCK),

    WITCH_CACKLE("witch_cackle", "entity.witch.celebrate", "Witch Cackle", Rarity.RARE, Material.CAULDRON, Season.HALLOWEEN);

    public static final Sound DEFAULT = Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 1f, 1.2f);

    private final String id;
    private final Sound sound;
    private final String displayName;
    private final Rarity rarity;
    private final Material icon;
    private final @Nullable Season season;

    SoundCosmetic(String id, String key, String displayName, Rarity rarity, Material icon) {
        this(id, key, displayName, rarity, icon, null);
    }

    SoundCosmetic(String id, String key, String displayName, Rarity rarity, Material icon, @Nullable Season season) {
        this.id = id;
        this.sound = Sound.sound(Key.key(key), Sound.Source.PLAYER, 1f, 1f);
        this.displayName = displayName;
        this.rarity = rarity;
        this.icon = icon;
        this.season = season;
    }

    @Override
    public @NotNull String id() {
        return id;
    }

    @Override
    public @NotNull CosmeticCategory category() {
        return CosmeticCategory.SOUND;
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
        return icon;
    }

    @Override
    public @Nullable Season season() {
        return season;
    }

    public @NotNull Sound sound() {
        return sound;
    }
}
