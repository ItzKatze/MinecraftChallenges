package gg.itzkatze.katze.lobby.npc;

import gg.itzkatze.katze.KatzeGames;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

final class NpcEntities {

    private static final NamespacedKey NPC_KEY = new NamespacedKey("katze", "lobby_npc");

    static @NotNull Mannequin spawn(@NotNull LobbyNpc npc, @NotNull Location location, @NotNull Player owner) {
        Mannequin copy = location.getWorld().spawn(location, Mannequin.class, mannequin -> {
            mannequin.setVisibleByDefault(false);
            mannequin.setPersistent(false);
            mannequin.setInvulnerable(true);
            mannequin.setSilent(true);
            mannequin.setCollidable(false);
            mannequin.customName(npc.getName());
            mannequin.setCustomNameVisible(true);
            mannequin.setDescription(npc.getDescription());
            ResolvableProfile skin = npc.getSkin();
            if (skin != null) {
                mannequin.setProfile(skin);
            }
            mannequin.getPersistentDataContainer().set(NPC_KEY, PersistentDataType.STRING, npc.getId());
        });
        copy.setRotation(location.getYaw(), 0);
        owner.showEntity(KatzeGames.get(), copy);
        return copy;
    }

    static void removeLeftovers(@NotNull World world, @NotNull BoundingBox bounds) {
        world.getNearbyEntities(bounds, entity -> entity.getPersistentDataContainer().has(NPC_KEY)).forEach(Entity::remove);
    }
}
