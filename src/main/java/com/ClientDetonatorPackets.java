package com;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.List;

// Called via DistExecutor from packet handlers.
public final class ClientDetonatorPackets {
    private ClientDetonatorPackets() {
    }

    public static void onPositions(List<BlockPos> positions) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof DetonatorScreen screen) {
            screen.applyPositions(positions);
        }
    }
}

