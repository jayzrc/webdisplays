/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.PacketDistributor;
import net.montoyo.wd.core.DefaultPeripheral;
import net.montoyo.wd.entity.*;
import net.montoyo.wd.item.ItemLinker;
import net.montoyo.wd.net.WDNetworkRegistry;
import net.montoyo.wd.net.client_bound.S2CMessageCloseGui;
import org.jetbrains.annotations.Nullable;

public class BlockRedCTRL extends WDBlockContainer {

    public static final EnumProperty<DefaultPeripheral> type = BlockPeripheral.type;
    private static final Property<?>[] properties = new Property<?>[] {type};

    public BlockRedCTRL() {
        super(BlockBehaviour.Properties.of(Material.STONE).strength(1.5f, 10.f));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityRedCtrl(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(player.isShiftKeyDown())
            return InteractionResult.FAIL;

        if(player.getItemInHand(hand).getItem() instanceof ItemLinker)
            return InteractionResult.FAIL;

        BlockEntity te = world.getBlockEntity(pos);

        if(te instanceof TileEntityRedCtrl)
            return ((TileEntityRedCtrl) te).onRightClick(player, hand);
        else if(te instanceof TileEntityServer) {
            ((TileEntityServer) te).onPlayerRightClick(player);
            return InteractionResult.PASS;
        } else
            return InteractionResult.FAIL;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if(world.isClientSide)
            return;
        if(placer instanceof Player) {
            BlockEntity te = world.getBlockEntity(pos);

            if(te instanceof TileEntityServer)
                ((TileEntityServer) te).setOwner((Player) placer);
            else if(te instanceof TileEntityInterfaceBase)
                ((TileEntityInterfaceBase) te).setOwner((Player) placer);
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.IGNORE;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborType, BlockPos neighbor, boolean isMoving) {
        BlockEntity te = world.getBlockEntity(pos);
        if(te != null && te instanceof TileEntityPeripheralBase)
            ((TileEntityPeripheralBase) te).onNeighborChange(neighborType, neighbor);

        if(world.isClientSide)
            return;

        if(neighbor.getX() == pos.getX() && neighbor.getY() == pos.getY() - 1 && neighbor.getZ() == pos.getZ() && world.isEmptyBlock(neighbor)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            WDNetworkRegistry.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(world, pos)), new S2CMessageCloseGui(pos));
        }
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if(!world.isClientSide) {
            WDNetworkRegistry.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(world, pos)), new S2CMessageCloseGui(pos));
        }
        super.playerDestroy(world, player, pos, state, blockEntity, tool);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        playerDestroy(level, null, pos, level.getBlockState(pos), null, null);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if(!world.isClientSide) {
            double rpos = (entity.getY() - ((double) pos.getY())) * 16.0;

            if(rpos >= 1.0 && rpos <= 2.0 && Math.random() < 0.25) {
                BlockEntity te = world.getBlockEntity(pos);

                if(te != null && te instanceof TileEntityKeyboard)
                    ((TileEntityKeyboard) te).simulateCat(entity);
            }
        }
    }

    public static PacketDistributor.TargetPoint point(Level world, BlockPos bp) {
        return new PacketDistributor.TargetPoint(bp.getX(), bp.getY(), bp.getZ(), 64.0, world.dimension());
    }

}
