/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LaserPointerRenderer implements IItemRenderer {
    private final Tesselator t = Tesselator.getInstance();
    private final BufferBuilder bb = t.getBuilder();
    public boolean isOn = false;

    public LaserPointerRenderer() {}

    @Override
    public boolean render(PoseStack poseStack, ItemStack is, float handSideSign, float swingProgress, float equipProgress, MultiBufferSource multiBufferSource, int packedLight) {
        RenderSystem.disableCull();
        RenderSystem.disableTexture();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        float PI = (float) Math.PI;

        float sqrtSwingProg = (float) Math.sqrt(swingProgress);
        float sinSqrtSwingProg1 = (float) Math.sin(sqrtSwingProg * PI);
    
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        var matrix0 = poseStack.last().pose();
        //Laser pointer
        poseStack.pushPose();
        poseStack.translate(handSideSign * -0.4f * sinSqrtSwingProg1, (float) (0.2f * Math.sin(sqrtSwingProg * PI * 2.0f)), (float) (-0.2f * Math.sin(swingProgress * PI)));
        poseStack.translate(handSideSign * 0.56f, -0.52f - equipProgress * 0.6f, -0.72f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees((float) (handSideSign * (45.0f - Math.sin(swingProgress * swingProgress * PI) * 20.0f))));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(handSideSign * sinSqrtSwingProg1 * -20.0f));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(sinSqrtSwingProg1 * -80.0f));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(handSideSign * -30.0f));
        poseStack.translate(0.0f, 0.2f, 0.0f);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(10.0f));
        poseStack.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        var matrix = poseStack.last().pose();

        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        bb.vertex(matrix, 0.0f, 0.0f, 0.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 1.0f, 0.0f, 0.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 1.0f, 0.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 0.0f, 0.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();

        bb.vertex(matrix, 0.0f, 0.0f, 0.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 0.0f, -1.0f, 0.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 0.0f, -1.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 0.0f, 0.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();

        bb.vertex(matrix,1.0f, 0.0f, 0.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix,1.0f, -1.0f, 0.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix,1.0f, -1.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix,1.0f, 0.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();

        bb.vertex(matrix, 0.0f, -1.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 1.0f, -1.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 1.0f, 0.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 0.0f, 0.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        
        if (isOn) {
            drawLineBetween(bb, matrix0, matrix, new Vec3(0.5f, -0.5f, 0.5f), new Vec3(-40.0f, 4000.5f, -100.0f));
        }
        
        t.end();

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.enableTexture(); //Fix for shitty minecraft fire
        RenderSystem.enableCull();
        poseStack.popPose();
        
        return true;
    }

    private static void drawLineBetween(BufferBuilder bb, Matrix4f matrix0, Matrix4f matrix, Vec3 local, Vec3 target)
    {
        //Calculate distance between points -> length of the line
        float distance = (float) local.distanceTo(target) / 2;
        float quarterWidth = 0.25f;
        float biggerWidth = 10;

        bb.vertex(matrix, 0.25f, -0.25f, 0.5f).color((float) 0.5, (float) 0.0, (float) 0.0, (float) 1.0).endVertex();
        bb.vertex(matrix, quarterWidth + 0.25f, -0.25f, 0.5f).color((float) 0.5, (float) 0.0, (float) 0.0, (float) 1.0).endVertex();
        bb.vertex(matrix0, biggerWidth - 6f, 3f, - distance).color((float) 0.5, (float) 0.0, (float) 0.0, (float) 1.0).endVertex();
        bb.vertex(matrix0, -6f, 3f, -distance).color((float) 0.5, (float) 0.0, (float) 0.0, (float) 1.0).endVertex();

        bb.vertex(matrix, 0.25f, -0.25f, 0.5f).color((float) 0.5, (float) 0.0, (float) 0.0, (float) 1.0).endVertex();
        bb.vertex(matrix, 0.25f, -quarterWidth - 0.25f, 0.5f).color((float) 0.5, (float) 0.0, (float) 0.0, (float) 1.0).endVertex();
        bb.vertex(matrix0, -6f, -biggerWidth + 3f, -distance).color((float) 0.5, (float) 0.0, (float) 0.0, (float) 1.0).endVertex();
        bb.vertex(matrix0, -6f, 3f, -distance).color((float) 0.5, (float) 0.0, (float) 0.0, (float) 1.0).endVertex();

        bb.vertex(matrix,quarterWidth + 0.25f, -0.25f, 0.5f).color((float) 0.5, (float) 0.0, (float) 0.0, (float) 1.0).endVertex();
        bb.vertex(matrix,quarterWidth + 0.25f, -quarterWidth - 0.25f, 0.5f).color((float) 0.5, (float) 0.0, (float) 0.0, (float) 1.0).endVertex();
        bb.vertex(matrix0,biggerWidth - 6f, -biggerWidth + 3f, -distance).color((float) 0.5, (float) 0.0, (float) 0.0, (float) 1.0).endVertex();
        bb.vertex(matrix0,biggerWidth - 6f, 3f, -distance).color((float) 0.5, (float) 0.0, (float) 0.0, (float) 1.0).endVertex();
    }
}
