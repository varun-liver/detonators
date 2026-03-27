package com;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;

@Mod.EventBusSubscriber(modid = detonators.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DetonatorForgeEvents {
    private DetonatorForgeEvents() {
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide) return;

        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof detonator det)) return;

        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(
                        serverPlayer,
                        new SimpleMenuProvider((id, inv, p) -> new DetonatorMenu(id, inv, det.getBlocks()), Component.literal("Detonator")),
                        buf -> DetonatorMenu.writePositions(buf, det.getBlocks())
                );
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        Entity target = event.getTarget();
        if (!(target instanceof MinecartTNT) && !(target instanceof EndCrystal)) return;

        BlockPos pos = target.blockPosition().immutable();
        det.getBlocks().add(pos);
        player.displayClientMessage(Component.literal("Detonator stored entity: " + pos.toShortString()), true);

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}

