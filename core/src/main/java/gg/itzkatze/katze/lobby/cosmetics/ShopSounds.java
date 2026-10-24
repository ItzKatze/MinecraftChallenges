package gg.itzkatze.katze.lobby.cosmetics;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public final class ShopSounds {

    public static final Sound CLICK = Sound.sound(Key.key("ui.button.click"), Sound.Source.UI, 1f, 1f);
    public static final Sound DENY = Sound.sound(Key.key("entity.villager.no"), Sound.Source.PLAYER, 1f, 1f);

    private ShopSounds() {
    }
}
