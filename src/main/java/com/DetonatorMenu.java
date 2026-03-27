package com;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DetonatorMenu extends AbstractContainerMenu {
    private static final int MAX_SYNCED_POSITIONS = 200;

    private final ArrayList<BlockPos> positions;

    // Client constructor (data arrives from server via NetworkHooks.openScreen(..., buf -> ...))
    public DetonatorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, readPositions(data));
    }

    // Server constructor
    public DetonatorMenu(int containerId, Inventory playerInventory, List<BlockPos> positions) {
        super(detonators.DETONATOR_MENU.get(), containerId);
        this.positions = new ArrayList<>(positions);
    }

    public List<BlockPos> getPositions() {
        return positions;
    }

    public void setPositions(List<BlockPos> newPositions) {
        positions.clear();
        positions.addAll(newPositions);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public static void writePositions(FriendlyByteBuf buf, List<BlockPos> positions) {
        int count = Math.min(positions.size(), MAX_SYNCED_POSITIONS);
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            buf.writeBlockPos(positions.get(i));
        }
    }

    public static List<BlockPos> readPositions(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        int safeCount = Math.min(Math.max(count, 0), MAX_SYNCED_POSITIONS);
        ArrayList<BlockPos> out = new ArrayList<>(safeCount);
        for (int i = 0; i < safeCount; i++) {
            out.add(buf.readBlockPos());
        }
        return out;
    }
}
