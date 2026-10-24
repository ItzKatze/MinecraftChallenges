package gg.itzkatze.katze.data;

import lombok.Getter;
import org.bukkit.NamespacedKey;

@Getter
public enum KatzeAdvancement {
    STONE_AGE(NamespacedKey.minecraft("story/mine_stone"), "Stone Age", "Mine Stone with your new Pickaxe"),
    GETTING_AN_UPGRADE(NamespacedKey.minecraft("story/upgrade_tools"), "Getting an Upgrade", "Construct a better Pickaxe"),
    ACQUIRE_HARDWARE(NamespacedKey.minecraft("story/smelt_iron"), "Acquire Hardware", "Smelt an Iron Ingot"),
    SUIT_UP(NamespacedKey.minecraft("story/obtain_armor"), "Suit Up", "Protect yourself with a piece of iron armor"),
    HOT_STUFF(NamespacedKey.minecraft("story/lava_bucket"), "Hot Stuff", "Fill a Bucket with lava"),
    ISNT_IT_IRON_PICK(NamespacedKey.minecraft("story/iron_tools"), "Isn't It Iron Pick", "Upgrade your Pickaxe"),
    NOT_TODAY_THANK_YOU(NamespacedKey.minecraft("story/deflect_arrow"), "Not Today, Thank You", "Deflect a projectile with a Shield"),
    ICE_BUCKET_CHALLENGE(NamespacedKey.minecraft("story/form_obsidian"), "Ice Bucket Challenge", "Obtain a block of Obsidian"),
    DIAMONDS(NamespacedKey.minecraft("story/mine_diamond"), "Diamonds!", "Acquire diamonds"),
    WE_NEED_TO_GO_DEEPER(NamespacedKey.minecraft("story/enter_the_nether"), "We Need to Go Deeper", "Build, light and enter a Nether Portal"),
    COVER_ME_WITH_DIAMONDS(NamespacedKey.minecraft("story/shiny_gear"), "Cover Me with Diamonds", "Diamond armor saves lives"),
    ENCHANTER(NamespacedKey.minecraft("story/enchant_item"), "Enchanter", "Enchant an item at an Enchanting Table"),
    ZOMBIE_DOCTOR(NamespacedKey.minecraft("story/cure_zombie_villager"), "Zombie Doctor", "Weaken and then cure a Zombie Villager"),
    EYE_SPY(NamespacedKey.minecraft("story/follow_ender_eye"), "Eye Spy", "Follow an Eye of Ender"),
    THE_END(NamespacedKey.minecraft("story/enter_the_end"), "The End?", "Enter the End Portal"),
    RETURN_TO_SENDER(NamespacedKey.minecraft("nether/return_to_sender"), "Return to Sender", "Destroy a Ghast with a fireball"),
    THOSE_WERE_THE_DAYS(NamespacedKey.minecraft("nether/find_bastion"), "Those Were the Days", "Enter a Bastion Remnant"),
    HIDDEN_IN_THE_DEPTHS(NamespacedKey.minecraft("nether/obtain_ancient_debris"), "Hidden in the Depths", "Obtain Ancient Debris"),
    SUBSPACE_BUBBLE(NamespacedKey.minecraft("nether/fast_travel"), "Subspace Bubble", "Use the Nether to travel 7 km in the Overworld"),
    A_TERRIBLE_FORTRESS(NamespacedKey.minecraft("nether/find_fortress"), "A Terrible Fortress", "Break your way into a Nether Fortress"),
    WHO_IS_CUTTING_ONIONS(NamespacedKey.minecraft("nether/obtain_crying_obsidian"), "Who is Cutting Onions?", "Obtain Crying Obsidian"),
    OH_SHINY(NamespacedKey.minecraft("nether/distract_piglin"), "Oh Shiny", "Distract Piglins with gold"),
    THIS_BOAT_HAS_LEGS(NamespacedKey.minecraft("nether/ride_strider"), "This Boat Has Legs", "Ride a Strider with a Warped Fungus on a Stick"),
    UNEASY_ALLIANCE(
            NamespacedKey.minecraft("nether/uneasy_alliance"),
            "Uneasy Alliance",
            "Rescue a Ghast from the Nether, bring it safely home to the Overworld... and then kill it"
    ),
    WAR_PIGS(NamespacedKey.minecraft("nether/loot_bastion"), "War Pigs", "Loot a Chest in a Bastion Remnant"),
    COUNTRY_LODE_TAKE_ME_HOME(NamespacedKey.minecraft("adventure/use_lodestone"), "Country Lode, Take Me Home", "Use a Compass on a Lodestone"),
    COVER_ME_IN_DEBRIS(NamespacedKey.minecraft("nether/netherite_armor"), "Cover Me in Debris", "Get a full suit of Netherite armor"),
    SPOOKY_SCARY_SKELETON(NamespacedKey.minecraft("nether/get_wither_skull"), "Spooky Scary Skeleton", "Obtain a Wither Skeleton's skull"),
    INTO_FIRE(NamespacedKey.minecraft("nether/obtain_blaze_rod"), "Into Fire", "Relieve a Blaze of its rod"),
    NOT_QUITE_NINE_LIVES(NamespacedKey.minecraft("nether/charge_respawn_anchor"), "Not Quite 'Nine' Lives", "Charge a Respawn Anchor to the maximum"),
    FEELS_LIKE_HOME(
            NamespacedKey.minecraft("nether/ride_strider_in_overworld_lava"),
            "Feels Like Home",
            "Take a Strider for a loooong ride on a lava lake in the Overworld"
    ),
    HOT_TOURIST_DESTINATION(NamespacedKey.minecraft("nether/explore_nether"), "Hot Tourist Destinations", "Explore all Nether biomes"),
    WITHERING_HEIGHTS(NamespacedKey.minecraft("nether/summon_wither"), "Withering Heights", "Summon the Wither"),
    LOCAL_BREWERY(NamespacedKey.minecraft("nether/brew_potion"), "Local Brewery", "Brew a Potion"),
    BRING_HOME_THE_BEACON(NamespacedKey.minecraft("nether/create_beacon"), "Bring Home the Beacon", "Construct and place a Beacon"),
    A_FURIOUS_COCKTAIL(NamespacedKey.minecraft("nether/all_potions"), "A Furious Cocktail", "Have every potion effect applied at the same time"),
    BEACONATOR(NamespacedKey.minecraft("nether/create_full_beacon"), "Beaconator", "Bring a Beacon to full power"),
    HOW_DID_WE_GET_HERE(NamespacedKey.minecraft("nether/all_effects"), "How Did We Get Here?", "Have every effect applied at the same time"),
    FREE_THE_END(NamespacedKey.minecraft("end/kill_dragon"), "Free the End", "Good luck"),
    REMOTE_GATEWAY(NamespacedKey.minecraft("end/enter_end_gateway"), "Remote Getaway", "Escape the island"),
    THE_END_AGAIN(NamespacedKey.minecraft("end/respawn_dragon"), "The End... Again...", "Respawn the Ender Dragon"),
    YOU_NEED_A_MINT(NamespacedKey.minecraft("end/dragon_breath"), "You Need a Mint", "Collect Dragon's Breath in a Glass Bottle"),
    THE_CITY_AT_THE_END_OF_THE_GAME(NamespacedKey.minecraft("end/find_end_city"), "The City at the End of the Game", "Go on in, what could happen?"),
    SKY_IS_THE_LIMIT(NamespacedKey.minecraft("end/elytra"), "Sky's the Limit", "Find Elytra"),
    GREAT_VIEW_FROM_UP_HERE(NamespacedKey.minecraft("end/levitate"), "Great View From Up Here", "Levitate up 50 blocks from the attacks of a Shulker"),
    VOLUNTARY_EXILE(NamespacedKey.minecraft("adventure/voluntary_exile"), "Voluntary Exile", "Kill a raid captain."),
    IS_IT_A_BIRD(NamespacedKey.minecraft("adventure/spyglass_at_parrot"), "Is It a Bird?", "Look at a Parrot through a Spyglass"),
    MONSTER_HUNTER(NamespacedKey.minecraft("adventure/kill_a_mob"), "Monster Hunter", "Kill any hostile monster"),
    THE_POWER_OF_BOOKS(
            NamespacedKey.minecraft("adventure/read_power_of_chiseled_bookshelf"),
            "The Power of Books",
            "Read the power signal of a Chiseled Bookshelf using a Comparator"
    ),
    WHAT_A_DEAL(NamespacedKey.minecraft("adventure/trade"), "What a Deal!", "Successfully trade with a Villager"),
    CRAFTING_A_NEW_LOOK(NamespacedKey.minecraft("adventure/trim_with_any_armor_pattern"), "Crafting a New Look", "Craft a trimmed armor at a Smithing Table"),
    STICKY_SITUATION(NamespacedKey.minecraft("adventure/honey_block_slide"), "Sticky Situation", "Jump into a Honey Block to break your fall"),
    OL_BETSY(NamespacedKey.minecraft("adventure/ol_betsy"), "Ol' Betsy", "Shoot a Crossbow"),
    SURGE_PROTECTOR(
            NamespacedKey.minecraft("adventure/lightning_rod_with_villager_no_fire"),
            "Surge Protector",
            "Protect a Villager from an undesired shock without starting a fire"
    ),
    CAVES_AND_CLIFFS(
            NamespacedKey.minecraft("adventure/fall_from_world_height"),
            "Caves & Cliffs",
            "Free fall from the top of the world (build limit) to the bottom of the world and survive"
    ),
    RESPECTING_THE_REMNANTS(NamespacedKey.minecraft("adventure/salvage_sherd"), "Respecting the Remnants", "Brush a Suspicious block to obtain a Pottery Sherd"),
    SNEAK_100(NamespacedKey.minecraft("adventure/avoid_vibration"), "Sneak 100", "Sneak near a Sculk Sensor or Warden to prevent it from detecting you"),
    SWEET_DREAMS(NamespacedKey.minecraft("adventure/sleep_in_bed"), "Sweet Dreams", "Sleep in a Bed to change your respawn point"),
    HERO_OF_THE_VILLAGE(NamespacedKey.minecraft("adventure/hero_of_the_village"), "Hero of the Village", "Successfully defend a village from a raid"),
    IS_IT_A_BALLOON(NamespacedKey.minecraft("adventure/spyglass_at_ghast"), "Is It a Balloon?", "Look at a Ghast through a Spyglass"),
    A_THROWAWAY_JOKE(NamespacedKey.minecraft("adventure/throw_trident"), "A Throwaway Joke", "Throw a Trident at something."),
    IT_SPREADS(NamespacedKey.minecraft("adventure/kill_mob_near_sculk_catalyst"), "It Spreads", "Kill a mob near a Sculk Catalyst"),
    TAKE_AIM(NamespacedKey.minecraft("adventure/shoot_arrow"), "Take Aim", "Shoot something with an Arrow"),
    MONSTERS_HUNTED(NamespacedKey.minecraft("adventure/kill_all_mobs"), "Monsters Hunted", "Kill one of every hostile monster"),
    POSTMORTAL(NamespacedKey.minecraft("adventure/totem_of_undying"), "Postmortal", "Use a Totem of Undying to cheat death"),
    HIRED_HELP(NamespacedKey.minecraft("adventure/summon_iron_golem"), "Hired Help", "Summon an Iron Golem to help defend a village"),
    STAR_TRADER(NamespacedKey.minecraft("adventure/trade_at_world_height"), "Star Trader", "Trade with a Villager at the build height limit"),
    SMITHING_WITH_STYLE(
            NamespacedKey.minecraft("adventure/trim_with_all_exclusive_armor_patterns"),
            "Smithing with Style",
            "Apply these smithing templates at least once: Spire, Snout, Rib, Ward, Silence, Vex, Tide, Wayfinder"
    ),
    TWO_BIRDS_ONE_ARROW(NamespacedKey.minecraft("adventure/two_birds_one_arrow"), "Two Birds, One Arrow", "Kill two Phantoms with a piercing Arrow"),
    WHO_IS_THE_PILLAGER_NOW(NamespacedKey.minecraft("adventure/whos_the_pillager_now"), "Who's the Pillager Now?", "Give a Pillager a taste of their own medicine"),
    ARBALISTIC(NamespacedKey.minecraft("adventure/arbalistic"), "Arbalistic", "Kill five unique mobs with one crossbow shot"),
    CAREFUL_RESTAURATION(
            NamespacedKey.minecraft("adventure/craft_decorated_pot_using_only_sherds"),
            "Careful Restoration",
            "Make a Decorated Pot out of 4 Pottery Sherds"
    ),
    ADVENTURING_TIME(NamespacedKey.minecraft("adventure/adventuring_time"), "Adventuring Time", "Discover every biome"),
    SOUND_OF_MUSIC(NamespacedKey.minecraft("adventure/play_jukebox_in_meadows"), "Sound of Music", "Make the Meadows come alive with the sound of music from a Jukebox"),
    LIGHT_AS_A_RABBIT(NamespacedKey.minecraft("adventure/walk_on_powder_snow_with_leather_boots"), "Light as a Rabbit", "Walk on Powder Snow... without sinking in it"),
    IS_IT_A_PLANE(NamespacedKey.minecraft("adventure/spyglass_at_dragon"), "Is It a Plane?", "Look at the Ender Dragon through a Spyglass"),
    VERY_VERY_FRIGHTENING(NamespacedKey.minecraft("adventure/very_very_frightening"), "Very Very Frightening", "Strike a Villager with lightning"),
    SNIPER_DUEL(NamespacedKey.minecraft("adventure/sniper_duel"), "Sniper Duel", "Kill a Skeleton from at least 50 meters away"),
    BULLSEYE(NamespacedKey.minecraft("adventure/bullseye"), "Bullseye", "Hit the bullseye of a Target block from at least 30 meters away"),
    BEE_OUR_GUEST(
            NamespacedKey.minecraft("husbandry/safely_harvest_honey"),
            "Bee Our Guest",
            "Use a Campfire to collect Honey from a Beehive using a Glass Bottle without aggravating the Bees"
    ),
    THE_PARROTS_AND_THE_BATS(NamespacedKey.minecraft("husbandry/breed_an_animal"), "The Parrots and the Bats", "Breed two animals together"),
    YOU_HAVE_GOT_A_FRIEND_IN_ME(NamespacedKey.minecraft("husbandry/allay_deliver_item_to_player"), "You've Got a Friend in Me", "Have an Allay deliver items to you"),
    WHATEVER_FLOATS_YOUR_GOAT(NamespacedKey.minecraft("husbandry/ride_a_boat_with_a_goat"), "Whatever Floats Your Goat!", "Get in a Boat and float with a Goat"),
    BEST_FRIENDS_FOREVER(NamespacedKey.minecraft("husbandry/tame_an_animal"), "Best Friends Forever", "Tame an animal"),
    GLOW_AND_BEHOLD(NamespacedKey.minecraft("husbandry/make_a_sign_glow"), "Glow and Behold!", "Make the text of any kind of sign glow"),
    FISHY_BUSINESS(NamespacedKey.minecraft("husbandry/fishy_business"), "Fishy Business", "Catch a fish"),
    TOTAL_BEELOCATION(NamespacedKey.minecraft("husbandry/silk_touch_nest"), "Total Beelocation", "Move a Bee Nest, with 3 Bees inside, using Silk Touch"),
    BUKKIT_BUKKIT(NamespacedKey.minecraft("husbandry/tadpole_in_a_bucket"), "Bukkit Bukkit", "Catch a Tadpole in a Bucket"),
    SMELLS_INTERESTING(NamespacedKey.minecraft("husbandry/obtain_sniffer_egg"), "Smells Interesting", "Obtain a Sniffer Egg"),
    A_SEEDY_PLACE(NamespacedKey.minecraft("husbandry/plant_seed"), "A Seedy Place", "Plant a seed and watch it grow"),
    WAX_ON(NamespacedKey.minecraft("husbandry/wax_on"), "Wax On", "Apply Honeycomb to a Copper block!"),
    TWO_BY_TWO(NamespacedKey.minecraft("husbandry/bred_all_animals"), "Two by Two", "Breed all the animals!"),
    BIRTHDAY_SONG(NamespacedKey.minecraft("husbandry/allay_deliver_cake_to_note_block"), "Birthday Song", "Have an Allay drop a Cake at a Note Block"),
    A_COMPLETE_CATALOGUE(NamespacedKey.minecraft("husbandry/complete_catalogue"), "A Complete Catalogue", "Tame all Cat variants!"),
    TACTICAL_FISHING(NamespacedKey.minecraft("husbandry/tactical_fishing"), "Tactical Fishing", "Catch a Fish... without a Fishing Rod!"),
    WHEN_THE_SQUAD_HOPS_INTO_TOWN(NamespacedKey.minecraft("husbandry/leash_all_frog_variants"), "When the Squad Hops into Town", "Get each Frog variant on a Lead"),
    LITTLE_SNIFFS(NamespacedKey.minecraft("husbandry/feed_snifflet"), "Little Sniffs", "Feed a Snifflet"),
    A_BALANCED_DIET(NamespacedKey.minecraft("husbandry/balanced_diet"), "A Balanced Diet", "Eat everything that is edible, even if it's not good for you"),
    SERIOUS_DEDICATION(
            NamespacedKey.minecraft("husbandry/obtain_netherite_hoe"),
            "Serious Dedication",
            "Use a Netherite Ingot to upgrade a Hoe, and then reevaluate your life choices"
    ),
    WAX_OFF(NamespacedKey.minecraft("husbandry/wax_off"), "Wax Off", "Scrape Wax off of a Copper block!"),
    THE_CUTEST_PREDATOR(NamespacedKey.minecraft("husbandry/axolotl_in_a_bucket"), "The Cutest Predator", "Catch an Axolotl in a Bucket"),
    WITH_OUR_POWERS_COMBINED(NamespacedKey.minecraft("husbandry/froglights"), "With Our Powers Combined!", "Have all Froglights in your inventory"),
    PLANTING_THE_PAST(NamespacedKey.minecraft("husbandry/plant_any_sniffer_seed"), "Planting the Past", "Plant any Sniffer seed"),
    THE_HEALING_POWER_OF_FRIENDSHIP(
            NamespacedKey.minecraft("husbandry/kill_axolotl_target"),
            "The Healing Power of Friendship!",
            "Team up with an axolotl and win a fight"
    ),
    UH_OH(NamespacedKey.minecraft("husbandry/uh_oh"), "Uh Oh", "Have a Sulfur Cube absorb a TNT block");

    public final NamespacedKey key;
    public final String name;
    public final String description;

    KatzeAdvancement(NamespacedKey key, String name, String description) {
        this.key = key;
        this.name = name;
        this.description = description;
    }

}
