package com;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;

public class detonator extends Item{
    // The Item instance is effectively a singleton per registry entry, so this behaves like "global" memory.
    private final ArrayList<BlockPos> blocks = new ArrayList<>();
    public detonator(Item.Properties properties) {
        super(properties);
    }

    public ArrayList<BlockPos> getBlocks() {
        return blocks;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (!level.isClientSide && player != null && player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) -> new DetonatorMenu(id, inv), Component.literal("Detonator")));
                ModNet.CHANNEL.send(new DetonatorPositionsS2CPacket(new ArrayList<>(blocks)), PacketDistributor.PLAYER.with(serverPlayer));
            }
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide) {
            BlockPos pos = context.getClickedPos().immutable();
            if (level.getBlockState(pos).is(Blocks.TNT)) {
                blocks.add(pos);
                if (player != null) {
                    player.displayClientMessage(Component.literal("Detonator stored TNT: " + pos.toShortString()), true);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) -> new DetonatorMenu(id, inv), Component.literal("Detonator")));
                ModNet.CHANNEL.send(new DetonatorPositionsS2CPacket(new ArrayList<>(blocks)), PacketDistributor.PLAYER.with(serverPlayer));
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
        }

        if (!level.isClientSide) {
            EntityHitResult entityHit = rayTraceAllowedEntity(level, player, 6.0);
            if (entityHit != null) {
                Entity e = entityHit.getEntity();
                BlockPos pos = e.blockPosition().immutable();
                blocks.add(pos);
                player.displayClientMessage(Component.literal("Detonator stored entity: " + pos.toShortString()), true);
            } else {
                BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = hit.getBlockPos().immutable();
                    if (level.getBlockState(pos).is(Blocks.TNT)) {
                        blocks.add(pos);
                        player.displayClientMessage(Component.literal("Detonator stored TNT: " + pos.toShortString()), true);
                    }
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    private static EntityHitResult rayTraceAllowedEntity(Level level, Player player, double range) {
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = start.add(look.scale(range));
        AABB bounds = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);

        return ProjectileUtil.getEntityHitResult(
                level,
                player,
                start,
                end,
                bounds,
                e -> !e.isSpectator() && e.isPickable() && (e instanceof MinecartTNT || e instanceof EndCrystal)
        );
    }
}
