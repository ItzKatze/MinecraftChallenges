package gg.itzkatze.katze.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.UUID;

public enum Skull {
    NUMBER_0("0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27"),
    NUMBER_1("71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530"),
    NUMBER_2("4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847"),
    NUMBER_3("1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5"),
    NUMBER_4("d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5"),
    NUMBER_5("6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2"),
    NUMBER_6("334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab"),
    NUMBER_7("6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9"),
    NUMBER_8("59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5"),
    NUMBER_9("e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840"),
    COLON("ccbee28e2c79db138f3977ba472dfae6b11a9bb82d5b3d7f25479338fff1fe92"),
    SLASH("7f95d7c1bbf3afa285d8d96757bb5572259a3ae854f5389dc53207699d94fd8"),
    INFO("d01afe973c5482fdc71e6aa10698833c79c437f21308ea9a1a095746ec274a0f"),
    QUESTION_MARK("badc048a7ce78f7dad72a07da27d85c0916881e5522eeed1e3daf217a38c1a"),
    HEART("336febeca7c488a6671dc071655dde2a1b65c3ccb20b6e8eaf9bfb08e64b80"),
    ARROW_LEFT("bd69e06e5dadfd84e5f3d1c21063f2553b2fa945ee1d4d7152fdc5425bc12a9"),
    ARROW_RIGHT("19bf3292e126a105b54eba713aa1b152d541a1d8938829c56364d178ed22bf"),
    ARROW_UP("3040fe836a6c2fbd2c7a9c8ec6be5174fddf1ac20f55e366156fa5f712e10"),
    ARROW_DOWN("7437346d8bda78d525d19f540a95e4e79daeda795cbc5a13256236312cf"),
    TRAFFIC_LIGHT("b0bf80b7142a2d076e4c13377bad9778e22a74c309f153c0324e21a4b5b965a"),
    FLAG_UK("8831c73f5468e888c3019e2847e442dfaa88898d50ccf01fd2f914af544d5368"),
    FLAG_GERMANY("5e7899b4806858697e283f084d9173fe487886453774626b24bd8cfecc77b3f"),
    FLAG_CHINA("7f9bc035cdc80f1ab5e1198f29f3ad3fdd2b42d9a69aeb64de990681800b98dc"),
    GITHUB("26e27da12819a8b053da0cc2b62dec4cda91de6eeec21ccf3bfe6dd8d4436a7");

    private final String url;

    Skull(String url) {
        this.url = url;
    }

    public @NotNull ItemStack asItemStack() {
        ItemStack skullStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skullStack.getItemMeta();
        PlayerProfile profile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();

        try {
            textures.setSkin(URI.create("https://textures.minecraft.net/texture/" + url).toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        profile.setTextures(textures);

        if (skullMeta != null) {
            skullMeta.setOwnerProfile(profile);
            skullStack.setItemMeta(skullMeta);
        }

        return skullStack;
    }

    public static class PlayerSkulls {

        public static @NotNull ItemStack getSkullByUUID(UUID uuid) {
            ItemStack skullStack = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skullStack.getItemMeta();
            PlayerProfile profile = Bukkit.getOfflinePlayer(uuid).getPlayerProfile();

            if (skullMeta != null) {
                skullMeta.setOwnerProfile(profile);
                skullStack.setItemMeta(skullMeta);
            }

            return skullStack;
        }

    }
}
