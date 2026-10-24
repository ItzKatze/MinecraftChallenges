package gg.itzkatze.katze.lobby.npc.impl;

import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.lobby.cosmetics.CosmeticMenuGui;
import gg.itzkatze.katze.lobby.npc.LobbyNpc;
import gg.itzkatze.katze.lobby.npc.NpcSkins;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.KatzeColor;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class CosmeticShopNpc extends LobbyNpc {

    @Override
    public @NotNull String getId() {
        return "cosmetic_shop";
    }

    @Override
    public @NotNull Component getName() {
        return Component.text("Cosmetics", KatzeColor.MAGENTA);
    }

    @Override
    public @Nullable KatzeGUI createGui(@NotNull KatzePlayer player) {
        return new CosmeticMenuGui();
    }

    @Override
    public @Nullable ResolvableProfile getSkin() {
        return NpcSkins.ofTexture("c8c725c41078780e3595b2f03ae1616da78ff6aac6669cc43d3df7d953127148");
    }
}
