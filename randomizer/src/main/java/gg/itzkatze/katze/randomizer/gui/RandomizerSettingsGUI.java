package gg.itzkatze.katze.randomizer.gui;

import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeRegistry;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.configuration.ChallengeSettingsManager;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.gui.impl.SettingsGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.randomizer.RandomizerSettings;
import gg.itzkatze.katze.util.ItemStackCreator;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RandomizerSettingsGUI extends KatzeGUI {

    public RandomizerSettingsGUI() {
        super("Randomizer Settings", 27);
    }

    private enum SettingButton {
        BLOCK_DROPS(10, Material.GRASS_BLOCK, "Block Drops", RandomizerSettings.RandomizerSetting.BLOCK_DROPS,
                List.of(Component.text("Randomize mined block drops").color(KatzeColor.LIGHT_GRAY))),
        MOB_DROPS(12, Material.ROTTEN_FLESH, "Mob Drops", RandomizerSettings.RandomizerSetting.MOB_DROPS,
                List.of(Component.text("Randomize mob drops").color(KatzeColor.LIGHT_GRAY))),
        CHEST_LOOT(14, Material.CHEST, "Chest Loot", RandomizerSettings.RandomizerSetting.CHEST_LOOT,
                List.of(Component.text("Randomize generated chest loot").color(KatzeColor.LIGHT_GRAY))),
        SHARED_MAPPINGS(16, Material.PLAYER_HEAD, "Shared Mappings", RandomizerSettings.RandomizerSetting.SHARED_MAPPINGS,
                List.of(
                        Component.text("On: every player shares the same mapping").color(KatzeColor.LIGHT_GRAY),
                        Component.text("Off: each player gets their own mapping").color(KatzeColor.LIGHT_GRAY)
                ));

        private final int slot;
        private final Material material;
        private final String label;
        private final RandomizerSettings.RandomizerSetting setting;
        private final List<Component> description;

        SettingButton(int slot, Material material, String label, RandomizerSettings.RandomizerSetting setting,
                      List<Component> description) {
            this.slot = slot;
            this.material = material;
            this.label = label;
            this.setting = setting;
            this.description = description;
        }
    }

    @Override
    public void setItems(KatzePlayer player) {
        fill(ItemStackCreator.fillerItem());

        for (SettingButton button : SettingButton.values()) {
            set(new GUIClickableItem(button.slot) {
                @Override
                public void run(InventoryClickEvent event, KatzePlayer player) {
                    SettingStateType state = getState(button.setting);
                    if (state != null) {
                        updateSetting(button.setting, state.next());
                    }
                    new RandomizerSettingsGUI().open(player);
                }

                @Override
                public ItemStack getItem(KatzePlayer p) {
                    SettingStateType state = getState(button.setting);
                    Component status = state == null ? Component.text("Unknown") : state.getDisplayName();
                    List<Component> lore = new java.util.ArrayList<>(button.description);
                    lore.add(Component.empty());
                    lore.add(Component.text("Current: ").color(KatzeColor.LIGHT_GRAY).append(status));
                    lore.add(Component.empty());
                    lore.add(Component.text("Click to toggle").color(KatzeColor.YELLOW));
                    return ItemStackCreator.getStack(
                            Component.text(button.label).color(KatzeColor.LIGHT_BLUE),
                            button.material,
                            1,
                            lore
                    );
                }
            });
        }

        set(GUIClickableItem.getGoBackItem(18, new SettingsGUI()));
    }

    private SettingStateType getState(RandomizerSettings.RandomizerSetting setting) {
        Challenge challenge = ChallengeRegistry.getChallenge(ChallengeType.RANDOMIZER);
        if (challenge == null || challenge.getSettingsManager() == null) return null;
        return challenge.getSettingsManager().getState(setting);
    }

    private void updateSetting(RandomizerSettings.RandomizerSetting setting, SettingStateType state) {
        Challenge challenge = ChallengeRegistry.getChallenge(ChallengeType.RANDOMIZER);
        if (challenge == null || challenge.getSettingsManager() == null) return;
        ChallengeSettingsManager manager = challenge.getSettingsManager();
        manager.setState(setting, state);
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }
}
