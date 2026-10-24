package gg.itzkatze.katze.forcebattle;

import gg.itzkatze.katze.util.ItemStackCreator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public final class ForceBattleGuideBook {

    private static final NamespacedKey KEY = new NamespacedKey("katzegames", "forcebattle_guide_book");
    private static final String TITLE = "ForceBattle";
    private static final String AUTHOR = "ForceBattle";
    private static final List<String> PAGES = List.of(
            "\n\n\n§3\n        Willkommen \n\n            bei\n\n       ForceBattle",
            "§8§l\u003c-\u003eWie funktioniert das Battle?\u003c-\u003e§0\n\nEs gibt zwei Arten von Punkten, die du sammeln kannst:\n§4\n§l- Individualpunkte\n- Teampunkte§c\n§0\nGewinner ist das Team mit den meisten gesammelten Punkten.",
            "§4§lIndividualpunkte§0\n\nDu erhältst ein Objective, das du abschließen musst (z.B. Item/Block/Höhe).\n\nSolltest du dieses Objective erfolgreich abschließen, erhältst du einen Punkt und bekommst eine weitere Aufgabe.",
            "§4§lTeampunkte§0\n\nDu erhältst ein Teamobjective, das jede Person deines Teams erledigen kann.\n\nSollte eine Person deines Teams das Objective abschließen bzw. erledigen, erhält dein Team einen Punkt und eine weitere Aufgabe.",
            "§lMögliche Aufgaben (Objectives) sind folgende:\n§0\n§9§lItem:§0 \nDu musst ein vorgegebenes Item einsammeln.\n\n§9§lMob: §0\nDu musst einen vorgegebenen Mob besiegen.",
            "§9§lBiom:§0 \nDu musst ein Biom aus der vorgegebenen Kategorie besuchen.\n\n§9§lAdvancement:§0\nDu musst ein vorgegebenes Advancement erreichen.\n\n",
            "§9§lHöhe:§0 \nDu musst dich auf die vorgegebene Höhe begeben.\n\n§9§lKoordinaten:§0 \nDu musst die vorgegebene Koordinate(n) erreichen.\n",
            "§9§lStruktur:§0 \nDu musst eine Struktur aus der vorgegebenen Kategorie besuchen.\n\n§9§lTod:§0 \nDu musst auf eine vorgegebene Art sterben.\n§7§o(Solltest du den richtigen Tod erleiden, behältst du deine Items)",
            "§5§lDu hast keine Lust auf dein zugewiesenes Item?\n§0\nVersuche es mal mit einem:\n\n§8§lSkip (Barrier):§0\nSkippt dein aktuelles Item und du erhältst eine neue Anforderung.",
            "§8§lSwap (Structure Void):§0\nSwapt dein aktuelles Item mit dem Item einer anderen Person (aus einem anderen Team).\n\n§8§lSkip (Recipe Book):§0 Skippt deine aktuelle Teamaufgabe und weist dir eine neue Aufgabe zu.",
            "§8§lDie wichtigsten Commands auf einen Blick:\n\n§5§l/bp (bzw.) /backpack§5 §8\n...ö§0ffnet den Backpack (eine geteilte Ender-\nchest für alle in einem Team)§8\n",
            "§5§l/chat team§0\n...stellt einen Chat-\nmodus ein, in dem nur Teammitglieder deine Nachrichten sehen können\n\n§5§l/chat all§0\n...stellt den normalen Chatmodus ein (Alle sehen deine Nachrichten)",
            "§5§l/info§0\n...öffnet ein Menü, in dem du die verschiedenen Teamaufgaben sehen kannst (Biome, Strukturen, Mobs, Tode)\n\n§5§l/recipe [Itemname]§0 ...öffnet eine Anleitung, wie du dein Item herstellen kannst",
            "§5§l/team create [Zahl]§0\n...erstellt ein Team, in dem du mit Partnern spielen kannst\n\n\n§5§l/team join [Zahl]§0\n...tritt dem entsprechenden Team bei",
            "§3§l\n\n\n\n\n   Viel Spaß beim          Spielen!  \n           \n          :D"
    );

    private ForceBattleGuideBook() {
    }

    public static @NotNull ItemStack create() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.title(Component.text(TITLE));
        meta.author(Component.text(AUTHOR));
        meta.addPages(PAGES.stream().map(LegacyComponentSerializer.legacySection()::deserialize).toArray(Component[]::new));
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.INTEGER, 1);
        book.setItemMeta(meta);
        return book;
    }

    public static boolean isGuideBook(@NotNull ItemStack stack) {
        return ItemStackCreator.hasData(stack, KEY);
    }

    public static void give(@NotNull Player player) {
        PlayerInventory inventory = player.getInventory();
        boolean owned = Arrays.stream(inventory.getContents()).anyMatch(stack -> stack != null && isGuideBook(stack));
        if (!owned) {
            inventory.addItem(create());
        }
    }
}
