package gg.itzkatze.katze.lobby.cosmetics;

import gg.itzkatze.katze.util.KatzeColor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Getter
public enum Season {

    WINTER("Winter", KatzeColor.WHITE, MonthDay.of(Month.DECEMBER, 15), MonthDay.of(Month.FEBRUARY, 15)),
    CHRISTMAS("Chrismas", KatzeColor.RED, MonthDay.of(Month.DECEMBER, 24), MonthDay.of(Month.DECEMBER, 31)),
    SPRING("Spring", KatzeColor.LIME, MonthDay.of(Month.MARCH, 15), MonthDay.of(Month.MAY, 15)),
    EASTER("Easter", KatzeColor.GOLD, MonthDay.of(Month.MARCH, 21), MonthDay.of(Month.MARCH, 31)),
    SUMMER("Summer", KatzeColor.YELLOW, MonthDay.of(Month.JUNE, 15), MonthDay.of(Month.AUGUST, 15)),
    AUTUMN("Autumn", KatzeColor.BROWN, MonthDay.of(Month.SEPTEMBER, 15), MonthDay.of(Month.NOVEMBER, 15)),
    HALLOWEEN("Halloween", KatzeColor.ORANGE, MonthDay.of(Month.OCTOBER, 20), MonthDay.of(Month.NOVEMBER, 2)),
    ;

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

    private final String displayName;
    private final TextColor color;
    private final MonthDay first;
    private final MonthDay last;

    Season(String displayName, TextColor color, MonthDay first, MonthDay last) {
        this.displayName = displayName;
        this.color = color;
        this.first = first;
        this.last = last;
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
        return Component.text(cosmetic.displayName() + " returns to the Cosmetic Case for " + displayName + " on " + firstDay() + ".", KatzeColor.RED);
    }

    // A window whose first day comes after its last day runs over New Year.
    static boolean isWithin(@NotNull MonthDay first, @NotNull MonthDay last, @NotNull LocalDate date) {
        MonthDay day = MonthDay.from(date);
        boolean started = !day.isBefore(first);
        boolean notOver = !day.isAfter(last);
        return first.isAfter(last) ? started || notOver : started && notOver;
    }
}
