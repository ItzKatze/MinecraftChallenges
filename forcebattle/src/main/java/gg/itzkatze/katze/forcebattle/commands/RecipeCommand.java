package gg.itzkatze.katze.forcebattle.commands;

import gg.itzkatze.katze.commands.framework.CallerType;
import gg.itzkatze.katze.commands.framework.KatzeCommand;
import gg.itzkatze.katze.commands.framework.CommandCaller;
import gg.itzkatze.katze.forcebattle.gui.CraftingRecipeGUI;
import gg.itzkatze.katze.player.KatzePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecipeCommand extends KatzeCommand {

    public RecipeCommand() {
        super(
                "recipe",
                List.of(),
                CallerType.PLAYER,
                "/recipe <item>",
                PermissionDefault.TRUE,
                "Opens the crafting recipe GUI for the specified item"
        );
    }

    @Override
    public void executeCommand(@NotNull CommandCaller caller, String[] args) {
        if (!(caller instanceof KatzePlayer player)) return;

        if (args.length == 0) {
            sendUsage(caller);
            return;
        }

        String input = args[0].toUpperCase();
        Material material = Material.matchMaterial(input);

        if (material == null) {
            caller.sendMessage(Component.text("Material couldn't be found."));
            return;
        }

        ItemStack stack = new ItemStack(material);
        new CraftingRecipeGUI(stack).open(player);
    }

    @Override
    public List<String> tabComplete(CommandCaller caller, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toUpperCase();
            List<String> suggestions = new ArrayList<>();
            for (Material mat : Material.values()) {
                if (mat.isItem() && mat.name().startsWith(prefix)) {
                    suggestions.add(mat.name().toLowerCase());
                }
            }
            return suggestions;
        }
        return List.of();
    }
}
