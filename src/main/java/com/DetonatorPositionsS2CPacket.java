package com;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public final class DetonatorPositionsS2CPacket {
    private final List<BlockPos> positions;

    public DetonatorPositionsS2CPacket(List<BlockPos> positions) {
        this.positions = positions;
    }

    public static void encode(DetonatorPositionsS2CPacket msg, FriendlyByteBuf buf) {
        DetonatorMenu.writePositions(buf, msg.positions);
    }

    public static DetonatorPositionsS2CPacket decode(FriendlyByteBuf buf) {
        return new DetonatorPositionsS2CPacket(DetonatorMenu.readPositions(buf));
    }

    public static void handle(DetonatorPositionsS2CPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientDetonatorPackets.onPositions(msg.positions)));
        ctx.setPacketHandled(true);
    }
}

