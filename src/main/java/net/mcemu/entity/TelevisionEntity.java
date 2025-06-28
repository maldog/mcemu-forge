package net.mcemu.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.mcemu.ModRegistry;
import net.mcemu.model.MCEmu;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class TelevisionEntity extends BlockEntity {

    private Direction facing;

    @OnlyIn(Dist.CLIENT)
    private MCEmu emulator;

    public TelevisionEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.TELEVISION_BLOCK_ENTITY.get(), pos, state);
        this.facing = state.getValue(HORIZONTAL_FACING);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public Direction getHorizontalFacing() {
        return facing;
    }

    @OnlyIn(Dist.CLIENT)
    private DynamicTexture nesTexture;
    @OnlyIn(Dist.CLIENT)
    private ResourceLocation nesTextureId;

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getNesTexture() {
        if (nesTexture == null) {
            nesTexture = new DynamicTexture(256, 240, false);
            nesTextureId = new ResourceLocation("mcemu", "nes_" + getBlockPos().asLong());

            System.out.println("Registering NES texture: " + nesTextureId);

            Minecraft.getInstance().getTextureManager().register(nesTextureId, nesTexture);
        }

        return nesTextureId;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (emulator != null) {
            emulator.stop(); // Stop the NES
            emulator = null;
        }
    }


    @OnlyIn(Dist.CLIENT)
    public void updateFrame(int[] argbPixels) {
        //System.out.println("Updating NES frame at " + this.getBlockPos());
        if (nesTexture == null) return;
        NativeImage img = nesTexture.getPixels();
//        for (int y = 0; y < 240; y++) {
//            for (int x = 0; x < 256; x++) {
//                img.setPixelRGBA(x, y, argbPixels[y * 256 + x]);
//            }
//        }
        for (int y = 0; y < 240; y++) {
            for (int x = 0; x < 256; x++) {
                int argb = argbPixels[y * 256 + x];

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int opaque = (0xFF << 24) | (r << 16) | (g << 8) | b;
                img.setPixelRGBA(x, y, opaque);
            }
        }

        nesTexture.upload();
    }
}
