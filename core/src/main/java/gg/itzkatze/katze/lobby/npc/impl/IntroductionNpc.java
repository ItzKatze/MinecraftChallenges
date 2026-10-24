package gg.itzkatze.katze.lobby.npc.impl;

import gg.itzkatze.katze.lobby.npc.LobbyNpc;
import gg.itzkatze.katze.lobby.npc.NpcSkins;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.KatzeColor;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IntroductionNpc extends LobbyNpc {

    private static final String TEXT = """
            Die wichtigsten Commands auf einem Blick:

            /bp (bzw. /backpack) -> Öffnet den Backpack (quasi eine geteilte Enderchest für alle in einem Team)
            /chat team -> Stellt einen Chatmodus ein, in dem nur Teammitglieder deine Nachrichten sehen können
            /chat all -> Stellt den normalen Chatmodus ein (Alle sehen deine Nachrichten)
            /info -> Öffnet ein Menü, in dem du die verschiedenen Teamsaufgaben sehen kannst (Biome, Strukturen, Mobs, Tode)
            /recipe [Itemname] -> öffnet eine Anleitung, wie du dein Item herstellen kannst

            <-/-> Wie funktioniert das Battle? <-\\->
            Es gibt zwei Arten von Punkten, die du sammeln kannst:

            <-> Individualpunkte <->
            Du erhältst ein Objective, welches du abschließen musst (z.B. Item/Block/Höhe)
            Solltest du dieses Objective erfolgreich abschließen, erhältst du einen Punkt und erhältst eine weitere Aufgabe

            <-> Teampunkte <->
            Du erhältst ein Teamobjective, das jede Person deines Teams erledigen kann, um einen Teampunkt zu erreichen
            Sollte eine Person diese Aufgabe erfolgreich absolvieren, erhält dein Team einen Punkt und erhältst eine weitere Anforderung, welche geschafft werden muss

            Gewinner ist das Team mit den meisten Punkten

            Mögliche Aufgaben sind folgende:
            -Item: Du musst ein vorgegebenes Item einsammeln
            -Mob: Du musst einen vorgegebenen Mob besiegen
            -Biom: Du musst ein Biom aus der vorgegebenen Kategorie besuchen
            -Errungenschaft: Du musst eine vorgegebene Errungenschaft erreichen
            -Höhe: Du musst dich auf die vorgegebene Höhe begeben
            -Koordinaten: Du musst die vorgegebene Koordinate(n) erreichen
            -Struktur: Du musst eine Struktur aus der vorgegebenen Kategorie besuchen
            -Tod: Du musst auf eine vorgegebene Art sterben (Solltest du den richtigen Tod erleiden, behältst du deine Items)

            Du hast keine Lust auf dein zugewiesenes Item? Versuche es mal mit einem:
            -Skip (Barrier): Skippt dein aktuelles Item und du erhältst eine neue Anforderung
            -Swap (Structure Void): Swapt dein aktuelles Item mit dem Item einer anderen Person (aus einem anderen Team)
            -Skip (Recipe Book): Skippt deine aktuelle Teamaufgabe und weist dir eine neue Aufgabe zu
            """;

    private static final List<Component> DIALOGUE = TEXT.lines()
            .filter(line -> !line.isBlank())
            .<Component>map(Component::text)
            .toList();

    @Override
    public @NotNull String getId() {
        return "introduction";
    }

    @Override
    public @NotNull Component getName() {
        return Component.text("Guide", KatzeColor.GOLD);
    }

    @Override
    public @NotNull List<Component> getDialogue(@NotNull KatzePlayer player) {
        return DIALOGUE;
    }

    @Override
    public @Nullable ResolvableProfile getSkin() {
        return NpcSkins.ofTexture("dc5117690e3bc4f7a3385c138fa780bba403886b058c11bbe22862b9b6675fe7");
    }
}
