package gg.itzkatze.mcc.lobby.cosmetics.cases;

import gg.itzkatze.mcc.MinecraftChallenges;
import gg.itzkatze.mcc.gui.GUIClickableItem;
import gg.itzkatze.mcc.gui.GUIItem;
import gg.itzkatze.mcc.gui.MccGUI;
import gg.itzkatze.mcc.lobby.cosmetics.Cosmetic;
import gg.itzkatze.mcc.lobby.cosmetics.CosmeticStore;
import gg.itzkatze.mcc.lobby.cosmetics.PointsText;
import gg.itzkatze.mcc.lobby.cosmetics.Rarity;
import gg.itzkatze.mcc.lobby.cosmetics.ShopSounds;
import gg.itzkatze.mcc.player.MccPlayer;
import gg.itzkatze.mcc.util.ItemStackCreator;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class CaseRollGui extends MccGUI {

    private static final int TOP_MARKER_SLOT = 4;
    private static final int REEL_SLOT = 9;
    private static final int BOTTOM_ROW_SLOT = 18;
    private static final int AGAIN_SLOT = 19;
    private static final int BOTTOM_MARKER_SLOT = 22;
    private static final int CLOSE_SLOT = 25;
    private static final int CENTER = CaseReel.WIDTH / 2;
    private static final int PRIZE_SLOT = REEL_SLOT + CENTER;
    private static final int REVEAL_DELAY = 40;
    private static final Sound TICK = Sound.sound(Key.key("block.wooden_button.click_on"), Sound.Source.PLAYER, 1f, 1f);
    private static final Sound SMALL_WIN = Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 1f, 1f);
    private static final Sound GOOD_WIN = Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 1f, 1f);
    private static final Sound BIG_WIN = Sound.sound(Key.key("ui.toast.challenge_complete"), Sound.Source.PLAYER, 1f, 1f);
    private static final Map<UUID, CaseRollGui> ROLLING = new HashMap<>();

    private final Player player;
    private final CasePrize prize;
    private final CaseReel reel;
    private final boolean pity;
    private int position;
    private int wait = CaseReel.delay(0);
    private boolean paid;
    private boolean reopenPending;
    private @Nullable BukkitTask task;
    private int revealWait;
    private boolean stopped;

    CaseRollGui(@NotNull Player player, @NotNull CasePrize prize, @NotNull CaseReel reel, boolean pity) {
        super("Cosmetic Case", 27);
        this.player = player;
        this.prize = prize;
        this.reel = reel;
        this.pity = pity;
    }

    public static void begin(@NotNull MccPlayer clicker) {
        begin(clicker, ThreadLocalRandom.current());
    }

    static void begin(@NotNull MccPlayer clicker, @NotNull Random random) {
        Player player = clicker.getPlayer();
        if (player == null || ROLLING.containsKey(player.getUniqueId())) return;

        CosmeticStore store = CosmeticStore.get();
        if (!store.openCase(player, CaseLoot.PRICE)) {
            player.sendMessage(Component.text(PointsText.missing(CaseLoot.PRICE - store.points(player.getUniqueId())), NamedTextColor.RED));
            player.playSound(ShopSounds.DENY);
            player.closeInventory();
            return;
        }
        List<Cosmetic> available = CaseLoot.available(store.catalog(), store.today());
        List<Cosmetic> missing = CaseLoot.missing(available, cosmetic -> store.owns(player.getUniqueId(), cosmetic));
        boolean pity = CaseLoot.pityDue(store.duplicateStreak(player.getUniqueId()), missing);
        CasePrize prize = pity ? CaseLoot.drawPity(available, missing, random) : CaseLoot.drawPrize(available, random);
        new CaseRollGui(player, prize, CaseReel.build(prize, () -> CaseLoot.drawShown(available, random)), pity).start();
    }

    public static void finishAllSilently() {
        for (CaseRollGui roll : List.copyOf(ROLLING.values())) {
            roll.pay(true);
        }
    }

    static int running() {
        return ROLLING.size();
    }

    void start() {
        ROLLING.put(player.getUniqueId(), this);
        open(MccPlayer.adapt(player));
        task = Bukkit.getScheduler().runTaskTimer(MinecraftChallenges.get(), this::tick, 1, 1);
    }

    @Override
    public void setItems(MccPlayer viewer) {
        if (paid) {
            setRevealed();
            return;
        }
        List<CasePrize> window = reel.window(position);
        for (int column = 0; column < CaseReel.WIDTH; column++) {
            CasePrize shown = window.get(column);
            set(fixed(REEL_SLOT + column, render(shown, false)));
            if (column != CENTER) {
                ItemStack pane = pane(shown.rarity());
                set(fixed(column, pane));
                set(fixed(BOTTOM_ROW_SLOT + column, pane));
            }
        }
        set(fixed(TOP_MARKER_SLOT, ItemStackCreator.createNamedItemStack(Material.HOPPER, Component.text("▼", NamedTextColor.GRAY))));
        set(fixed(BOTTOM_MARKER_SLOT, ItemStackCreator.createNamedItemStack(Material.HOPPER, Component.text("▲", NamedTextColor.GRAY))));
    }

    private void setRevealed() {
        ItemStack frame = pane(prize.rarity());
        ItemStack filler = ItemStackCreator.fillerItem();
        for (int slot = 0; slot < size; slot++) {
            set(fixed(slot, isAroundPrize(slot) ? frame : filler));
        }
        set(fixed(PRIZE_SLOT, render(prize, true)));
        set(againButton());
        set(GUIClickableItem.getCloseItem(CLOSE_SLOT));
    }

    private static boolean isAroundPrize(int slot) {
        int rows = Math.abs(slot / CaseReel.WIDTH - PRIZE_SLOT / CaseReel.WIDTH);
        int columns = Math.abs(slot % CaseReel.WIDTH - CENTER);
        return slot != PRIZE_SLOT && rows <= 1 && columns <= 1;
    }

    @Override
    public boolean allowItemMoving() {
        return false;
    }

    // The roll screen can't be closed while the reel spins: it comes back on the next tick, and a player who leaves gets the prize silently.
    @Override
    public void onClose(InventoryCloseEvent event, CloseReason reason) {
        if (paid) return;
        if (event.getReason() == InventoryCloseEvent.Reason.DISCONNECT) {
            pay(true);
            return;
        }
        if (reopenPending) return;

        reopenPending = true;
        Bukkit.getScheduler().runTask(MinecraftChallenges.get(), this::reopen);
    }

    private void reopen() {
        reopenPending = false;
        if (paid) return;

        Player online = Bukkit.getPlayer(player.getUniqueId());
        if (online == null) {
            pay(true);
            return;
        }
        if (online.getOpenInventory().getTopInventory().getHolder() == this) return;
        open(MccPlayer.adapt(online));
    }

    private void tick() {
        Player online = Bukkit.getPlayer(player.getUniqueId());
        if (online == null) {
            pay(true);
            return;
        }

        if (stopped) {
            if (--revealWait <= 0) {
                pay(false);
            }
            return;
        }

        if (--wait > 0) return;

        position++;
        online.playSound(TICK);

        if (position == CaseReel.STEPS) {
            stopped = true;
            revealWait = REVEAL_DELAY;
            redraw();
            return;
        }

        wait = CaseReel.delay(position);
        redraw();
    }

    private void pay(boolean silent) {
        if (paid) return;

        paid = true;
        if (task != null) {
            task.cancel();
            task = null;
        }
        ROLLING.remove(player.getUniqueId(), this);
        List<Component> messages = award(CosmeticStore.get());
        if (silent) return;

        messages.forEach(player::sendMessage);
        player.playSound(winSound(prize.rarity()));
        CaseFireworks.play(player, prize.rarity());
        redraw();
    }

    private @NotNull List<Component> award(@NotNull CosmeticStore store) {
        if (prize instanceof CasePrize.Points(int amount, Rarity tier)) {
            store.addPoints(player, amount);
            return List.of(Component.text("You won ", NamedTextColor.GREEN)
                    .append(Component.text(PointsText.signed(amount), tier.color()))
                    .append(Component.text("!", NamedTextColor.GREEN)));
        }
        Cosmetic cosmetic = ((CasePrize.Item) prize).cosmetic();
        int refund = CaseLoot.refund(cosmetic.rarity());
        if (store.winCosmetic(player, cosmetic, refund)) {
            Component won = Component.text("You won ", NamedTextColor.GREEN).append(cosmetic.styledName()).append(Component.text("! Equip it in the Cosmetics menu.", NamedTextColor.GREEN));
            return pity ? List.of(won, Component.text("Your duplicate streak guaranteed it.", NamedTextColor.LIGHT_PURPLE)) : List.of(won);
        }
        return List.of(Component.text("You won ", NamedTextColor.YELLOW).append(cosmetic.styledName())
                .append(Component.text(", but you already own it: " + PointsText.signed(refund) + " instead.", NamedTextColor.YELLOW)));
    }

    private void redraw() {
        MccPlayer viewer = MccPlayer.adapt(player);
        setItems(viewer);
        updateInventory(viewer);
    }

    private @NotNull GUIClickableItem againButton() {
        return new GUIClickableItem(AGAIN_SLOT) {
            @Override
            public void run(InventoryClickEvent event, MccPlayer clicker) {
                CaseConfirmGui.offer(clicker);
            }

            @Override
            public ItemStack getItem(MccPlayer viewer) {
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
            public ItemStack getItem(MccPlayer viewer) {
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
