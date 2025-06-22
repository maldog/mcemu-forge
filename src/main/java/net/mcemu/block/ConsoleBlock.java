package net.mcemu.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;

import net.mcemu.item.CartridgeItem;
import net.minecraft.network.chat.Component;


import java.nio.file.Path;
import java.nio.file.Files;
import net.minecraftforge.fml.loading.FMLPaths;


import net.mcemu.block.ConsoleBlockEntity;

import javax.annotation.Nullable;

public class ConsoleBlock extends Block implements EntityBlock {

    public ConsoleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConsoleBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        System.out.println("➡️ use() called at " + pos + ", client=" + level.isClientSide);

        if (!level.isClientSide) {
            return InteractionResult.PASS; // skip entirely on server
        }


        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof CartridgeItem)) return InteractionResult.PASS;

        System.out.println("🧾 Item NBT: " + held.getTag());
        String romFileName = CartridgeItem.getRomFilename(held);
        System.out.println("🎮 ROM filename: " + romFileName);

        Path romPath = Path.of(FMLPaths.CONFIGDIR.get().toString(), "mcemu", "roms", "nes", romFileName);
        System.out.println("📁 Checking for path: " + romPath);
        System.out.println("📄 Exists? " + Files.exists(romPath));

        if (!Files.exists(romPath)) {
            player.displayClientMessage(Component.literal("ROM not found: " + romFileName), true);
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ConsoleBlockEntity console) {
            System.out.println("📀 Cartridge inserted: " + romFileName);
            console.insertCartridge(romPath);
            player.displayClientMessage(Component.literal("Loaded " + romFileName), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }





    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (!level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof ConsoleBlockEntity consoleBE) {
                    consoleBE.tickServer();
                }
            };
        }
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }


}
