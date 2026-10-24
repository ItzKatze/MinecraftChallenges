package gg.itzkatze.katze.lobby.cosmetics.cases;

import gg.itzkatze.katze.KatzeGames;
import gg.itzkatze.katze.gui.GUIClickableItem;
import gg.itzkatze.katze.gui.GUIItem;
import gg.itzkatze.katze.gui.KatzeGUI;
import gg.itzkatze.katze.lobby.cosmetics.Cosmetic;
import gg.itzkatze.katze.lobby.cosmetics.CosmeticStore;
import gg.itzkatze.katze.lobby.cosmetics.PointsText;
import gg.itzkatze.katze.lobby.cosmetics.Rarity;
import gg.itzkatze.katze.lobby.cosmetics.ShopSounds;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.util.ItemStackCreator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class CaseRollGui extends KatzeGUI {

    static final int MAX_CASES = 4;
    private static final int CENTER = CaseReel.WIDTH / 2;
    private static final int AGAIN_COLUMN = 1;
    private static final int CLOSE_COLUMN = 7;
    private static final Sound TICK = Sound.sound(Key.key("block.wooden_button.click_on"), Sound.Source.PLAYER, 1f, 1f);
    private static final Sound SMALL_WIN = Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 1f, 1f);
    private static final Sound GOOD_WIN = Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 1f, 1f);
    private static final Sound BIG_WIN = Sound.sound(Key.key("ui.toast.challenge_complete"), Sound.Source.PLAYER, 1f, 1f);
    private static final Map<UUID, CaseRollGui> ROLLING = new HashMap<>();

    private final Player player;
    private final List<CaseSpin> spins;
    private boolean reopenPending;
    private @Nullable BukkitTask task;

    CaseRollGui(@NotNull Player player, @NotNull List<CaseSpin> spins) {
        super("Cosmetic Case", CaseReel.WIDTH * (spins.size() + 2));
        this.player = player;
        this.spins = List.copyOf(spins);
    }

    public static boolean begin(@NotNull KatzePlayer clicker, int count) {
        return begin(clicker, count, ThreadLocalRandom.current());
    }

    static boolean begin(@NotNull KatzePlayer clicker, int count, @NotNull Random random) {
        Player player = clicker.getPlayer();
        if (player == null || count < 1 || count > MAX_CASES || ROLLING.containsKey(player.getUniqueId())) return false;

        UUID id = player.getUniqueId();
        CosmeticStore store = CosmeticStore.get();
        if (!store.openCases(player, CaseLoot.PRICE, count)) {
            player.sendMessage(Component.text(PointsText.missing(CaseLoot.PRICE * count - store.points(id)), NamedTextColor.RED));
            player.playSound(ShopSounds.DENY);
            return false;
        }
        List<Cosmetic> available = CaseLoot.available(store.catalog(), store.today());
        List<CaseLoot.Draw> draws = CaseLoot.drawCases(available, cosmetic -> store.owns(id, cosmetic), store.duplicateStreak(id), count, random);
        List<CaseSpin> spins = new ArrayList<>(draws.size());
        for (int row = 0; row < draws.size(); row++) {
            CaseLoot.Draw draw = draws.get(row);
            spins.add(new CaseSpin(CaseReel.build(draw.prize(), () -> CaseLoot.drawShown(available, random), row), draw.pity()));
        }
        new CaseRollGui(player, spins).start();
        return true;
    }

    public static void finishAllSilently() {
        for (CaseRollGui roll : List.copyOf(ROLLING.values())) {
            roll.finishSilently();
        }
    }

    static int running() {
        return ROLLING.size();
    }

    void start() {
        ROLLING.put(player.getUniqueId(), this);
        open(KatzePlayer.adapt(player));
        task = Bukkit.getScheduler().runTaskTimer(KatzeGames.get(), this::tick, 1, 1);
    }

    @Override
    public void setItems(KatzePlayer viewer) {
        int bottom = size - CaseReel.WIDTH;
        setEdgeRow(0, spins.getFirst(), "▼");
        for (int row = 0; row < spins.size(); row++) {
            setReelRow(CaseReel.WIDTH * (row + 1), spins.get(row));
        }
        setEdgeRow(bottom, spins.getLast(), "▲");
        if (isFinished()) {
            set(againButton(bottom + AGAIN_COLUMN));
            set(GUIClickableItem.getCloseItem(bottom + CLOSE_COLUMN));
        }
    }

    // The top row belongs to the first reel and the bottom row to the last one: their panes follow the reel, and its paid prize gets them as its frame.
    private void setEdgeRow(int start, @NotNull CaseSpin spin, @NotNull String marker) {
        if (spin.paid()) {
            ItemStack frame = pane(spin.prize().rarity());
            ItemStack filler = ItemStackCreator.fillerItem();
            for (int column = 0; column < CaseReel.WIDTH; column++) {
                set(fixed(start + column, Math.abs(column - CENTER) <= 1 ? frame : filler));
            }
            return;
        }
        List<CasePrize> window = spin.reel().window(spin.position());
        for (int column = 0; column < CaseReel.WIDTH; column++) {
            ItemStack stack = column == CENTER ? ItemStackCreator.createNamedItemStack(Material.HOPPER, Component.text(marker, NamedTextColor.GRAY))
                    : pane(window.get(column).rarity());
            set(fixed(start + column, stack));
        }
    }

    private void setReelRow(int start, @NotNull CaseSpin spin) {
        if (spin.paid()) {
            ItemStack frame = pane(spin.prize().rarity());
            ItemStack filler = ItemStackCreator.fillerItem();
            for (int column = 0; column < CaseReel.WIDTH; column++) {
                ItemStack stack = column == CENTER ? render(spin.prize(), true) : Math.abs(column - CENTER) == 1 ? frame : filler;
                set(fixed(start + column, stack));
            }
            return;
        }
        List<CasePrize> window = spin.reel().window(spin.position());
        for (int column = 0; column < CaseReel.WIDTH; column++) {
            set(fixed(start + column, render(window.get(column), false)));
        }
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }

    // The roll screen can't be closed before its last row has finished: it comes back on the next tick, and a player who leaves gets every prize not yet paid silently.
    @Override
    public void onClose(InventoryCloseEvent event, CloseReason reason) {
        if (isFinished()) return;
        if (event.getReason() == InventoryCloseEvent.Reason.DISCONNECT) {
            finishSilently();
            return;
        }
        if (reopenPending) return;

        reopenPending = true;
        Bukkit.getScheduler().runTask(KatzeGames.get(), this::reopen);
    }

    private void reopen() {
        reopenPending = false;
        if (isFinished()) return;

        Player online = Bukkit.getPlayer(player.getUniqueId());
        if (online == null) {
            finishSilently();
            return;
        }
        if (online.getOpenInventory().getTopInventory().getHolder() == this) return;
        open(KatzePlayer.adapt(online));
    }

    private void tick() {
        Player online = Bukkit.getPlayer(player.getUniqueId());
        if (online == null) {
            finishSilently();
            return;
        }

        boolean moved = false;
        boolean changed = false;
        for (CaseSpin spin : spins) {
            switch (spin.tick()) {
                case MOVED -> {
                    moved = true;
                    changed = true;
                }
                case DUE -> {
                    pay(spin, false);
                    changed = true;
                }
                case NONE -> {
                }
            }
        }
        if (moved) {
            online.playSound(TICK);
        }
        if (isFinished()) {
            stop();
        }
        if (changed) {
            redraw();
        }
    }

    private boolean isFinished() {
        return spins.stream().allMatch(CaseSpin::paid);
    }

    private void finishSilently() {
        for (CaseSpin spin : spins) {
            pay(spin, true);
        }
        stop();
    }

    private void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        ROLLING.remove(player.getUniqueId(), this);
    }

    private void pay(@NotNull CaseSpin spin, boolean silent) {
        if (!spin.settle()) return;

        List<Component> messages = award(CosmeticStore.get(), spin);
        if (silent) return;

        messages.forEach(player::sendMessage);
        player.playSound(winSound(spin.prize().rarity()));
        CaseFireworks.play(player, spin.prize().rarity());
    }

    private @NotNull List<Component> award(@NotNull CosmeticStore store, @NotNull CaseSpin spin) {
        if (spin.prize() instanceof CasePrize.Points(int amount, Rarity tier)) {
            store.addPoints(player, amount);
            return List.of(Component.text("You won ", NamedTextColor.GREEN)
                    .append(Component.text(PointsText.signed(amount), tier.color()))
                    .append(Component.text("!", NamedTextColor.GREEN)));
        }
        Cosmetic cosmetic = ((CasePrize.Item) spin.prize()).cosmetic();
        int refund = CaseLoot.refund(cosmetic.rarity());
        if (store.winCosmetic(player, cosmetic, refund)) {
            Component won = Component.text("You won ", NamedTextColor.GREEN).append(cosmetic.styledName()).append(Component.text("! Equip it in the Cosmetics menu.", NamedTextColor.GREEN));
            return spin.pity() ? List.of(won, Component.text("Your duplicate streak guaranteed it.", NamedTextColor.LIGHT_PURPLE)) : List.of(won);
        }
        return List.of(Component.text("You won ", NamedTextColor.YELLOW).append(cosmetic.styledName())
                .append(Component.text(", but you already own it: " + PointsText.signed(refund) + " instead.", NamedTextColor.YELLOW)));
    }

    private void redraw() {
        KatzePlayer viewer = KatzePlayer.adapt(player);
        setItems(viewer);
        updateInventory(viewer);
    }

    private @NotNull GUIClickableItem againButton(int slot) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryClickEvent event, KatzePlayer clicker) {
                CaseConfirmGui.offer(clicker);
            }

            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                int balance = CosmeticStore.get().points(viewer.getUuid());
                return ItemStackCreator.getStack(Component.text("Open another case", NamedTextColor.LIGHT_PURPLE), Material.ENDER_CHEST,
                        List.of(Component.text("Price: " + PointsText.amount(CaseLoot.PRICE), NamedTextColor.GOLD), Component.text("You have " + PointsText.amount(balance), NamedTextColor.GRAY),
                                CaseConfirmGui.pityLine(viewer.getUuid())));
            }
        };
    }

    private static @NotNull GUIItem fixed(int slot, @NotNull ItemStack stack) {
        return new GUIItem(slot) {
            @Override
            public ItemStack getItem(KatzePlayer viewer) {
                return stack;
            }
        };
    }

    private static @NotNull ItemStack render(@NotNull CasePrize prize, boolean glint) {
        Rarity tier = prize.rarity();
        Component rarity = Component.text("Rarity: " + tier.displayName(), tier.color());
        ItemStack stack = switch (prize) {
            case CasePrize.Points points -> ItemStackCreator.getStack(Component.text(PointsText.signed(points.amount()), tier.color()), pointsIcon(points.amount()), List.of(rarity));
            case CasePrize.Item item -> ItemStackCreator.getStack(item.cosmetic().styledName(), item.cosmetic().icon(),
                    List.of(rarity, Component.text(item.cosmetic().category().title(), NamedTextColor.GRAY)));
        };
        if (glint) {
            ItemMeta meta = stack.getItemMeta();
            meta.setEnchantmentGlintOverride(true);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static @NotNull Material pointsIcon(int amount) {
        if (amount < 100) return Material.GOLD_NUGGET;
        return amount < 500 ? Material.GOLD_INGOT : Material.GOLD_BLOCK;
    }

    private static @NotNull ItemStack pane(@NotNull Rarity tier) {
        Material material = switch (tier) {
            case COMMON -> Material.WHITE_STAINED_GLASS_PANE;
            case UNCOMMON -> Material.LIME_STAINED_GLASS_PANE;
            case RARE -> Material.BLUE_STAINED_GLASS_PANE;
            case EPIC -> Material.PURPLE_STAINED_GLASS_PANE;
            case LEGENDARY -> Material.ORANGE_STAINED_GLASS_PANE;
            case MYTHIC -> Material.MAGENTA_STAINED_GLASS_PANE;
        };
        return ItemStackCreator.createNamedItemStack(material, Component.text(tier.displayName(), tier.color()));
    }

    private static @NotNull Sound winSound(@NotNull Rarity tier) {
        return switch (tier) {
            case COMMON, UNCOMMON -> SMALL_WIN;
            case RARE, EPIC -> GOOD_WIN;
            case LEGENDARY, MYTHIC -> BIG_WIN;
        };
    }
}
