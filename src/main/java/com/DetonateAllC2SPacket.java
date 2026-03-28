package com;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.ArrayList;

public final class DetonateAllC2SPacket {
    public DetonateAllC2SPacket() {
    }

    public static void encode(DetonateAllC2SPacket msg, FriendlyByteBuf buf) {
        // no payload
    }

    public static DetonateAllC2SPacket decode(FriendlyByteBuf buf) {
        return new DetonateAllC2SPacket();
    }

    public static void handle(DetonateAllC2SPacket msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) return;

            detonator det = getHeldDetonator(sender);
            if (det == null) return;

            ServerLevel level = sender.serverLevel();
            ArrayList<BlockPos> list = det.getBlocks();
            if (list.isEmpty()) return;

            // Copy first so explosions don't interact badly with list mutation, then clear.
            ArrayList<BlockPos> toDetonate = new ArrayList<>(list);
            list.clear();

            for (BlockPos pos : toDetonate) {
                if (!level.hasChunkAt(pos)) continue;
                level.explode(sender, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.TNT);
            }

            // Refresh the client list to empty.
            ModNet.CHANNEL.send(new DetonatorPositionsS2CPacket(new ArrayList<>(list)), PacketDistributor.PLAYER.with(sender));
        });
        ctx.setPacketHandled(true);
    }

    private static detonator getHeldDetonator(ServerPlayer player) {
        if (player.getMainHandItem().getItem() instanceof detonator d) return d;
        if (player.getOffhandItem().getItem() instanceof detonator d) return d;
        return null;
    }
}
