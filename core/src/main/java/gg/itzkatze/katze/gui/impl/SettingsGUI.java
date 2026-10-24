package gg.itzkatze.katze.gui.impl;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.challenges.ChallengeRegistry;
import gg.itzkatze.katze.challenges.ChallengeSettings;
import gg.itzkatze.katze.configuration.GlobalSetting;
import gg.itzkatze.katze.configuration.Setting;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.GUIMaterial;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import gg.itzkatze.katze.util.KatzeColor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SettingsGUI extends KatzeGUI {

    public SettingsGUI() {
        super("Settings", 54);
    }

    @Getter
    public enum GenericSettingButton {
        NO_HUNGER(45, Material.COOKED_BEEF, GlobalSetting.NO_HUNGER, "No Hunger", "Click to toggle no Hunger.", "No Hunger"),
        NO_DAMAGE(46, Material.IRON_SWORD, GlobalSetting.NO_DAMAGE, "No Damage", "Click to toggle no Damage.", "No Damage"),
        MOBILE_CRAFTING(47, Material.CRAFTING_TABLE, GlobalSetting.MOBILE_CRAFTING, "Mobile Crafting", "Click to toggle mobile crafting.", "Mobile Crafting"),
        HIDE_POINTS(48, Material.IRON_TRAPDOOR, GlobalSetting.HIDE_POINTS, "Hide Points", "Click to toggle Hide Points.", "Hide Points"),
        HIDE_OBJECTIVES(49, Material.DARK_OAK_DOOR, GlobalSetting.HIDE_OBJECTIVES, "Hide Objectives", "Click to toggle Hide Objectives.", "Hide Objectives"),
        NO_DUPLICATE_OBJECTIVES(50, Material.NAME_TAG, GlobalSetting.NO_DUPLICATE_OBJECTIVES, "No Duplicate Objectives", "Click to toggle no duplicate objectives.", "No duplicate objectives"),
        SIMPLIFIED_OBJECTIVES(51, Material.DIRT, GlobalSetting.SIMPLIFIED_OBJECTIVES, "Simplified Objectives", "Click to toggle simplified objectives.", "Simplified objectives"),
        EXTRA_TEAM_OBJECTIVE(52, Material.ALLAY_SPAWN_EGG, GlobalSetting.EXTRA_TEAM_OBJECTIVE, "Extra Team Objective", "Click to toggle the extra team objective.", "Extra team objective"),
        SHOW_SCOREBOARD(53, Material.ARMOR_STAND, GlobalSetting.SHOW_SCOREBOARD, "Show Scoreboard", "Click to toggle the scoreboard.", "Show scoreboard");

        private final int slot;
        private final Material material;
        private final GlobalSetting setting;
        private final String nameText;
        private final String toggleLine;
        private final String changedMsg;

        GenericSettingButton(int slot, Material material, GlobalSetting setting, String nameText, String toggleLine, String changedMsg) {
            this.slot = slot;
            this.material = material;
            this.setting = setting;
            this.nameText = nameText;
            this.toggleLine = toggleLine;
            this.changedMsg = changedMsg;
        }
    }

    @Override
    public void setItems(KatzePlayer player) {
        fill(ItemStackCreator.fillerItem());

        List<Challenge> challenges = new ArrayList<>(ChallengeRegistry.getAllChallenges());
        int slot = 0;

        for (Challenge challenge : challenges) {
            if (slot >= 45) break;

            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryClickEvent event, KatzePlayer player) {
                    if (event.getClick() == ClickType.RIGHT && challenge instanceof ChallengeSettings settings && settings.hasSettings()) {
                        KatzeGUI settingsGUI = settings.getSettingsGUI();
                        if (settingsGUI != null) {
                            settingsGUI.open(player);
                        }
                    } else {
                        if (ChallengeManager.isChallengeActive(challenge.getType())) {
                            ChallengeManager.unloadChallenge(challenge);
                            player.sendMessage(Component.text("Disabled challenge: ").color(KatzeColor.RED)
                                    .append(Component.text(challenge.getType().getName()).color(KatzeColor.RED)));
                        } else {
                            ChallengeManager.loadChallenge(challenge);
                            player.sendMessage(Component.text("Enabled challenge: ").color(KatzeColor.LIME)
                                    .append(Component.text(challenge.getType().getName()).color(KatzeColor.LIME)));
                        }
                        new SettingsGUI().open(player);
                    }
                }

                @Override
                public ItemStack getItem(KatzePlayer p) {
                    boolean active = ChallengeManager.isChallengeActive(challenge.getType());
                    GUIMaterial material = challenge.getType().getMaterial();
                    Component displayName = Component.text(challenge.getType().getName());

                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Status: ").color(KatzeColor.LIGHT_GRAY)
                            .append(Component.text(active ? "Enabled" : "Disabled")
                                    .color(active ? KatzeColor.LIME : KatzeColor.RED)));

                    if (challenge.hasSettings()) {
                        lore.add(Component.empty());
                        lore.add(Component.text("Left-click: ").color(KatzeColor.YELLOW)
                                .append(Component.text("Enable/Disable").color(KatzeColor.WHITE)));
                        lore.add(Component.text("Right-click: ").color(KatzeColor.YELLOW)
                                .append(Component.text("Open Settings").color(KatzeColor.WHITE)));
                    } else {
                        lore.add(Component.empty());
                        lore.add(Component.text("Click: ").color(KatzeColor.YELLOW)
                                .append(Component.text("Enable/Disable").color(KatzeColor.WHITE)));
                    }

                    lore.add(Component.empty());
                    lore.add(Component.text("Description: ").color(KatzeColor.LIGHT_GRAY)
                            .append(Component.text(challenge.getType().getDescription()).color(KatzeColor.WHITE)));

                    if (material.hasSkull()) {
                        return ItemStackCreator.getSkull(material.skull(), displayName, 1, lore);
                    } else {
                        return ItemStackCreator.getStack(displayName, material.material(), 1, lore);
                    }
                }
            });

            slot++;
        }

        for (GenericSettingButton button : GenericSettingButton.values()) {
            Setting setting = button.getSetting();
            String changedMsg = button.getChangedMsg();
            String toggleLine = button.getToggleLine();

            set(new GUIClickableItem(button.getSlot()) {
                @Override
                public void run(InventoryClickEvent event, KatzePlayer player) {
                    SettingStateType nextState = SettingsManager.getState(setting).next();
                    SettingsManager.setState(setting, nextState);

                    KatzeGames.broadcast(
                            Component.text(changedMsg + " ").color(KatzeColor.BLUE)
                                    .append(Component.text("has been set to ").color(KatzeColor.LIGHT_GRAY))
                                    .append(nextState.getDisplayName())
                                    .append(Component.text("!").color(KatzeColor.LIGHT_GRAY)));

                    new SettingsGUI().open(player);
                }

                @Override
                public ItemStack getItem(KatzePlayer p) {
                    List<Component> loreLines = new ArrayList<>();
                    if (toggleLine != null && !toggleLine.isBlank()) {
                        loreLines.add(Component.text(toggleLine).color(KatzeColor.LIGHT_GRAY));
                    }
                    loreLines.add(Component.empty());
                    loreLines.add(Component.text("Current Status").color(KatzeColor.BLUE)
                            .append(Component.text(": ").color(KatzeColor.GRAY))
                            .append(SettingsManager.getState(setting).getDisplayName()));

                    return ItemStackCreator.getStack(
                            Component.text(button.getNameText()).color(KatzeColor.BLUE),
                            button.getMaterial(),
                            1,
                            loreLines
                    );
                }
            });
        }
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }
}
