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

public class TelevisionEntityRenderer implements BlockEntityRenderer<TelevisionEntity> {

    public TelevisionEntityRenderer(BlockEntityRendererProvider.Context context) {
        // Use context for rendering hooks if needed
    }

    @Override
    public void render(TelevisionEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

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
        // For now, this is just a placeholder
        poseStack.popPose();
    }
}
