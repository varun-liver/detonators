package com;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public final class ModNet {
    private static final int PROTOCOL_VERSION = 1;

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(detonators.MODID, "main"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .simpleChannel();

    private ModNet() {
    }

    public static void init() {
        int id = 0;

        CHANNEL.messageBuilder(RemoveDetonatorPosC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RemoveDetonatorPosC2SPacket::encode)
                .decoder(RemoveDetonatorPosC2SPacket::decode)
                .consumerMainThread(RemoveDetonatorPosC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(DetonateAllC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DetonateAllC2SPacket::encode)
                .decoder(DetonateAllC2SPacket::decode)
                .consumerMainThread(DetonateAllC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(DetonatorPositionsS2CPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DetonatorPositionsS2CPacket::encode)
                .decoder(DetonatorPositionsS2CPacket::decode)
                .consumerMainThread(DetonatorPositionsS2CPacket::handle)
                .add();
    }
}
