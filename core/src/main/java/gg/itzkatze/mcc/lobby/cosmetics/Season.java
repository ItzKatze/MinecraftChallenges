package gg.itzkatze.mcc.lobby.cosmetics;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public enum Season {

    HALLOWEEN("Halloween", NamedTextColor.GOLD, MonthDay.of(Month.OCTOBER, 15), MonthDay.of(Month.NOVEMBER, 2)),
    CHRISTMAS("Christmas", NamedTextColor.RED, MonthDay.of(Month.DECEMBER, 1), MonthDay.of(Month.DECEMBER, 26)),
    SUMMER("Summer", NamedTextColor.YELLOW, MonthDay.of(Month.JUNE, 21), MonthDay.of(Month.SEPTEMBER, 22));

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

    private final String displayName;
    private final NamedTextColor color;
    private final MonthDay first;
    private final MonthDay last;

    Season(String displayName, NamedTextColor color, MonthDay first, MonthDay last) {
        this.displayName = displayName;
        this.color = color;
        this.first = first;
        this.last = last;
    }

    public @NotNull String displayName() {
        return displayName;
    }

    public @NotNull NamedTextColor color() {
        return color;
    }

    public boolean isActive(@NotNull LocalDate date) {
        return isWithin(first, last, date);
    }

    public @NotNull String firstDay() {
        return DAY.format(first);
    }

    public @NotNull String lastDay() {
        return DAY.format(last);
    }

    public @NotNull Component availability(@NotNull LocalDate today) {
        return Component.text(displayName + " · " + (isActive(today) ? "until " + lastDay() : "returns " + firstDay()), color);
    }

    public @NotNull Component returnsMessage(@NotNull Cosmetic cosmetic) {
        return Component.text(cosmetic.displayName() + " returns to the Cosmetic Case for " + displayName + " on " + firstDay() + ".", NamedTextColor.RED);
    }

    // A window whose first day comes after its last day runs over New Year.
    static boolean isWithin(@NotNull MonthDay first, @NotNull MonthDay last, @NotNull LocalDate date) {
        MonthDay day = MonthDay.from(date);
        boolean started = !day.isBefore(first);
        boolean notOver = !day.isAfter(last);
        return first.isAfter(last) ? started || notOver : started && notOver;
    }
}
