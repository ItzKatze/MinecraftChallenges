package gg.itzkatze.katze.client;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// The plugin's MusicChannel writes these messages byte by byte, so any change to their format needs a new channel name.
public sealed interface MusicPayload extends CustomPacketPayload {

    CustomPacketPayload.Type<MusicPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("katzegames", "music"));
    StreamCodec<FriendlyByteBuf, MusicPayload> CODEC = StreamCodec.of(MusicPayload::write, MusicPayload::read);

    int PLAY = 0;
    int FADE = 1;
    int STOP = 2;

    record Play(Identifier sound, boolean file) implements MusicPayload {
    }

    record Fade(int ticks) implements MusicPayload {
    }

    record Stop() implements MusicPayload {
    }

    @Override
    default CustomPacketPayload.Type<MusicPayload> type() {
        return TYPE;
    }

    private static MusicPayload read(FriendlyByteBuf buffer) {
        byte kind = buffer.readByte();
        return switch (kind) {
            case PLAY -> new Play(Identifier.parse(buffer.readUtf()), buffer.readBoolean());
            case FADE -> new Fade(buffer.readVarInt());
            case STOP -> new Stop();
            default -> throw new DecoderException("Unknown music message kind " + kind);
        };
    }

    private static void write(FriendlyByteBuf buffer, MusicPayload payload) {
        switch (payload) {
            case Play(Identifier sound, boolean file) -> {
                buffer.writeByte(PLAY);
                buffer.writeUtf(sound.toString());
                buffer.writeBoolean(file);
            }
            case Fade(int ticks) -> {
                buffer.writeByte(FADE);
                buffer.writeVarInt(ticks);
            }
            case Stop() -> buffer.writeByte(STOP);
        }
    }
}
