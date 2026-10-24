package gg.itzkatze.katze.allitems;

import gg.itzkatze.katze.allitems.data.AllItemsData;
import gg.itzkatze.katze.player.KatzePlayer;
import gg.itzkatze.katze.scoreboard.ScoreboardContent;
import gg.itzkatze.katze.util.KatzeColor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AllItemsScoreboardContent implements ScoreboardContent {

    private final AllItemsData data;

    public AllItemsScoreboardContent(AllItemsData data) {
        this.data = data;
    }

    @Override
    @NotNull public Component getTitle() {
        return Component.text("Collect All Items");
    }

    @Override
    @NotNull public Component[] getLines(@NotNull KatzePlayer player) {
        List<Component> lines = new ArrayList<>();

        String mode = AllItemsSettings.isTeamMode() ? "Team" : "Individual";
        lines.add(Component.text("Mode: ").color(KatzeColor.YELLOW)
                .append(Component.text(mode).color(KatzeColor.WHITE)));

        UUID playerId = player.getPlayer().getUniqueId();
        if (AllItemsSettings.isItemsEnabled()) {
            lines.add(buildProgressLine("Items", AllItemsObjectiveType.ITEM, playerId));
        }

        if (AllItemsSettings.isBiomesEnabled()) {
            lines.add(buildProgressLine("Biomes", AllItemsObjectiveType.BIOME, playerId));
        }
        if (AllItemsSettings.isMobsEnabled()) {
            lines.add(buildProgressLine("Mobs", AllItemsObjectiveType.MOB, playerId));
        }
        if (AllItemsSettings.isStructuresEnabled()) {
            lines.add(buildProgressLine("Structures", AllItemsObjectiveType.STRUCTURE, playerId));
        }
        if (AllItemsSettings.isAdvancementsEnabled()) {
            lines.add(buildProgressLine("Adv", AllItemsObjectiveType.ADVANCEMENT, playerId));
        }

        lines.add(Component.text(" "));

        List<AllItemsData.FoundObjective> recentItems = data.getRecentFinds(3);
        if (!recentItems.isEmpty()) {
            lines.add(Component.text("Recent Finds:").color(KatzeColor.GOLD));

            for (AllItemsData.FoundObjective item : recentItems) {
                String displayName = item.displayName().length() > 12
                        ? item.displayName().substring(0, 9) + "..."
                        : item.displayName();
                lines.add(Component.text(displayName).color(KatzeColor.WHITE));
            }
        }

        if (AllItemsSettings.isChainModeEnabled()) {
            AllItemsObjective objective = data.getCurrentObjective(player.getPlayer());
            if (objective != null) {
                lines.add(Component.text("Chain: ").color(KatzeColor.BLUE)
                        .append(Component.text(objective.displayName()).color(KatzeColor.WHITE)));
            }
        }

        return lines.toArray(new Component[0]);
    }

    private Component buildProgressLine(String label, AllItemsObjectiveType type, UUID playerId) {
        int found = data.getFoundCount(type, playerId);
        int total = switch (type) {
            case ITEM -> data.getTotalAvailableItems();
            case BIOME -> data.getTotalAvailableBiomes();
            case MOB -> data.getTotalAvailableMobs();
            case STRUCTURE -> data.getTotalAvailableStructures();
            case ADVANCEMENT -> data.getTotalAvailableAdvancements();
        };
        return Component.text(label + ": ").color(KatzeColor.LIME)
                .append(Component.text(found + "/" + total).color(KatzeColor.WHITE));
    }
}
