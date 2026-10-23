package gg.itzkatze.mcc.lobby.cosmetics;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MusicCosmetic implements Cosmetic {

    DISC_13("disc_13", "music_disc.13", "13", Rarity.UNCOMMON, Material.MUSIC_DISC_13, 179),
    CAT("cat", "music_disc.cat", "Cat", Rarity.RARE, Material.MUSIC_DISC_CAT, 186),
    BLOCKS("blocks", "music_disc.blocks", "Blocks", Rarity.UNCOMMON, Material.MUSIC_DISC_BLOCKS, 346),
    CHIRP("chirp", "music_disc.chirp", "Chirp", Rarity.RARE, Material.MUSIC_DISC_CHIRP, 186),
    FAR("far", "music_disc.far", "Far", Rarity.RARE, Material.MUSIC_DISC_FAR, 175),
    MALL("mall", "music_disc.mall", "Mall", Rarity.UNCOMMON, Material.MUSIC_DISC_MALL, 198),
    MELLOHI("mellohi", "music_disc.mellohi", "Mellohi", Rarity.UNCOMMON, Material.MUSIC_DISC_MELLOHI, 97),
    STAL("stal", "music_disc.stal", "Stal", Rarity.RARE, Material.MUSIC_DISC_STAL, 151),
    STRAD("strad", "music_disc.strad", "Strad", Rarity.RARE, Material.MUSIC_DISC_STRAD, 189),
    DISC_11("disc_11", "music_disc.11", "11", Rarity.EPIC, Material.MUSIC_DISC_11, 72),
    WARD("ward", "music_disc.ward", "Ward", Rarity.RARE, Material.MUSIC_DISC_WARD, 252),
    WAIT("wait", "music_disc.wait", "Wait", Rarity.RARE, Material.MUSIC_DISC_WAIT, 238),
    OTHERSIDE("otherside", "music_disc.otherside", "Otherside", Rarity.EPIC, Material.MUSIC_DISC_OTHERSIDE, 196),
    PIGSTEP("pigstep", "music_disc.pigstep", "Pigstep", Rarity.LEGENDARY, Material.MUSIC_DISC_PIGSTEP, 149),
    CREATOR("creator", "music_disc.creator", "Creator", Rarity.RARE, Material.MUSIC_DISC_CREATOR, 177),
    PRECIPICE("precipice", "music_disc.precipice", "Precipice", Rarity.RARE, Material.MUSIC_DISC_PRECIPICE, 300),
    RELIC("relic", "music_disc.relic", "Relic", Rarity.EPIC, Material.MUSIC_DISC_RELIC, 219),
    DISC_5("disc_5", "music_disc.5", "5", Rarity.EPIC, Material.MUSIC_DISC_5, 179),
    TEARS("tears", "music_disc.tears", "Tears", Rarity.UNCOMMON, Material.MUSIC_DISC_TEARS, 176),
    LAVA_CHICKEN("lava_chicken", "music_disc.lava_chicken", "Lava Chicken", Rarity.LEGENDARY, Material.MUSIC_DISC_LAVA_CHICKEN, 135),
    BOUNCE("bounce", "music_disc.bounce", "Bounce", Rarity.UNCOMMON, Material.MUSIC_DISC_BOUNCE, 235),

    SONG_MINECRAFT("song_minecraft", "minecraftchallenges:music.minecraft", "Minecraft", Rarity.LEGENDARY, Material.GRASS_BLOCK, 261, "music/game/minecraft"),
    SONG_CLARK("song_clark", "minecraftchallenges:music.clark", "Clark", Rarity.RARE, Material.GRASS_BLOCK, 202, "music/game/clark"),
    SONG_SWEDEN("song_sweden", "minecraftchallenges:music.sweden", "Sweden", Rarity.LEGENDARY, Material.GRASS_BLOCK, 230, "music/game/sweden"),
    SONG_SUBWOOFER_LULLABY("song_subwoofer_lullaby", "minecraftchallenges:music.subwoofer_lullaby", "Subwoofer Lullaby", Rarity.EPIC, Material.GRASS_BLOCK, 210, "music/game/subwoofer_lullaby"),
    SONG_LIVING_MICE("song_living_mice", "minecraftchallenges:music.living_mice", "Living Mice", Rarity.RARE, Material.GRASS_BLOCK, 188, "music/game/living_mice"),
    SONG_HAGGSTROM("song_haggstrom", "minecraftchallenges:music.haggstrom", "Haggstrom", Rarity.RARE, Material.GRASS_BLOCK, 192, "music/game/haggstrom"),
    SONG_DANNY("song_danny", "minecraftchallenges:music.danny", "Danny", Rarity.RARE, Material.GRASS_BLOCK, 256, "music/game/danny"),
    SONG_KEY("song_key", "minecraftchallenges:music.key", "Key", Rarity.UNCOMMON, Material.GRASS_BLOCK, 62, "music/game/key"),
    SONG_OXYGENE("song_oxygene", "minecraftchallenges:music.oxygene", "Oxygène", Rarity.UNCOMMON, Material.GRASS_BLOCK, 70, "music/game/oxygene"),
    SONG_DRY_HANDS("song_dry_hands", "minecraftchallenges:music.dry_hands", "Dry Hands", Rarity.EPIC, Material.GRASS_BLOCK, 69, "music/game/dry_hands"),
    SONG_WET_HANDS("song_wet_hands", "minecraftchallenges:music.wet_hands", "Wet Hands", Rarity.EPIC, Material.GRASS_BLOCK, 90, "music/game/wet_hands"),
    SONG_MICE_ON_VENUS("song_mice_on_venus", "minecraftchallenges:music.mice_on_venus", "Mice on Venus", Rarity.LEGENDARY, Material.GRASS_BLOCK, 281, "music/game/mice_on_venus"),

    SONG_ARIA_MATH("song_aria_math", "minecraftchallenges:music.aria_math", "Aria Math", Rarity.LEGENDARY, Material.CRAFTING_TABLE, 310, "music/game/creative/aria_math"),
    SONG_BIOME_FEST("song_biome_fest", "minecraftchallenges:music.biome_fest", "Biome Fest", Rarity.UNCOMMON, Material.CRAFTING_TABLE, 383, "music/game/creative/biome_fest"),
    SONG_BLIND_SPOTS("song_blind_spots", "minecraftchallenges:music.blind_spots", "Blind Spots", Rarity.UNCOMMON, Material.CRAFTING_TABLE, 333, "music/game/creative/blind_spots"),
    SONG_DREITON("song_dreiton", "minecraftchallenges:music.dreiton", "Dreiton", Rarity.RARE, Material.CRAFTING_TABLE, 497, "music/game/creative/dreiton"),
    SONG_HAUNT_MUSKIE("song_haunt_muskie", "minecraftchallenges:music.haunt_muskie", "Haunt Muskie", Rarity.RARE, Material.CRAFTING_TABLE, 362, "music/game/creative/haunt_muskie"),
    SONG_TASWELL("song_taswell", "minecraftchallenges:music.taswell", "Taswell", Rarity.EPIC, Material.CRAFTING_TABLE, 515, "music/game/creative/taswell"),

    SONG_BALLAD_OF_THE_CATS("song_ballad_of_the_cats", "minecraftchallenges:music.ballad_of_the_cats", "Ballad of the Cats", Rarity.RARE, Material.NETHERRACK, 280, "music/game/nether/ballad_of_the_cats"),
    SONG_CONCRETE_HALLS("song_concrete_halls", "minecraftchallenges:music.concrete_halls", "Concrete Halls", Rarity.RARE, Material.NETHERRACK, 260, "music/game/nether/concrete_halls"),
    SONG_DEAD_VOXEL("song_dead_voxel", "minecraftchallenges:music.dead_voxel", "Dead Voxel", Rarity.RARE, Material.NETHERRACK, 304, "music/game/nether/dead_voxel"),
    SONG_WARMTH("song_warmth", "minecraftchallenges:music.warmth", "Warmth", Rarity.EPIC, Material.NETHERRACK, 240, "music/game/nether/warmth"),

    SONG_THE_END("song_the_end", "minecraftchallenges:music.the_end", "The End", Rarity.LEGENDARY, Material.END_STONE, 905, "music/game/end/the_end"),
    SONG_ALPHA("song_alpha", "minecraftchallenges:music.alpha", "Alpha", Rarity.LEGENDARY, Material.END_STONE, 603, "music/game/end/alpha"),
    SONG_BOSS("song_boss", "minecraftchallenges:music.boss", "Boss", Rarity.EPIC, Material.END_STONE, 345, "music/game/end/boss"),

    SONG_BEGINNING_2("song_beginning_2", "minecraftchallenges:music.beginning_2", "Beginning 2", Rarity.UNCOMMON, Material.PAINTING, 183, "music/menu/beginning_2"),
    SONG_FLOATING_TREES("song_floating_trees", "minecraftchallenges:music.floating_trees", "Floating Trees", Rarity.RARE, Material.PAINTING, 245, "music/menu/floating_trees"),
    SONG_MOOG_CITY_2("song_moog_city_2", "minecraftchallenges:music.moog_city_2", "Moog City 2", Rarity.RARE, Material.PAINTING, 184, "music/menu/moog_city_2"),
    SONG_MUTATION("song_mutation", "minecraftchallenges:music.mutation", "Mutation", Rarity.UNCOMMON, Material.PAINTING, 195, "music/menu/mutation"),

    SONG_AXOLOTL("song_axolotl", "minecraftchallenges:music.axolotl", "Axolotl", Rarity.UNCOMMON, Material.HEART_OF_THE_SEA, 304, "music/game/water/axolotl"),
    SONG_DRAGON_FISH("song_dragon_fish", "minecraftchallenges:music.dragon_fish", "Dragon Fish", Rarity.RARE, Material.HEART_OF_THE_SEA, 373, "music/game/water/dragon_fish"),
    SONG_SHUNIJI("song_shuniji", "minecraftchallenges:music.shuniji", "Shuniji", Rarity.RARE, Material.HEART_OF_THE_SEA, 245, "music/game/water/shuniji");

    private final String id;
    private final Sound sound;
    private final String displayName;
    private final Rarity rarity;
    private final Material icon;
    private final int durationSeconds;
    private final @Nullable String packFile;

    MusicCosmetic(String id, String key, String displayName, Rarity rarity, Material icon, int durationSeconds) {
        this(id, key, displayName, rarity, icon, durationSeconds, null);
    }

    MusicCosmetic(String id, String key, String displayName, Rarity rarity, Material icon, int durationSeconds, @Nullable String packFile) {
        this.id = id;
        this.sound = Sound.sound(
                Key.key(key),
                Sound.Source.RECORD,
                1f,
                1f
        );
        this.displayName = displayName;
        this.rarity = rarity;
        this.icon = icon;
        this.durationSeconds = durationSeconds;
        this.packFile = packFile;
    }

    public int durationSeconds() {
        return durationSeconds;
    }

    public @Nullable String packFile() {
        return packFile;
    }

    @Override
    public @NotNull String id() {
        return id;
    }

    @Override
    public @NotNull CosmeticCategory category() {
        return CosmeticCategory.MUSIC;
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
        return null;
    }

    public @NotNull Sound sound() {
        return sound;
    }
}
