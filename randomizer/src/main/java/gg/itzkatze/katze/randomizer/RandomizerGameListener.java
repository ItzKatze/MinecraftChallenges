package gg.itzkatze.katze.randomizer;

import gg.itzkatze.katze.game.SessionPersistence;
import gg.itzkatze.katze.util.WorldStorage;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RandomizerGameListener implements Listener {

    private static final String FILE_NAME = "randomizer_mappings.json";

    private final Random random = new Random();
    private final List<Material> itemPool;
    private final Map<Material, Material> blockDropMap = new EnumMap<>(Material.class);
    private final Map<Material, Material> mobDropMap = new EnumMap<>(Material.class);
    private final Map<Material, Material> chestLootMap = new EnumMap<>(Material.class);
    private final Map<UUID, Map<Material, Material>> playerBlockDropMaps = new HashMap<>();
    private final Map<UUID, Map<Material, Material>> playerMobDropMaps = new HashMap<>();
    private final Map<UUID, Map<Material, Material>> playerChestLootMaps = new HashMap<>();

    public static class SaveData {
        public Map<String, String> blockDropMap = new HashMap<>();
        public Map<String, String> mobDropMap = new HashMap<>();
        public Map<String, String> chestLootMap = new HashMap<>();
        public Map<String, Map<String, String>> playerBlockDropMaps = new HashMap<>();
        public Map<String, Map<String, String>> playerMobDropMaps = new HashMap<>();
        public Map<String, Map<String, String>> playerChestLootMaps = new HashMap<>();
    }

    public RandomizerGameListener() {
        this.itemPool = buildItemPool();
    }

    public void saveToFile() {
        SaveData data = new SaveData();
        data.blockDropMap = serializeMap(blockDropMap);
        data.mobDropMap = serializeMap(mobDropMap);
        data.chestLootMap = serializeMap(chestLootMap);
        data.playerBlockDropMaps = serializeNestedMap(playerBlockDropMaps);
        data.playerMobDropMaps = serializeNestedMap(playerMobDropMaps);
        data.playerChestLootMaps = serializeNestedMap(playerChestLootMaps);

        WorldStorage.save(FILE_NAME, data);
    }

    public void loadFromFile() {
        if (!WorldStorage.exists(FILE_NAME)) return;

        SaveData data = WorldStorage.load(FILE_NAME, SaveData.class);
        if (data == null) return;

        blockDropMap.clear();
        mobDropMap.clear();
        chestLootMap.clear();
        playerBlockDropMaps.clear();
        playerMobDropMaps.clear();
        playerChestLootMaps.clear();

        deserializeMap(data.blockDropMap, blockDropMap);
        deserializeMap(data.mobDropMap, mobDropMap);
        deserializeMap(data.chestLootMap, chestLootMap);
        deserializeNestedMap(data.playerBlockDropMaps, playerBlockDropMaps);
        deserializeNestedMap(data.playerMobDropMaps, playerMobDropMaps);
        deserializeNestedMap(data.playerChestLootMaps, playerChestLootMaps);
    }

    public void resetMappings() {
        blockDropMap.clear();
        mobDropMap.clear();
        chestLootMap.clear();
        playerBlockDropMaps.clear();
        playerMobDropMaps.clear();
        playerChestLootMaps.clear();
        WorldStorage.delete(FILE_NAME);
    }

    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        if (!RandomizerSettings.isBlockDropsEnabled()) return;
        if (event.getItems().isEmpty()) return;

        Material blockType = event.getBlockState().getType();
        Material replacement = getOrCreate(getMapping(playerBlockDropMaps, blockDropMap, event.getPlayer()), blockType);
        for (Item item : event.getItems()) {
            ItemStack stack = item.getItemStack();
            ItemStack replaced = new ItemStack(replacement, stack.getAmount());
            item.setItemStack(replaced);
        }
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (!RandomizerSettings.isMobDropsEnabled()) return;
        List<ItemStack> drops = event.getDrops();
        if (drops.isEmpty()) return;

        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (drop == null || drop.getType().isAir()) continue;
            Material replacement = getOrCreate(getMapping(playerMobDropMaps, mobDropMap, event.getEntity().getKiller()), drop.getType());
            drops.set(i, new ItemStack(replacement, drop.getAmount()));
        }
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        if (!RandomizerSettings.isChestLootEnabled()) return;
        List<ItemStack> loot = event.getLoot();
        if (loot.isEmpty()) return;

        for (int i = 0; i < loot.size(); i++) {
            ItemStack stack = loot.get(i);
            if (stack == null || stack.getType().isAir()) continue;
            Player player = event.getEntity() instanceof Player p ? p : null;
            Material replacement = getOrCreate(getMapping(playerChestLootMaps, chestLootMap, player), stack.getType());
            loot.set(i, new ItemStack(replacement, stack.getAmount()));
        }
    }

    private Map<Material, Material> getMapping(Map<UUID, Map<Material, Material>> playerMappings,
                                               Map<Material, Material> sharedMapping,
                                               Player player) {
        if (RandomizerSettings.isSharedMappingsEnabled() || player == null) {
            return sharedMapping;
        }
        return playerMappings.computeIfAbsent(player.getUniqueId(), ignored -> new EnumMap<>(Material.class));
    }

    private Material getOrCreate(Map<Material, Material> map, Material source) {
        if (map.containsKey(source)) {
            return map.get(source);
        }
        if (itemPool.isEmpty()) return source;
        Material chosen = itemPool.get(random.nextInt(itemPool.size()));
        if (chosen == source && itemPool.size() > 1) {
            chosen = itemPool.get((itemPool.indexOf(chosen) + 1) % itemPool.size());
        }
        map.put(source, chosen);
        SessionPersistence.requestSave();
        return chosen;
    }

    private static List<Material> buildItemPool() {
        List<Material> materials = new ArrayList<>();
        for (Material material : Material.values()) {
            if (!material.isItem()) continue;
            if (material.isAir()) continue;
            materials.add(material);
        }
        Collections.shuffle(materials);
        return materials;
    }

    private static Map<String, String> serializeMap(Map<Material, Material> map) {
        Map<String, String> result = new HashMap<>();
        map.forEach((k, v) -> result.put(k.name(), v.name()));
        return result;
    }

    private static Map<String, Map<String, String>> serializeNestedMap(Map<UUID, Map<Material, Material>> map) {
        Map<String, Map<String, String>> result = new HashMap<>();
        map.forEach((k, v) -> result.put(k.toString(), serializeMap(v)));
        return result;
    }

    private static void deserializeMap(Map<String, String> source, Map<Material, Material> target) {
        if (source == null) return;
        source.forEach((k, v) -> {
            Material sourceMat = Material.matchMaterial(k);
            Material targetMat = Material.matchMaterial(v);
            if (sourceMat != null && targetMat != null) {
                target.put(sourceMat, targetMat);
            }
        });
    }

    private static void deserializeNestedMap(Map<String, Map<String, String>> source, Map<UUID, Map<Material, Material>> target) {
        if (source == null) return;
        source.forEach((k, v) -> {
            try {
                UUID uuid = UUID.fromString(k);
                Map<Material, Material> matMap = target.computeIfAbsent(uuid, ignored -> new EnumMap<>(Material.class));
                deserializeMap(v, matMap);
            } catch (Exception ignored) {
            }
        });
    }
}
