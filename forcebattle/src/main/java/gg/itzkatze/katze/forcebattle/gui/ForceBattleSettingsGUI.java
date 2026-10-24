package gg.itzkatze.katze.forcebattle.gui;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeRegistry;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.configuration.ChallengeSettingsManager;
import gg.itzkatze.katze.configuration.Setting;
import gg.itzkatze.katze.configuration.states.PlayerTeamBothState;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import gg.itzkatze.katze.forcebattle.ForceBattle;
import gg.itzkatze.katze.forcebattle.ForceBattleSettings;
import gg.itzkatze.katze.game.GameLifecycleManager;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.gui.sign.NumberSignInput;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ForceBattleSettingsGUI extends KatzeGUI {

    public ForceBattleSettingsGUI() {
        super("Settings", 27);
    }

    @Getter
    public enum SettingButton {
        FORCE_ITEM(0, Material.ITEM_FRAME, ForceBattleSettings.FORCE_ITEM, true, "Force Item",
                "Click to toggle ForceItem.", "ForceItem"),
        FORCE_MOB(1, Material.DIAMOND_SWORD, ForceBattleSettings.FORCE_MOB, true, "Force Mob",
                "Click to toggle ForceMob.", "ForceMob"),
        FORCE_BIOME(2, Material.GRASS_BLOCK, ForceBattleSettings.FORCE_BIOME, true, "Force Biome",
                "Click to toggle ForceBiome.", "ForceBiome"),
        FORCE_ADVANCEMENT(3, Material.BREWING_STAND, ForceBattleSettings.FORCE_ADVANCEMENT, true, "Force Advancement",
                "Click to toggle ForceAdvancement.", "ForceAdvancement"),
        FORCE_HEIGHT(4, Material.SCAFFOLDING, ForceBattleSettings.FORCE_HEIGHT, true, "Force Height",
                "Click to toggle ForceHeight.", "ForceHeight"),
        FORCE_COORDS(5, Material.FILLED_MAP, ForceBattleSettings.FORCE_COORDS, true, "Force Coords",
                "Click to toggle ForceCoords.", "ForceCoords"),
        FORCE_STRUCTURE(6, Material.STRUCTURE_BLOCK, ForceBattleSettings.FORCE_STRUCTURE, true, "Force Structure",
                "Click to toggle ForceStructure.", "ForceStructure"),
        FORCE_DEATH(7, Material.SKELETON_SKULL, ForceBattleSettings.FORCE_DEATH, true, "Force Death",
                "Click to toggle ForceDeath.", "ForceDeath"),

        BATTLE_DURATION(18, Material.CLOCK, null, false, "Battle Duration",
                "Click to set battle timer duration in minutes.", "Battle Duration"),
        CHAIN_MODE(19, Material.IRON_CHAIN, ForceBattleSettings.CHAIN_MODE, false, "Chain Mode",
                "Click to toggle Chain Mode.", "Chain Mode"),
        BACKPACK(20, Material.CHEST, ForceBattleSettings.BACKPACK, false, "Backpacks",
                "Click to toggle Backpacks.", "Backpacks"),

        RESET(26, Material.BARRIER, null, false, "RESET", null, null);

        private final int slot;
        private final Material material;
        private final Setting setting;
        private final boolean multiState;
        private final String nameText;
        private final String toggleLine;
        private final String changedMsg;

        SettingButton(int slot, Material material, Setting setting, boolean multiState,
                      String nameText, String toggleLine, String changedMsg) {
            this.slot = slot;
            this.material = material;
            this.setting = setting;
            this.multiState = multiState;
            this.nameText = nameText;
            this.toggleLine = toggleLine;
            this.changedMsg = changedMsg;
        }

        public ItemStack createItem() {
            if (this == BATTLE_DURATION) {
                int minutes = KatzeGames.get().getConfig().getInt(
                        ForceBattle.BATTLE_DURATION_MINUTES_KEY,
                        ForceBattle.DEFAULT_BATTLE_DURATION_MINUTES
                );
                List<Component> loreLines = new ArrayList<>();
                loreLines.add(Component.text(toggleLine).color(NamedTextColor.GRAY));
                loreLines.add(Component.empty());
                loreLines.add(Component.text("Current").color(NamedTextColor.BLUE)
                        .append(Component.text(": ").color(NamedTextColor.DARK_GRAY))
                        .append(Component.text(minutes + " min").color(NamedTextColor.AQUA)));
                return ItemStackCreator.getStack(
                        Component.text(nameText).color(NamedTextColor.BLUE),
                        material,
                        1,
                        loreLines
                );
            }

            if (setting == null) {
                return ItemStackCreator.createNamedItemStack(
                        material,
                        Component.text(nameText).color(NamedTextColor.RED)
                );
            }

            List<Component> loreLines = new ArrayList<>();
            if (toggleLine != null && !toggleLine.isBlank()) {
                loreLines.add(Component.text(toggleLine).color(NamedTextColor.GRAY));
            }
            loreLines.add(Component.empty());
            loreLines.add(Component.text("Current Status").color(NamedTextColor.BLUE)
                    .append(Component.text(": ").color(NamedTextColor.DARK_GRAY))
                    .append(getSettingState().getDisplayName()));

            return ItemStackCreator.getStack(
                    Component.text(nameText).color(NamedTextColor.BLUE),
                    material,
                    1,
                    loreLines
            );
        }

        private SettingStateType getSettingState() {
            if (setting instanceof ForceBattleSettings fbSetting) {
                Challenge forceBattle = ChallengeRegistry.getChallenge(ChallengeType.FORCE_BATTLE);
                if (forceBattle != null && forceBattle.getSettingsManager() != null) {
                    ChallengeSettingsManager manager = forceBattle.getSettingsManager();
                    return manager.getState(fbSetting);
                }
            }
            return PlayerTeamBothState.OFF;
        }

        public void handleClick(KatzePlayer player) {
            if (this == RESET) {
                GameLifecycleManager.resetGame();
                return;
            }

            if (this == BATTLE_DURATION) {
                int current = KatzeGames.get().getConfig().getInt(
                        ForceBattle.BATTLE_DURATION_MINUTES_KEY,
                        ForceBattle.DEFAULT_BATTLE_DURATION_MINUTES
                );
                NumberSignInput.open(
                        player,
                        "Battle Duration (min)",
                        current,
                        1,
                        1440,
                        (katzePlayer, value) -> {
                            KatzeGames.get().getConfig().set(ForceBattle.BATTLE_DURATION_MINUTES_KEY, value);
                            KatzeGames.get().saveConfig();
                            KatzeGames.broadcast(
                                    Component.text("Battle Duration ").color(NamedTextColor.BLUE)
                                            .append(Component.text("has been set to ").color(NamedTextColor.GRAY))
                                            .append(Component.text(value + " min").color(NamedTextColor.AQUA))
                                            .append(Component.text("!").color(NamedTextColor.GRAY))
                            );
                            new ForceBattleSettingsGUI().open(katzePlayer);
                        },
                        (katzePlayer, error) -> {
                            katzePlayer.sendMessage(Component.text(error).color(NamedTextColor.RED));
                            new ForceBattleSettingsGUI().open(katzePlayer);
                        }
                );
                return;
            }

            if (setting == null) return;

            SettingStateType next = getSettingState().next();

            if (setting instanceof ForceBattleSettings fbSetting) {
                Challenge forceBattle = ChallengeRegistry.getChallenge(ChallengeType.FORCE_BATTLE);
                if (forceBattle != null && forceBattle.getSettingsManager() != null) {
                    ChallengeSettingsManager manager = forceBattle.getSettingsManager();
                    manager.setState(fbSetting, next);
                }
            }

            String settingName = changedMsg.replace("_", " ");
            settingName = settingName.substring(0, 1).toUpperCase() + settingName.substring(1);
            KatzeGames.broadcast(
                    Component.text(settingName + " ").color(NamedTextColor.BLUE)
                            .append(Component.text("has been set to ").color(NamedTextColor.GRAY))
                            .append(getSettingState().getDisplayName())
                            .append(Component.text("!").color(NamedTextColor.GRAY)));

            new ForceBattleSettingsGUI().open(player);
        }
    }

    @Override
    public void setItems(KatzePlayer player) {
        fill(ItemStackCreator.fillerItem());
        set(GUIClickableItem.getGoForthItem(8, new ConfigSettingsGUI()));

        for (SettingButton button : SettingButton.values()) {
            set(new GUIClickableItem(button.getSlot()) {
                @Override
                public void run(final InventoryClickEvent event, final KatzePlayer player) {
                    button.handleClick(player);
                }

                @Override
                public ItemStack getItem(KatzePlayer p) {
                    return button.createItem();
                }
            });
        }
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }
}
