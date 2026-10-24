package gg.itzkatze.katze.lobby.npc;

import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class NpcSkins {

    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/";

    public static @NotNull ResolvableProfile ofPlayer(@NotNull String name) {
        return ResolvableProfile.resolvableProfile().name(name).build();
    }

    public static @NotNull ResolvableProfile ofTexture(@NotNull String hash) {
        String textures = "{\"textures\":{\"SKIN\":{\"url\":\"" + TEXTURE_URL + hash + "\"}}}";
        String value = Base64.getEncoder().encodeToString(textures.getBytes(StandardCharsets.UTF_8));
        return ResolvableProfile.resolvableProfile().addProperty(new ProfileProperty("textures", value)).build();
    }
}
