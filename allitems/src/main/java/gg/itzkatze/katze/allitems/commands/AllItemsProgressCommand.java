package gg.itzkatze.katze.allitems.commands;

import gg.itzkatze.katze.allitems.AllItems;
import gg.itzkatze.katze.allitems.AllItemsObjectiveType;
import gg.itzkatze.katze.allitems.AllItemsSettings;
import gg.itzkatze.katze.allitems.data.AllItemsData;
import gg.itzkatze.katze.challenges.ChallengeManager;
import gg.itzkatze.katze.challenges.ChallengeType;
import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class AllItemsProgressCommand extends KatzeCommand {

    public AllItemsProgressCommand() {
        super(
                "allitemsprogress",
                List.of("allitemsstats", "allitemsinfo"),
                CallerType.PLAYER,
                "allitemsprogress",
                PermissionDefault.TRUE,
                "View your progress in the All Items challenge"
        );
    }

    @Override
    protected void executeCommand(@NotNull CommandCaller caller, String[] args) {
        if (!ChallengeManager.isChallengeActive(ChallengeType.ALL_ITEMS)) {
            caller.sendMessage(Component.text("All Items challenge is not active."));
            return;
        }

        AllItemsData data = AllItems.getData();

        UUID playerId = caller instanceof KatzePlayer player ? player.getUuid() : null;
        int foundItems = data.getFoundCount(AllItemsObjectiveType.ITEM, playerId);
        int totalItems = data.getTotalAvailableItems();
        double itemPercent = data.getProgressPercentage(AllItemsObjectiveType.ITEM, playerId);

        caller.sendMessage(Component.text("Items: " + foundItems + "/" + totalItems + " (" + String.format("%.1f", itemPercent) + "%)"));

        if (AllItemsSettings.isBiomesEnabled()) {
            int foundBiomes = data.getFoundCount(AllItemsObjectiveType.BIOME, playerId);
            int totalBiomes = data.getTotalAvailableBiomes();
            double percent = data.getProgressPercentage(AllItemsObjectiveType.BIOME, playerId);
            caller.sendMessage(Component.text("Biomes: " + foundBiomes + "/" + totalBiomes + " (" + String.format("%.1f", percent) + "%)"));
        }
        if (AllItemsSettings.isMobsEnabled()) {
            int foundMobs = data.getFoundCount(AllItemsObjectiveType.MOB, playerId);
            int totalMobs = data.getTotalAvailableMobs();
            double percent = data.getProgressPercentage(AllItemsObjectiveType.MOB, playerId);
            caller.sendMessage(Component.text("Mobs: " + foundMobs + "/" + totalMobs + " (" + String.format("%.1f", percent) + "%)"));
        }
        if (AllItemsSettings.isStructuresEnabled()) {
            int foundStructures = data.getFoundCount(AllItemsObjectiveType.STRUCTURE, playerId);
            int totalStructures = data.getTotalAvailableStructures();
            double percent = data.getProgressPercentage(AllItemsObjectiveType.STRUCTURE, playerId);
            caller.sendMessage(Component.text("Structures: " + foundStructures + "/" + totalStructures + " (" + String.format("%.1f", percent) + "%)"));
        }
        if (AllItemsSettings.isAdvancementsEnabled()) {
            int foundAdv = data.getFoundCount(AllItemsObjectiveType.ADVANCEMENT, playerId);
            int totalAdv = data.getTotalAvailableAdvancements();
            double percent = data.getProgressPercentage(AllItemsObjectiveType.ADVANCEMENT, playerId);
            caller.sendMessage(Component.text("Advancements: " + foundAdv + "/" + totalAdv + " (" + String.format("%.1f", percent) + "%)"));
        }

        String mode = AllItemsSettings.isTeamMode() ? "Team" : "Individual";
        caller.sendMessage(Component.text("Current mode: " + mode));

        List<AllItemsData.FoundObjective> recentItems = data.getRecentFinds(5).stream()
                .sorted((a, b) -> b.entry().foundTime().compareTo(a.entry().foundTime()))
                .toList();

        if (!recentItems.isEmpty()) {
            caller.sendMessage(Component.text("Recent discoveries:"));

            for (AllItemsData.FoundObjective item : recentItems) {
                String itemName = item.displayName();
                caller.sendMessage(Component.text("  * " + itemName + " by " + item.entry().playerName() +
                        " at " + item.entry().getFormattedTime()));
            }
        }
    }

    @Override
    protected List<String> tabComplete(@NotNull CommandCaller caller, String[] args) {
        return List.of();
    }
}
