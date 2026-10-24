package gg.itzkatze.katze.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class EventRegistrar {
    public static void registerAll(JavaPlugin plugin, String basePackage) {
        try {
            String path = basePackage.replace('.', '/');
            URL resource = plugin.getClass().getClassLoader().getResource(path);
            if (resource == null) return;

            String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
            try (JarFile jar = new JarFile(jarPath)) {
                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (name.startsWith(path) && name.endsWith(".class") && !name.contains("$")) {
                        String className = name.replace('/', '.').substring(0, name.length() - 6);
                        try {
                            Class<?> clazz = Class.forName(className);
                            if (Listener.class.isAssignableFrom(clazz)) {
                                Listener listener = (Listener) clazz.getDeclaredConstructor().newInstance();
                                Bukkit.getPluginManager().registerEvents(listener, plugin);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
