package gg.itzkatze.katze.itemrain;

import gg.itzkatze.katze.configuration.Setting;
import gg.itzkatze.katze.configuration.SettingsManager;
import gg.itzkatze.katze.configuration.states.ConfigurableSettingState;
import gg.itzkatze.katze.configuration.states.OnOffState;
import gg.itzkatze.katze.configuration.states.SettingStateType;
import gg.itzkatze.katze.util.KatzeColor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemRainSettings {

    public static int getIntervalSeconds() {
        SettingStateType state = SettingsManager.getSettingState(ItemRainSetting.ITEM_INTERVAL);
        if (state instanceof ItemIntervalState intervalState) {
            return intervalState.getSeconds();
        }
        return ItemIntervalState.SEC_10.getSeconds();
    }

    public static boolean isSkyDropEnabled() {
        return SettingsManager.isEnabled(ItemRainSetting.SKY_DROP_MODE);
    }

    public static int getSkyDropRadius() {
        SettingStateType state = SettingsManager.getSettingState(ItemRainSetting.SKY_DROP_RADIUS);
        if (state instanceof DropRadiusState radiusState) {
            return radiusState.getRadius();
        }
        return DropRadiusState.BLOCKS_5.getRadius();
    }

    @Getter
    public enum ItemRainSetting implements Setting {
        ITEM_INTERVAL(true, "Item Interval", ItemIntervalState.SEC_10),
        SKY_DROP_MODE(false, "Sky Drop Mode", OnOffState.OFF),
        SKY_DROP_RADIUS(true, "Sky Drop Radius", DropRadiusState.BLOCKS_5);

        private final boolean updateOnChange;
        private final String displayName;
        private final SettingStateType defaultState;

        ItemRainSetting(boolean updateOnChange, String displayName, SettingStateType defaultState) {
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

    public static final class ItemIntervalState implements ConfigurableSettingState {

        public static final int MIN_SECONDS = 1;
        public static final int MAX_SECONDS = 300;
        public static final ItemIntervalState SEC_10 = new ItemIntervalState(10);

        @Getter
        private final int seconds;

        private ItemIntervalState(int seconds) {
            this.seconds = seconds;
        }

        public static @Nullable ItemIntervalState fromSeconds(int seconds) {
            if (seconds < MIN_SECONDS || seconds > MAX_SECONDS) return null;
            if (seconds == SEC_10.seconds) return SEC_10;
            return new ItemIntervalState(seconds);
        }

        @Override
        public @NotNull Component getDisplayName() {
            String label = seconds == 1 ? "1 Second" : seconds + " Seconds";
            return Component.text(label).color(KatzeColor.LIGHT_BLUE);
        }

        @Override
        public @NotNull SettingStateType next() {
            int next = seconds + 1;
            if (next > MAX_SECONDS) {
                next = MIN_SECONDS;
            }
            if (next == SEC_10.seconds) {
                return SEC_10;
            }
            return new ItemIntervalState(next);
        }

        @Override
        public @NotNull String serialize() {
            return Integer.toString(seconds);
        }

        @Override
        public @Nullable SettingStateType parse(@NotNull String raw) {
            try {
                int value = Integer.parseInt(raw.trim());
                return fromSeconds(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static final class DropRadiusState implements ConfigurableSettingState {

        public static final int MIN_RADIUS = 0;
        public static final int MAX_RADIUS = 32;
        public static final DropRadiusState BLOCKS_5 = new DropRadiusState(5);

        @Getter
        private final int radius;

        private DropRadiusState(int radius) {
            this.radius = radius;
        }

        public static @Nullable DropRadiusState fromRadius(int radius) {
            if (radius < MIN_RADIUS || radius > MAX_RADIUS) return null;
            if (radius == BLOCKS_5.radius) return BLOCKS_5;
            return new DropRadiusState(radius);
        }

        @Override
        public @NotNull Component getDisplayName() {
            String label = radius == 1 ? "1 Block" : radius + " Blocks";
            return Component.text(label).color(KatzeColor.LIGHT_BLUE);
        }

        @Override
        public @NotNull SettingStateType next() {
            int next = radius + 1;
            if (next > MAX_RADIUS) {
                next = MIN_RADIUS;
            }
            if (next == BLOCKS_5.radius) {
                return BLOCKS_5;
            }
            return new DropRadiusState(next);
        }

        @Override
        public @NotNull String serialize() {
            return Integer.toString(radius);
        }

        @Override
        public @Nullable SettingStateType parse(@NotNull String raw) {
            try {
                int value = Integer.parseInt(raw.trim());
                return fromRadius(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
