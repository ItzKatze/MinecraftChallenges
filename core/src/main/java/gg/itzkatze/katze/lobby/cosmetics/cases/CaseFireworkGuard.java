package gg.itzkatze.katze.lobby.cosmetics.cases;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

// A real rocket's explosion hurts every living entity within 5 blocks; the case's rockets are only for show.
public final class CaseFireworkGuard implements Listener {

    @EventHandler
    public void onDamage(@NotNull EntityDamageEvent event) {
        if (CaseFireworks.isCaseFirework(event.getDamageSource().getDirectEntity())) {
            event.setCancelled(true);
        }
    }
}
