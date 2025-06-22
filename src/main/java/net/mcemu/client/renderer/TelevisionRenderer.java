package net.mcemu.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.mcemu.MCEmuMod;
import net.mcemu.block.TelevisionBlock;
import net.mcemu.block.entity.TelevisionEntity;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import org.joml.Matrix4f;
import org.joml.Matrix3f;
import net.minecraft.client.renderer.RenderType;

public class TelevisionRenderer implements BlockEntityRenderer<TelevisionEntity> {
    private static final ResourceLocation STATIC_SCREEN = new ResourceLocation(MCEmuMod.MOD_ID, "textures/block/screen_static.png");

    public TelevisionRenderer(BlockEntityRendererProvider.Context context) {}

    //@Override
    //public void render(TelevisionEntity entity, float partialTicks, PoseStack poseStack,
    //                   MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
    @Override
    public void render(TelevisionEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // Center and orient
        poseStack.translate(0.5, 0.5, 0.5);
        switch (entity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> {}
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(-90));
        }
        poseStack.translate(-0.5, -0.5, -0.5);

        // Screen quad (flat square inside the TV)
        //VertexConsumer builder = bufferSource.getBuffer(RenderType.entityTranslucent(STATIC_SCREEN));
        ResourceLocation frameTex = entity.getNesTexture();
        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityTranslucent(frameTex));
        //Option A for washed out colors
        //VertexConsumer builder = bufferSource.getBuffer(RenderType.text(frameTex));
        //Option B
        //VertexConsumer builder = bufferSource.getBuffer(RenderType.entitySolid(frameTex));
        //VertexConsumer builder = bufferSource.getBuffer(RenderType.text(entity.getNesTexture()));

        /*
        float x1 = 2f / 16f, x2 = 14f / 16f;
        float y1 = 4f / 16f, y2 = 12f / 16f;
        float z = 0.001f; // Near the front face

        builder.vertex(poseStack.last().pose(), x1, y1, z).color(255, 255, 255, 255).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(0, 0, 1).endVertex();
        builder.vertex(poseStack.last().pose(), x2, y1, z).color(255, 255, 255, 255).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(0, 0, 1).endVertex();
        builder.vertex(poseStack.last().pose(), x2, y2, z).color(255, 255, 255, 255).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(0, 0, 1).endVertex();
        builder.vertex(poseStack.last().pose(), x1, y2, z).color(255, 255, 255, 255).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(0, 0, 1).endVertex();
        */

        // inside render() method
        Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();
        Matrix3f normal = pose.normal();

        // screen corners (adjust if needed to match TV model)
        // Expand vertically from 0.0 to 1.0
        float minX = 0.0625f;
        float maxX = 0.9375f;
        float minY = 0.0f;
        float maxY = 1.0f;
        float z = 0.01f;  // slight offset to avoid z-fighting

        builder.vertex(mat, minX, maxY, z).color(255, 255, 255, 255).uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(normal, 0, 0, -1).endVertex();

        builder.vertex(mat, maxX, maxY, z).color(255, 255, 255, 255).uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(normal, 0, 0, -1).endVertex();

        builder.vertex(mat, maxX, minY, z).color(255, 255, 255, 255).uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(normal, 0, 0, -1).endVertex();

        builder.vertex(mat, minX, minY, z).color(255, 255, 255, 255).uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(normal, 0, 0, -1).endVertex();

        poseStack.popPose();
    }
}
