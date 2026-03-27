package com;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.function.Supplier;

public final class RemoveDetonatorPosC2SPacket {
    private final int index;

    public RemoveDetonatorPosC2SPacket(int index) {
        this.index = index;
    }

    public static void encode(RemoveDetonatorPosC2SPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.index);
    }

    public static RemoveDetonatorPosC2SPacket decode(FriendlyByteBuf buf) {
        return new RemoveDetonatorPosC2SPacket(buf.readVarInt());
    }

    public static void handle(RemoveDetonatorPosC2SPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) return;

            detonator det = getHeldDetonator(sender);
            if (det == null) return;

            ArrayList<BlockPos> list = det.getBlocks();
            int idx = msg.index;
            if (idx < 0 || idx >= list.size()) return;

            list.remove(idx);

            ModNet.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> sender),
                    new DetonatorPositionsS2CPacket(new ArrayList<>(list))
            );
        });
        ctx.setPacketHandled(true);
    }

    private static detonator getHeldDetonator(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof detonator d) return d;
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof detonator d) return d;
        return null;
    }
}

