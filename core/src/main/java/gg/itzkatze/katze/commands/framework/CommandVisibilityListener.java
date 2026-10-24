package gg.itzkatze.katze.commands.framework;

import gg.itzkatze.katze.KatzeGames;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandVisibilityListener implements Listener {

    private static Set<String> ourCommandNames;

    private static Set<String> getOurCommandNames() {
        if (ourCommandNames != null) {
            return ourCommandNames;
        }
        PluginDescriptionFile desc = KatzeGames.get().getDescription();
        Map<String, Map<String, Object>> commands = desc.getCommands();
        Set<String> names = new HashSet<>();
        for (Map.Entry<String, Map<String, Object>> entry : commands.entrySet()) {
            names.add(entry.getKey().toLowerCase());
            Map<String, Object> cmd = entry.getValue();
            if (cmd != null && cmd.containsKey("aliases")) {
                Object aliases = cmd.get("aliases");
                if (aliases instanceof Collection<?> list) {
                    for (Object a : list) {
                        if (a != null) names.add(a.toString().toLowerCase());
                    }
                } else if (aliases != null) {
                    names.add(aliases.toString().toLowerCase());
                }
            }
        }
        ourCommandNames = names;
        return ourCommandNames;
    }

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        Set<String> registered = KatzeCommand.getRegisteredCommandNames();
        Set<String> ours = getOurCommandNames();
        // Remove any of our plugin's commands that are not currently registered
        event.getCommands().removeIf(name -> {
            String lower = name.toLowerCase();
            return ours.contains(lower) && !registered.contains(lower);
        });
    }
}
