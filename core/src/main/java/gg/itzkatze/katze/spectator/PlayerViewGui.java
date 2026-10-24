package gg.itzkatze.katze.spectator;

import gg.itzkatze.katze.challenges.Challenge;
import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.GUIItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.team.KatzeTeam;
import gg.itzkatze.katze.util.ItemStackCreator;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class PlayerViewGui extends KatzeGUI implements SpectatorView {

    private static final int OFFHAND_SLOT = 5;
    private static final int STORAGE_START = 9;
    private static final int HOTBAR_START = 36;
    private static final int HOTBAR_SIZE = 9;
    private static final int BACKPACK_SLOT = 47;
    private static final int INFO_SLOT = 49;
    private static final int CAMERA_SLOT = 51;

    private final UUID targetId;
    private final String targetName;

    public PlayerViewGui(@NotNull KatzePlayer target) {
        super("Spectating " + target.getName(), 54);
        this.targetId = target.getUuid();
        this.targetName = target.getName();
    }

    public static @NotNull Component infoLine(@NotNull String label, @NotNull Component value) {
        return Component.text(label + ": ", KatzeColor.LIGHT_GRAY).append(value.colorIfAbsent(KatzeColor.WHITE));
    }

    @Override
    public @NotNull UUID targetId() {
        return targetId;
    }

    @Override
    public @NotNull String targetName() {
        return targetName;
    }

    @Override
    public void setItems(KatzePlayer viewer) {
        fill(ItemStackCreator.fillerItem());
        set(equipment(0, PlayerInventory::getHelmet, "No helmet"));
        set(equipment(1, PlayerInventory::getChestplate, "No chestplate"));
        set(equipment(2, PlayerInventory::getLeggings, "No leggings"));
        set(equipment(3, PlayerInventory::getBoots, "No boots"));
        set(equipment(OFFHAND_SLOT, PlayerInventory::getItemInOffHand, "No offhand item"));
        for (int slot = STORAGE_START; slot < HOTBAR_START; slot++) {
            set(mirror(slot, slot));
        }
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            set(mirror(HOTBAR_START + slot, slot));
        }
        set(backpackButton());
        set(infoHead());
        set(cameraButton());
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }

    private @Nullable Player target() {
        return Bukkit.getPlayer(targetId);
    }

    private @NotNull GUIItem mirror(int guiSlot, int inventorySlot) {
        return new GUIItem(guiSlot) {
            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                Player target = target();
                return target == null ? null : copy(target.getInventory().getItem(inventorySlot));
            }
        };
    }

    private @NotNull GUIItem equipment(int slot, @NotNull Function<PlayerInventory, ItemStack> piece, @NotNull String emptyName) {
        return new GUIItem(slot) {
            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                Player target = target();
                ItemStack worn = target == null ? null : copy(piece.apply(target.getInventory()));
                return worn != null ? worn : ItemStackCreator.createNamedItemStack(Material.GRAY_STAINED_GLASS_PANE, Component.text(emptyName, KatzeColor.LIGHT_GRAY));
            }
        };
    }

    private @NotNull GUIClickableItem backpackButton() {
        return new GUIClickableItem(BACKPACK_SLOT) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer viewer) {
                KatzePlayer target = KatzePlayer.adapt(targetId).orElse(null);
                if (target != null && target.getBackpackContents() != null) {
                    new BackpackViewGui(target).open(viewer);
                }
            }

            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                KatzePlayer target = KatzePlayer.adapt(targetId).orElse(null);
                if (target == null || target.getBackpackContents() == null) {
                    return ItemStackCreator.createNamedItemStack(Material.GRAY_DYE, Component.text("No backpack", KatzeColor.LIGHT_GRAY));
                }
                return ItemStackCreator.createNamedItemStack(Material.CHEST, Component.text("Backpack", KatzeColor.YELLOW));
            }
        };
    }

    private @NotNull GUIItem infoHead() {
        return new GUIItem(INFO_SLOT) {
            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                Player target = target();
                if (target == null) return ItemStackCreator.fillerItem();
                return ItemStackCreator.getPlayerSkull(targetId, Component.text(targetName, KatzeColor.YELLOW), 1, infoLines(target).toArray(Component[]::new));
            }
        };
    }

    private @NotNull List<Component> infoLines(@NotNull Player target) {
        KatzePlayer wrapper = KatzePlayer.adapt(targetId).orElse(null);
        KatzeTeam team = wrapper == null ? null : wrapper.getTeam();
        Location location = target.getLocation();
        List<Component> lines = new ArrayList<>(List.of(
                infoLine("Team", Component.text(team == null ? "none" : String.valueOf(team.getId()))),
                infoLine("Health", Component.text(decimal(target.getHealth()) + " / " + decimal(maxHealth(target)))),
                infoLine("Food", Component.text(target.getFoodLevel() + " / 20")),
                infoLine("Level", Component.text(target.getLevel())),
                infoLine("World", Component.text(target.getWorld().getName())),
                infoLine("Position", Component.text(location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ()))));
        if (wrapper != null) {
            ChallengeManager.getActiveChallenges().stream()
                    .sorted(Comparator.comparing(Challenge::getType))
                    .forEach(challenge -> lines.addAll(challenge.getSpectatorInfo(wrapper)));
        }
        return lines;
    }

    private @NotNull GUIClickableItem cameraButton() {
        return new GUIClickableItem(CAMERA_SLOT) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer viewer) {
                Player spectator = viewer.getPlayer();
                Player target = target();
                if (spectator == null || target == null || spectator.getGameMode() != GameMode.SPECTATOR) return;

                spectator.closeInventory();
                spectator.setSpectatorTarget(target);
            }

            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                return ItemStackCreator.getStack(Component.text("View through their eyes", KatzeColor.YELLOW), Material.ENDER_EYE,
                        List.of(Component.text("Sneak to leave", KatzeColor.LIGHT_GRAY)));
            }
        };
    }

    private static @Nullable ItemStack copy(@Nullable ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : stack.clone();
    }

    private static double maxHealth(@NotNull Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        return maxHealth == null ? 20 : maxHealth.getValue();
    }

    private static @NotNull String decimal(double value) {
        double rounded = Math.round(value * 10) / 10.0;
        return rounded == Math.rint(rounded) ? String.valueOf((long) rounded) : String.valueOf(rounded);
    }
}
