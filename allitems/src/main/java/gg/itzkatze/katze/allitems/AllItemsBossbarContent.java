package gg.itzkatze.katze.allitems;

import gg.itzkatze.katze.allitems.data.AllItemsData;
import gg.itzkatze.katze.bossbar.BossbarContent;
import gg.itzkatze.katze.bossbar.BossbarKind;
import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AllItemsBossbarContent implements BossbarContent {

    private final AllItemsData data;
    private static final long CYCLE_MILLIS = 5000L;

    public AllItemsBossbarContent(AllItemsData data) {
        this.data = data;
    }

    @Override
    @NotNull public BossbarKind getKind() {
        return BossbarKind.RUNNING;
    }

    @Override
    @Nullable public BossBar getBossbar(@NotNull KatzePlayer player) {
        List<AllItemsObjectiveType> enabled = getEnabledTypes();
        if (enabled.isEmpty()) {
            return null;
        }

        int index = (int) ((System.currentTimeMillis() / CYCLE_MILLIS) % enabled.size());
        AllItemsObjectiveType type = enabled.get(index);
        double percent = data.getProgressPercentage(type, player.getPlayer().getUniqueId());
        int found = data.getFoundCount(type, player.getPlayer().getUniqueId());

        int total = switch (type) {
            case ITEM -> data.getTotalAvailableItems();
            case BIOME -> data.getTotalAvailableBiomes();
            case MOB -> data.getTotalAvailableMobs();
            case STRUCTURE -> data.getTotalAvailableStructures();
            case ADVANCEMENT -> data.getTotalAvailableAdvancements();
        };

        String label = AllItemsSettings.isTeamMode() ? "Team" : "Your";
        String title = String.format("%s - %s: %d/%d (%.1f%%)",
                type.getDisplayName(), label, found, total, percent);

        return BossBar.bossBar(
                Component.text(title),
                (float) Math.clamp(percent / 100.0, 0.0, 1.0),
                BossBar.Color.GREEN,
                BossBar.Overlay.PROGRESS
        );
    }

    private List<AllItemsObjectiveType> getEnabledTypes() {
        List<AllItemsObjectiveType> enabled = new ArrayList<>();
        if (AllItemsSettings.isItemsEnabled()) enabled.add(AllItemsObjectiveType.ITEM);
        if (AllItemsSettings.isBiomesEnabled()) enabled.add(AllItemsObjectiveType.BIOME);
        if (AllItemsSettings.isMobsEnabled()) enabled.add(AllItemsObjectiveType.MOB);
        if (AllItemsSettings.isStructuresEnabled()) enabled.add(AllItemsObjectiveType.STRUCTURE);
        if (AllItemsSettings.isAdvancementsEnabled()) enabled.add(AllItemsObjectiveType.ADVANCEMENT);
        return enabled;
    }
}
