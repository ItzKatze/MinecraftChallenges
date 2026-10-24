package gg.itzkatze.katze.deathswap.gui;

import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeRegistry;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.configuration.ChallengeSettingsManager;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import gg.itzkatze.katze.deathswap.DeathSwapSettings;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.gui.sign.NumberSignInput;
import gg.itzkatze.katze.gui.impl.SettingsGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DeathSwapSettingsGUI extends KatzeGUI {

    public DeathSwapSettingsGUI() {
        super("Death Swap Settings", 27);
    }

    @Override
    public void setItems(KatzePlayer player) {
        fill(ItemStackCreator.fillerItem());

        set(new GUIClickableItem(13) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer player) {
                DeathSwapSettings.SwapIntervalState state = getIntervalState();
                int current = state == null ? 5 : state.getMinutes();
                NumberSignInput.open(
                        player,
                        "Minutes (1-60)",
                        current,
                        1,
                        60,
                        DeathSwapSettingsGUI.this::applyMinutes,
                        (katzePlayer, message) -> {
                            katzePlayer.sendMessage(Component.text(message).color(KatzeColor.RED));
                            new DeathSwapSettingsGUI().open(katzePlayer);
                        }
                );
            }

            @Override
            public ItemStack getItem(KatzePlayer p) {
                DeathSwapSettings.SwapIntervalState state = getIntervalState();
                Component status = state == null ? Component.text("Unknown") : state.getDisplayName();
                return ItemStackCreator.getStack(
                        Component.text("Swap Interval").color(KatzeColor.LIGHT_BLUE),
                        Material.CLOCK,
                        1,
                        List.of(
                                Component.text("Current: ").color(KatzeColor.LIGHT_GRAY).append(status),
                                Component.empty(),
                                Component.text("Click to set minutes").color(KatzeColor.YELLOW)
                        )
                );
            }
        });

        set(GUIClickableItem.getGoBackItem(18, new SettingsGUI()));
    }

    private DeathSwapSettings.SwapIntervalState getIntervalState() {
        Challenge challenge = ChallengeRegistry.getChallenge(ChallengeType.DEATH_SWAP);
        if (challenge == null || challenge.getSettingsManager() == null) return null;
        ChallengeSettingsManager manager = challenge.getSettingsManager();
        SettingStateType state = manager.getState(DeathSwapSettings.DeathSwapSetting.SWAP_INTERVAL);
        if (state instanceof DeathSwapSettings.SwapIntervalState intervalState) {
            return intervalState;
        }
        return null;
    }

    private void updateSetting(SettingStateType state) {
        Challenge challenge = ChallengeRegistry.getChallenge(ChallengeType.DEATH_SWAP);
        if (challenge == null || challenge.getSettingsManager() == null) return;
        ChallengeSettingsManager manager = challenge.getSettingsManager();
        manager.setState(DeathSwapSettings.DeathSwapSetting.SWAP_INTERVAL, state);
    }

    private void applyMinutes(KatzePlayer player, int minutes) {
        DeathSwapSettings.SwapIntervalState state = DeathSwapSettings.SwapIntervalState.fromMinutes(minutes);
        if (state == null) {
            player.sendMessage(Component.text("Unsupported value.").color(KatzeColor.RED));
            new DeathSwapSettingsGUI().open(player);
            return;
        }
        updateSetting(state);
        new DeathSwapSettingsGUI().open(player);
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }
}
