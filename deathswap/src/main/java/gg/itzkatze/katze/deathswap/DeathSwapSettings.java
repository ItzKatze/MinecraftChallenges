package gg.itzkatze.katze.deathswap;

import gg.itzkatze.katze.configuration.Setting;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.configuration.states.ConfigurableSettingState;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import gg.itzkatze.katze.util.KatzeColor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeathSwapSettings {

    public static int getSwapIntervalMinutes() {
        SettingStateType state = SettingsManager.getSettingState(DeathSwapSetting.SWAP_INTERVAL);
        if (state instanceof SwapIntervalState intervalState) {
            return intervalState.getMinutes();
        }
        return SwapIntervalState.MIN_5.getMinutes();
    }

    @Getter
    public enum DeathSwapSetting implements Setting {
        SWAP_INTERVAL(true, "Swap Interval", SwapIntervalState.MIN_5);

        private final boolean updateOnChange;
        private final String displayName;
        private final SettingStateType defaultState;

        DeathSwapSetting(boolean updateOnChange, String displayName, SettingStateType defaultState) {
            this.updateOnChange = updateOnChange;
            this.displayName = displayName;
            this.defaultState = defaultState;
        }

        @Override
        public String getId() {
            return name();
        }

        @Override
        public boolean shouldUpdateOnChange() {
            return updateOnChange;
        }

        @Override
        public SettingStateType getDefaultState() {
            return defaultState;
        }
    }

    public static final class SwapIntervalState implements ConfigurableSettingState {

        public static final int MIN_MINUTES = 1;
        public static final int MAX_MINUTES = 60;
        public static final SwapIntervalState MIN_5 = new SwapIntervalState(5);

        @Getter
        private final int minutes;

        private SwapIntervalState(int minutes) {
            this.minutes = minutes;
        }

        public static @Nullable SwapIntervalState fromMinutes(int minutes) {
            if (minutes < MIN_MINUTES || minutes > MAX_MINUTES) return null;
            if (minutes == MIN_5.minutes) return MIN_5;
            return new SwapIntervalState(minutes);
        }

        @Override
        public @NotNull Component getDisplayName() {
            String label = minutes == 1 ? "1 Minute" : minutes + " Minutes";
            return Component.text(label).color(KatzeColor.LIGHT_BLUE);
        }

        @Override
        public @NotNull SettingStateType next() {
            int next = minutes + 1;
            if (next > MAX_MINUTES) {
                next = MIN_MINUTES;
            }
            if (next == MIN_5.minutes) {
                return MIN_5;
            }
            return new SwapIntervalState(next);
        }

        @Override
        public @NotNull String serialize() {
            return Integer.toString(minutes);
        }

        @Override
        public @Nullable SettingStateType parse(@NotNull String raw) {
            try {
                int value = Integer.parseInt(raw.trim());
                return fromMinutes(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
