package gg.itzkatze.katze.itemrain.gui;

import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeRegistry;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.configuration.ChallengeSettingsManager;
import gg.itzkatze.katze.configuration.states.OnOffState;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.gui.impl.SettingsGUI;
import gg.itzkatze.katze.gui.sign.NumberSignInput;
import gg.itzkatze.katze.itemrain.ItemRainSettings;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemRainSettingsGUI extends KatzeGUI {

    public ItemRainSettingsGUI() {
        super("Item Rain Settings", 36);
    }

    @Override
    public void setItems(KatzePlayer player) {
        fill(ItemStackCreator.fillerItem());

        set(new GUIClickableItem(13) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer player) {
                ItemRainSettings.ItemIntervalState state = getIntervalState();
                int current = state == null ? 10 : state.getSeconds();
                NumberSignInput.open(
                        player,
                        "Seconds (1-60)",
                        current,
                        1,
                        60,
                        ItemRainSettingsGUI.this::applySeconds,
                        (katzePlayer, message) -> {
                            katzePlayer.sendMessage(Component.text(message).color(NamedTextColor.RED));
                            new ItemRainSettingsGUI().open(katzePlayer);
                        }
                );
            }

            @Override
            public ItemStack getItem(KatzePlayer p) {
                ItemRainSettings.ItemIntervalState state = getIntervalState();
                Component status = state == null ? Component.text("Unknown") : state.getDisplayName();
                return ItemStackCreator.getStack(
                        Component.text("Item Interval").color(NamedTextColor.AQUA),
                        Material.CLOCK,
                        1,
                        List.of(
                                Component.text("Current: ").color(NamedTextColor.GRAY).append(status),
                                Component.empty(),
                                Component.text("Click to set seconds").color(NamedTextColor.YELLOW)
                        )
                );
            }
        });

        set(new GUIClickableItem(15) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer player) {
                SettingStateType current = getState(ItemRainSettings.ItemRainSetting.SKY_DROP_MODE);
                SettingStateType next = current == null ? OnOffState.ON : current.next();
                updateSetting(ItemRainSettings.ItemRainSetting.SKY_DROP_MODE, next);
                new ItemRainSettingsGUI().open(player);
            }

            @Override
            public ItemStack getItem(KatzePlayer p) {
                SettingStateType state = getState(ItemRainSettings.ItemRainSetting.SKY_DROP_MODE);
                Component status = state == null ? Component.text("Unknown") : state.getDisplayName();
                return ItemStackCreator.getStack(
                        Component.text("Sky Drop Mode").color(NamedTextColor.AQUA),
                        Material.ELYTRA,
                        1,
                        List.of(
                                Component.text("Current: ").color(NamedTextColor.GRAY).append(status),
                                Component.empty(),
                                Component.text("Click to toggle").color(NamedTextColor.YELLOW)
                        )
                );
            }
        });

        set(new GUIClickableItem(17) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer player) {
                ItemRainSettings.DropRadiusState state = getRadiusState();
                int current = state == null ? 5 : state.getRadius();
                NumberSignInput.open(
                        player,
                        "Radius (0-32)",
                        current,
                        0,
                        32,
                        ItemRainSettingsGUI.this::applyRadius,
                        (katzePlayer, message) -> {
                            katzePlayer.sendMessage(Component.text(message).color(NamedTextColor.RED));
                            new ItemRainSettingsGUI().open(katzePlayer);
                        }
                );
            }

            @Override
            public ItemStack getItem(KatzePlayer p) {
                ItemRainSettings.DropRadiusState state = getRadiusState();
                Component status = state == null ? Component.text("Unknown") : state.getDisplayName();
                return ItemStackCreator.getStack(
                        Component.text("Sky Drop Radius").color(NamedTextColor.AQUA),
                        Material.WIND_CHARGE,
                        1,
                        List.of(
                                Component.text("Current: ").color(NamedTextColor.GRAY).append(status),
                                Component.empty(),
                                Component.text("Click to set radius").color(NamedTextColor.YELLOW)
                        )
                );
            }
        });

        set(GUIClickableItem.getGoBackItem(27, new SettingsGUI()));
    }

    private ItemRainSettings.ItemIntervalState getIntervalState() {
        SettingStateType state = getState(ItemRainSettings.ItemRainSetting.ITEM_INTERVAL);
        if (state instanceof ItemRainSettings.ItemIntervalState intervalState) {
            return intervalState;
        }
        return null;
    }

    private ItemRainSettings.DropRadiusState getRadiusState() {
        SettingStateType state = getState(ItemRainSettings.ItemRainSetting.SKY_DROP_RADIUS);
        if (state instanceof ItemRainSettings.DropRadiusState radiusState) {
            return radiusState;
        }
        return null;
    }

    private SettingStateType getState(ItemRainSettings.ItemRainSetting setting) {
        Challenge challenge = ChallengeRegistry.getChallenge(ChallengeType.ITEM_RAIN);
        if (challenge == null || challenge.getSettingsManager() == null) return null;
        return challenge.getSettingsManager().getState(setting);
    }

    private void updateSetting(ItemRainSettings.ItemRainSetting setting, SettingStateType state) {
        Challenge challenge = ChallengeRegistry.getChallenge(ChallengeType.ITEM_RAIN);
        if (challenge == null || challenge.getSettingsManager() == null) return;
        ChallengeSettingsManager manager = challenge.getSettingsManager();
        manager.setState(setting, state);
    }

    private void applySeconds(KatzePlayer player, int seconds) {
        ItemRainSettings.ItemIntervalState state = ItemRainSettings.ItemIntervalState.fromSeconds(seconds);
        if (state == null) {
            player.sendMessage(Component.text("Unsupported value.").color(NamedTextColor.RED));
            new ItemRainSettingsGUI().open(player);
            return;
        }
        updateSetting(ItemRainSettings.ItemRainSetting.ITEM_INTERVAL, state);
        new ItemRainSettingsGUI().open(player);
    }

    private void applyRadius(KatzePlayer player, int radius) {
        ItemRainSettings.DropRadiusState state = ItemRainSettings.DropRadiusState.fromRadius(radius);
        if (state == null) {
            player.sendMessage(Component.text("Unsupported value.").color(NamedTextColor.RED));
            new ItemRainSettingsGUI().open(player);
            return;
        }
        updateSetting(ItemRainSettings.ItemRainSetting.SKY_DROP_RADIUS, state);
        new ItemRainSettingsGUI().open(player);
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }
}
