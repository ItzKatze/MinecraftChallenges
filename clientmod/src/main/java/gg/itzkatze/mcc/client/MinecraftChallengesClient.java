package gg.itzkatze.mcc.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;

public final class MinecraftChallengesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.clientboundPlay().register(MusicPayload.TYPE, MusicPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(MusicPayload.TYPE, (payload, context) -> context.client().execute(() -> handle(payload)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(RunMusicPlayer::stop));
    }

    private static void handle(MusicPayload payload) {
        switch (payload) {
            case MusicPayload.Play(Identifier sound, boolean file) -> RunMusicPlayer.play(sound, file);
            case MusicPayload.Fade(int ticks) -> RunMusicPlayer.fade(ticks);
            case MusicPayload.Stop() -> RunMusicPlayer.stop();
        }
    }
}
