package gg.itzkatze.katze.allitems.gui;

import gg.itzkatze.katze.allitems.AllItems;
import gg.itzkatze.katze.allitems.AllItemsSettings;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import gg.itzkatze.katze.gui.impl.SettingsGUI;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AllItemsSettingsGUI extends KatzeGUI {

    public AllItemsSettingsGUI() {
        super("All Items Settings", 45);
    }

    private enum SettingButton {
        MODE(10, Material.PLAYER_HEAD, "Game Mode", AllItemsSettingsGUI::toggleGameMode, null),
        ITEMS(12, Material.CHEST, "Items", null, AllItemsSettings.AllItemsSetting.ITEMS),
        BIOMES(13, Material.GRASS_BLOCK, "Biomes", null, AllItemsSettings.AllItemsSetting.BIOMES),
        MOBS(14, Material.ZOMBIE_SPAWN_EGG, "Mobs", null, AllItemsSettings.AllItemsSetting.MOBS),
        STRUCTURES(15, Material.STRUCTURE_BLOCK, "Structures", null, AllItemsSettings.AllItemsSetting.STRUCTURES),
        ADVANCEMENTS(16, Material.BOOK, "Advancements", null, AllItemsSettings.AllItemsSetting.ADVANCEMENTS),
        CHAIN_MODE(28, Material.IRON_CHAIN, "Chain Mode", null, AllItemsSettings.AllItemsSetting.CHAIN_MODE),
        MISSING(30, Material.MAP, "Missing Objectives", AllItemsSettingsGUI::openMissing, null),
        LEADERBOARD(32, Material.NAME_TAG, "Leaderboard", AllItemsSettingsGUI::openLeaderboard, null);

        private final int slot;
        private final Material material;
        private final String label;
        private final ClickHandler handler;
        private final AllItemsSettings.AllItemsSetting setting;

        SettingButton(int slot, Material material, String label, ClickHandler handler,
                      AllItemsSettings.AllItemsSetting setting) {
            this.slot = slot;
            this.material = material;
            this.label = label;
            this.handler = handler;
            this.setting = setting;
        }
    }

    @FunctionalInterface
    private interface ClickHandler {
        void onClick(AllItemsSettingsGUI gui, KatzePlayer player);
    }

    @Override
    public void setItems(KatzePlayer player) {
        fill(ItemStackCreator.fillerItem());

        for (SettingButton button : SettingButton.values()) {
            set(new GUIClickableItem(button.slot) {
                @Override
                public void run(InventoryClickEvent event, KatzePlayer player) {
                    if (button.handler != null) {
                        button.handler.onClick(AllItemsSettingsGUI.this, player);
                        return;
                    }
                    if (button.setting != null) {
                        SettingStateType state = SettingsManager.getSettingState(button.setting);
                        if (state != null) {
                            SettingsManager.setState(button.setting, state.next());
                        }
                        if (button.setting == AllItemsSettings.AllItemsSetting.CHAIN_MODE) {
                            var data = AllItems.getData();
                            data.resetChains();
                        }
                        new AllItemsSettingsGUI().open(player);
                    }
                }

                @Override
                public ItemStack getItem(KatzePlayer p) {
                    return buildButtonItem(button);
                }
            });
        }

        // Go back button
        set(GUIClickableItem.getGoBackItem(36, new SettingsGUI()));
    }

    private static void toggleGameMode(AllItemsSettingsGUI gui, KatzePlayer player) {
        AllItemsSettings.GameMode currentMode = AllItemsSettings.getGameMode();
        AllItemsSettings.GameMode newMode = currentMode == AllItemsSettings.GameMode.TEAM
            ? AllItemsSettings.GameMode.INDIVIDUAL
            : AllItemsSettings.GameMode.TEAM;

        AllItemsSettings.setGameMode(newMode);

        String modeName = newMode == AllItemsSettings.GameMode.TEAM ? "Team Mode" : "Individual Mode";
        player.sendMessage(Component.text("All Items game mode set to: " + modeName, KatzeColor.LIME));

        // Refresh the GUI
        gui.open(player);
    }

    private static void openMissing(AllItemsSettingsGUI gui, KatzePlayer player) {
        new AllItemsMissingObjectivesMenu().open(player);
    }

    private static void openLeaderboard(AllItemsSettingsGUI gui, KatzePlayer player) {
        new AllItemsLeaderboardGUI().open(player);
    }

    private ItemStack buildButtonItem(SettingButton button) {
        if (button == SettingButton.MODE) {
            return buildModeItem(button);
        }
        if (button == SettingButton.MISSING) {
            return buildMissingItem(button);
        }
        if (button == SettingButton.LEADERBOARD) {
            return buildLeaderboardItem(button);
        }
        if (button.setting != null) {
            return buildToggleItem(button);
        }
        return buildFallbackItem(button);
    }

    private ItemStack buildModeItem(SettingButton button) {
        boolean isTeamMode = AllItemsSettings.isTeamMode();
        String modeName = isTeamMode ? "Team Mode" : "Individual Mode";
        TextColor color = isTeamMode ? KatzeColor.LIME : KatzeColor.LIGHT_BLUE;

        return ItemStackCreator.getStack(
                Component.text(modeName).color(color),
                button.material,
                1,
                List.of(
                        Component.text("Current: ").color(KatzeColor.LIGHT_GRAY)
                                .append(Component.text(modeName).color(color)),
                        Component.empty(),
                        Component.text("Click to toggle").color(KatzeColor.YELLOW),
                        Component.empty(),
                        Component.text("Team Mode: ").color(KatzeColor.LIGHT_GRAY)
                                .append(Component.text("All players collect together").color(KatzeColor.WHITE)),
                        Component.text("Individual Mode: ").color(KatzeColor.LIGHT_GRAY)
                                .append(Component.text("Each player collects separately").color(KatzeColor.WHITE))
                )
        );
    }

    private ItemStack buildMissingItem(SettingButton button) {
        return ItemStackCreator.getStack(
                Component.text(button.label).color(KatzeColor.LIGHT_BLUE),
                button.material,
                1,
                List.of(
                        Component.text("View remaining objectives.").color(KatzeColor.LIGHT_GRAY),
                        Component.empty(),
                        Component.text("Click to open").color(KatzeColor.YELLOW)
                )
        );
    }

    private ItemStack buildLeaderboardItem(SettingButton button) {
        return ItemStackCreator.getStack(
                Component.text(button.label).color(KatzeColor.GOLD),
                button.material,
                1,
                List.of(
                        Component.text("View player progress.").color(KatzeColor.LIGHT_GRAY),
                        Component.empty(),
                        Component.text("Click to open").color(KatzeColor.YELLOW)
                )
        );
    }

    private ItemStack buildToggleItem(SettingButton button) {
        SettingStateType state = SettingsManager.getSettingState(button.setting);
        Component status = state == null ? Component.text("Unknown") : state.getDisplayName();
        return ItemStackCreator.getStack(
                Component.text(button.label).color(KatzeColor.BLUE),
                button.material,
                1,
                List.of(
                        Component.text("Current: ").color(KatzeColor.LIGHT_GRAY).append(status),
                        Component.empty(),
                        Component.text("Click to toggle").color(KatzeColor.YELLOW)
                )
        );
    }

    private ItemStack buildFallbackItem(SettingButton button) {
        return ItemStackCreator.createNamedItemStack(
                button.material,
                Component.text(button.label).color(KatzeColor.RED)
        );
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }
}
