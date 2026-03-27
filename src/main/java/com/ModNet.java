package com;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNet {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(detonators.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private ModNet() {
    }

    public static void init() {
        int id = 0;

        CHANNEL.messageBuilder(RemoveDetonatorPosC2SPacket.class, id++)
                .encoder(RemoveDetonatorPosC2SPacket::encode)
                .decoder(RemoveDetonatorPosC2SPacket::decode)
                .consumerMainThread(RemoveDetonatorPosC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(DetonateAllC2SPacket.class, id++)
                .encoder(DetonateAllC2SPacket::encode)
                .decoder(DetonateAllC2SPacket::decode)
                .consumerMainThread(DetonateAllC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(DetonatorPositionsS2CPacket.class, id++)
                .encoder(DetonatorPositionsS2CPacket::encode)
                .decoder(DetonatorPositionsS2CPacket::decode)
                .consumerMainThread(DetonatorPositionsS2CPacket::handle)
                .add();
    }
}
