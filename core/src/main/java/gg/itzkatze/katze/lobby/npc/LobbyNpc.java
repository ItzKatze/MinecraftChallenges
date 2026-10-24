package gg.itzkatze.katze.lobby.npc;

import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class LobbyNpc {

    public abstract @NotNull String getId();

    public abstract @NotNull Component getName();

    public @Nullable ResolvableProfile getSkin() {
        return null;
    }

    public @Nullable Component getDescription() {
        return Component.text("CLICK", NamedTextColor.YELLOW, TextDecoration.BOLD);
    }

    public @NotNull List<Component> getDialogue(@NotNull KatzePlayer player) {
        return List.of();
    }

    public @NotNull DialogueMode getDialogueMode() {
        return DialogueMode.EVERY_TIME;
    }

    public @Nullable KatzeGUI createGui(@NotNull KatzePlayer player) {
        return null;
    }
}
