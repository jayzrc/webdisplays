/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ModelMinePad {
	public void render(MultiBufferSource buffers, PoseStack stack) {
		// TODO: this needs completing
		// TODO: I'd like this to be able to load a model from a JSON if possible
		
		double x1 = 1.0;
		double y1 = 0.0;
		double x2 = 27.65 / 32.0 + 0.01;
		double y2 = 14.0 / 32.0 + 0.002;
		
		Matrix4f positionMatrix = stack.last().pose();
		Tesselator t = Tesselator.getInstance();
		BufferBuilder vb = t.getBuilder();
		
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		vb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		vb.vertex(positionMatrix, (float) x1, (float) y1, 0.0f).color(0, 0, 0, 255).endVertex();
		vb.vertex(positionMatrix, (float) x2, (float) y1, 0.0f).color(0, 0, 0, 255).endVertex();
		vb.vertex(positionMatrix, (float) x2, (float) y2, 0.0f).color(0, 0, 0, 255).endVertex();
		vb.vertex(positionMatrix, (float) x1, (float) y2, 0.0f).color(0, 0, 0, 255).endVertex();
		t.end();
	}
}
