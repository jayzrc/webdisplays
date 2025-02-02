/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.gui.controls;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.montoyo.wd.client.gui.loading.JsonOWrapper;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class Icon extends BasicControl {

    protected int width;
    protected int height;
    protected double u1;
    protected double v1;
    protected double u2;
    protected double v2;
    protected ResourceLocation texture;

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void load(JsonOWrapper json) {
        super.load(json);

        width = json.getInt("width", 16);
        height = json.getInt("height", 16);
        u1 = json.getDouble("u1", 0.0);
        v1 = json.getDouble("v1", 0.0);
        u2 = json.getDouble("u2", 1.0);
        v2 = json.getDouble("v2", 1.0);
        texture = new ResourceLocation(json.getString("resourceLocation", ""));
    }

    @Override
    public void draw(GuiGraphics poseStack, int mouseX, int mouseY, float ptt) {
        if(texture != null) {
            poseStack.pose().pushPose();
//            RenderSystem.enableTexture();
            RenderSystem.setShaderTexture(1, texture);
            RenderSystem.bindTexture(1);
            RenderSystem.enableBlend();
            fillTexturedRect(poseStack.pose(), x, y, width, height, u1, v1, u2, v2);
            RenderSystem.disableBlend();
            RenderSystem.bindTexture(-1);
            poseStack.pose().popPose();
        }
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setTextureCoordinates(double u1, double v1, double u2, double v2) {
        this.u1 = u1;
        this.v1 = v1;
        this.u2 = u2;
        this.v2 = v2;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public double getU1() {
        return u1;
    }

    public double getV1() {
        return v1;
    }

    public double getU2() {
        return u2;
    }

    public double getV2() {
        return v2;
    }

}
