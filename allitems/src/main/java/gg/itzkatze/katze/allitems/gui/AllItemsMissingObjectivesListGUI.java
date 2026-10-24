package gg.itzkatze.katze.allitems.gui;

import gg.itzkatze.katze.allitems.AllItems;
import gg.itzkatze.katze.allitems.AllItemsObjectiveType;
import gg.itzkatze.katze.allitems.data.AllItemsData;
import gg.itzkatze.katze.data.BiomeSimplified;
import gg.itzkatze.katze.data.KatzeAdvancement;
import gg.itzkatze.katze.data.StructureSimplified;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import gg.itzkatze.katze.util.StringUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AllItemsMissingObjectivesListGUI extends KatzeGUI {

    private static final int PAGE_SIZE = 45;

    private final AllItemsObjectiveType type;
    private final int page;

    public AllItemsMissingObjectivesListGUI(AllItemsObjectiveType type, int page) {
        super(type.getDisplayName() + " Missing", 54);
        this.type = type;
        this.page = page;
    }

    @Override
    public void setItems(KatzePlayer player) {
        fill(ItemStackCreator.fillerItem());

        AllItemsData data = AllItems.getData();
        if (player.getPlayer() == null) {
            set(GUIClickableItem.getGoBackItem(45, new AllItemsMissingObjectivesMenu()));
            return;
        }

        UUID playerId = player.getPlayer().getUniqueId();
        List<ItemStack> items = buildMissingItems(data, playerId);
        int start = page * PAGE_SIZE;
        int end = Math.min(items.size(), start + PAGE_SIZE);

        for (int i = start; i < end; i++) {
            int slot = i - start;
            ItemStack stack = items.get(i);
            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryClickEvent event, KatzePlayer player) {
                }

                @Override
                public ItemStack getItem(KatzePlayer p) {
                    return stack;
                }
            });
        }

        set(GUIClickableItem.getGoBackItem(45, new AllItemsMissingObjectivesMenu()));
        if (page > 0) {
            set(GUIClickableItem.getGoBackItem(47, new AllItemsMissingObjectivesListGUI(type, page - 1)));
        }
        if (end < items.size()) {
            set(GUIClickableItem.getGoForthItem(51, new AllItemsMissingObjectivesListGUI(type, page + 1)));
        }
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }

    private List<ItemStack> buildMissingItems(AllItemsData data, UUID playerId) {
        return switch (type) {
            case ITEM -> buildItemStacks(data.getMissingItems(playerId), Material.CHEST);
            case BIOME -> buildBiomeStacks(data.getMissingBiomes(playerId));
            case MOB -> buildMobStacks(data.getMissingMobs(playerId));
            case STRUCTURE -> buildStructureStacks(data.getMissingStructures(playerId));
            case ADVANCEMENT -> buildAdvancementStacks(data.getMissingAdvancements(playerId));
        };
    }

    private List<ItemStack> buildItemStacks(List<Material> materials, Material fallback) {
        List<ItemStack> items = new ArrayList<>();
        for (Material material : materials) {
            items.add(ItemStackCreator.getStack(
                    Component.text(StringUtility.formatName(material.name())).color(NamedTextColor.AQUA),
                    material.isItem() ? material : fallback,
                    1,
                    List.of(Component.text("Missing item").color(NamedTextColor.GRAY))
            ));
        }
        return items;
    }

    private List<ItemStack> buildBiomeStacks(List<BiomeSimplified> biomes) {
        List<ItemStack> items = new ArrayList<>();
        for (BiomeSimplified biome : biomes) {
            items.add(ItemStackCreator.getStack(
                    Component.text(biome.getName()).color(NamedTextColor.AQUA),
                    Material.GRASS_BLOCK,
                    1,
                    List.of(Component.text("Missing biome").color(NamedTextColor.GRAY))
            ));
        }
        return items;
    }

    private List<ItemStack> buildMobStacks(List<EntityType> mobs) {
        List<ItemStack> items = new ArrayList<>();
        for (EntityType type : mobs) {
            Material icon = Material.matchMaterial(type.name() + "_SPAWN_EGG");
            if (icon == null) {
                icon = Material.ZOMBIE_SPAWN_EGG;
            }
            items.add(ItemStackCreator.getStack(
                    Component.text(StringUtility.formatName(type.name())).color(NamedTextColor.AQUA),
                    icon,
                    1,
                    List.of(Component.text("Missing mob").color(NamedTextColor.GRAY))
            ));
        }
        return items;
    }

    private List<ItemStack> buildStructureStacks(List<StructureSimplified> structures) {
        List<ItemStack> items = new ArrayList<>();
        for (StructureSimplified structure : structures) {
            items.add(ItemStackCreator.getStack(
                    Component.text(structure.getName()).color(NamedTextColor.AQUA),
                    Material.STRUCTURE_BLOCK,
                    1,
                    List.of(Component.text("Missing structure").color(NamedTextColor.GRAY))
            ));
        }
        return items;
    }

    private List<ItemStack> buildAdvancementStacks(List<KatzeAdvancement> advancements) {
        List<ItemStack> items = new ArrayList<>();
        for (KatzeAdvancement advancement : advancements) {
            items.add(ItemStackCreator.getStack(
                    Component.text(advancement.getName()).color(NamedTextColor.AQUA),
                    Material.BOOK,
                    1,
                    List.of(Component.text(advancement.getDescription()).color(NamedTextColor.GRAY))
            ));
        }
        return items;
    }
}
