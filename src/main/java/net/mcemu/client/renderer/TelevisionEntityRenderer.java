package net.mcemu.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mcemu.block.entity.TelevisionEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.client.renderer.GameRenderer;

public class TelevisionEntityRenderer implements BlockEntityRenderer<TelevisionEntity> {

    public TelevisionEntityRenderer(BlockEntityRendererProvider.Context context) {
        // Use context for rendering hooks if needed
    }

    @Override
    public void render(TelevisionEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        // 🔹 Start emulator if not running
        if (entity.emulator == null) {
            java.io.File rom = new java.io.File("config/mcemu/roms/smb.nes"); // TODO: replace with actual cartridge logic
            entity.startEmulator(rom);
        }

        Direction dir = entity.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
        float rotationY = switch (dir) {
            case SOUTH -> 180f;
            case WEST -> 90f;
            case EAST -> -90f;
            default -> 0f;
        };

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationY));
        poseStack.translate(-0.5, -0.5, -0.5);

        // TODO: Draw NES framebuffer or static image
        ResourceLocation screenTex = entity.getNesTexture();
        if (screenTex != null) {
            RenderSystem.setShaderTexture(0, screenTex);
            //RenderSystem.setShader(RenderSystem::getPositionTexShader);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            float pixelW = 1.0F / 256.0F;
            float pixelH = 1.0F / 240.0F;

            float x1 = 0.1875f; // inset the screen within the block face
            float x2 = 0.8125f;
            float y1 = 0.125f;
            float y2 = 0.75f;
            float z = 0.001f; // slight depth to prevent Z-fighting

            Tesselator tess = Tesselator.getInstance();
            BufferBuilder buffer = tess.getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            buffer.vertex(poseStack.last().pose(), x1, y2, z).uv(0, 1).endVertex();
            buffer.vertex(poseStack.last().pose(), x2, y2, z).uv(1, 1).endVertex();
            buffer.vertex(poseStack.last().pose(), x2, y1, z).uv(1, 0).endVertex();
            buffer.vertex(poseStack.last().pose(), x1, y1, z).uv(0, 0).endVertex();

            tess.end();
        }

        poseStack.popPose();
    }

}
