/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.gui;

import com.cinemamod.mcef.MCEFBrowser;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.client.ClientProxy;
import net.montoyo.wd.utilities.data.BlockSide;
import org.cef.misc.CefCursorType;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

import static net.minecraftforge.api.distmarker.Dist.CLIENT;

@OnlyIn(CLIENT)
public class GuiMinePad extends WDScreen {
	
	private ClientProxy.PadData pad;
	private double vx;
	private double vy;
	private double vw;
	private double vh;
	
	public GuiMinePad() {
		super(Component.nullToEmpty(null));
	}
	
	public GuiMinePad(ClientProxy.PadData pad) {
		this();
		this.pad = pad;
	}
	
	int trueWidth, trueHeight;
	
	@Override
	public void init() {
		vw = ((double) width) - 32.0f;
		vh = vw / WebDisplays.PAD_RATIO;
		vx = 16.0f;
		vy = (((double) height) - vh) / 2.0f;
		
		trueWidth = width;
		trueHeight = height;
		
		this.width = (int) vw;
		this.height = (int) vh;
		
		super.init();
		
		((MCEFBrowser) pad.view).setCursor(CefCursorType.fromId(pad.activeCursor));
		((MCEFBrowser) pad.view).setCursorChangeListener((id) -> {
			pad.activeCursor = id;
			((MCEFBrowser) pad.view).setCursor(CefCursorType.fromId(id));
		});
	}
	
	private static void addRect(BufferBuilder bb, double x, double y, double w, double h) {
		bb.vertex(x, y, 0.0).color(255, 255, 255, 255).endVertex();
		bb.vertex(x + w, y, 0.0).color(255, 255, 255, 255).endVertex();
		bb.vertex(x + w, y + h, 0.0).color(255, 255, 255, 255).endVertex();
		bb.vertex(x, y + h, 0.0).color(255, 255, 255, 255).endVertex();
	}
	
	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float ptt) {
		width = trueWidth;
		height = trueHeight;
		renderBackground(graphics);
		width = (int) vw;
		height = (int) vh;
		
		RenderSystem.disableCull();
		RenderSystem.setShaderColor(0.73f, 0.73f, 0.73f, 1.0f);
		
		Tesselator t = Tesselator.getInstance();
		BufferBuilder bb = t.getBuilder();
		bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		addRect(bb, vx, vy - 16, vw, 16);
		addRect(bb, vx, vy + vh, vw, 16);
		addRect(bb, vx - 16, vy, 16, vh);
		addRect(bb, vx + vw, vy, 16, vh);
		t.end();
		
		if (pad.view != null) {
//            pad.view.draw(poseStack, vx, vy + vh, vx + vw, vy);
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			RenderSystem.disableDepthTest();
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
			RenderSystem.setShaderTexture(0, ((MCEFBrowser) pad.view).getRenderer().getTextureID());
			t = Tesselator.getInstance();
			BufferBuilder buffer = t.getBuilder();
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			double x1 = vx;
			double y1 = vy;
			double x2 = vx + vw;
			double y2 = vy + vh;
			buffer.vertex(graphics.pose().last().pose(), (float) x1, (float) y1, 0.0f).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
			buffer.vertex(graphics.pose().last().pose(), (float) x2, (float) y1, 0.0f).uv(1.0F, 0.0F).color(255, 255, 255, 255).endVertex();
			buffer.vertex(graphics.pose().last().pose(), (float) x2, (float) y2, 0.0f).uv(1.0F, 1.0F).color(255, 255, 255, 255).endVertex();
			buffer.vertex(graphics.pose().last().pose(), (float) x1, (float) y2, 0.0f).uv(0.0F, 1.0F).color(255, 255, 255, 255).endVertex();
			t.end();
			RenderSystem.enableDepthTest();
		}
		
		RenderSystem.enableCull();
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return this.keyChanged(keyCode, scanCode, modifiers, true) || super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return this.keyChanged(keyCode, scanCode, modifiers, false) || super.keyReleased(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (pad.view != null) {
			((MCEFBrowser) pad.view).sendKeyTyped(codePoint, modifiers);
			return true;
		} else {
			return super.charTyped(codePoint, modifiers);
		}
	}
	
	/* copied from MCEF */
	public boolean keyChanged(int keyCode, int scanCode, int modifiers, boolean pressed) {
		assert minecraft != null;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			minecraft.setScreen(null);
			return true;
		}
		
		InputConstants.Key iuKey = InputConstants.getKey(keyCode, scanCode);
		String keystr = iuKey.getDisplayName().getString();
//        System.out.println("KEY STR " + keystr);
		if (keystr.length() == 0)
			return false;
		
		char key = keystr.charAt(keystr.length() - 1);
		
		if (keystr.equals("Enter")) {
			keyCode = 10;
			key = '\n';
		}
		
		if (pad.view != null) {
			if (pressed)
				((MCEFBrowser) pad.view).sendKeyPress(keyCode, scanCode, modifiers);
			else
				((MCEFBrowser) pad.view).sendKeyRelease(keyCode, scanCode, modifiers);
			
			if (pressed && key == '\n')
				if (modifiers != 0) ((MCEFBrowser) pad.view).sendKeyTyped('\r', modifiers);
			return true;
		}
		
		return false;
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		super.mouseMoved(mouseX, mouseY);
		mouse(-1, false, (int) mouseX, (int) mouseY, 0);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		mouse(button, true, (int) mouseX, (int) mouseY, 0);
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		mouse(button, false, (int) mouseX, (int) mouseY, 0);
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		double mx = (mouseX - vx) / vw;
		double my = (mouseY - vy) / vh;
		int sx = (int) (mx * WebDisplays.INSTANCE.padResX);
		int sy = (int) (my * WebDisplays.INSTANCE.padResY);
		// TODO: this doesn't work, and I don't understand why?
		((MCEFBrowser) pad.view).sendMouseWheel(sx, sy, amount, (hasControlDown() && !hasAltDown() && !hasShiftDown()) ? GLFW.GLFW_MOD_CONTROL : 0);
		
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	public void mouse(int btn, boolean pressed, int sx, int sy, double scrollAmount) {
		double mx = (sx - vx) / vw;
		double my = (sy - vy) / vh;
		
		if (pad.view != null && mx >= 0 && mx <= 1) {
			//Scale again according to the webview
			sx = (int) (mx * WebDisplays.INSTANCE.padResX);
			sy = (int) (my * WebDisplays.INSTANCE.padResY);
			
			if (btn == -1)
				((MCEFBrowser) pad.view).sendMouseMove(sx, sy);
			else if (pressed)
				((MCEFBrowser) pad.view).sendMousePress(sx, sy, btn);
			else
				((MCEFBrowser) pad.view).sendMouseRelease(sx, sy, btn);
			pad.view.setFocus(true);
		}
	}
	
	public static Optional<Character> getChar(int keyCode, int scanCode) {
		String keystr = GLFW.glfwGetKeyName(keyCode, scanCode);
		if (keystr == null) {
			keystr = "\0";
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			keystr = "\n";
		}
		if (keystr.length() == 0) {
			return Optional.empty();
		}
		
		return Optional.of(keystr.charAt(keystr.length() - 1));
	}
	
	@Override
	public void tick() {
		if (pad.view == null)
			minecraft.setScreen(null); //In case the user dies with the pad in the hand
	}
	
	@Override
	public boolean isForBlock(BlockPos bp, BlockSide side) {
		return false;
	}
	
	@Override
	public void removed() {
		super.removed();
		if (pad.view instanceof MCEFBrowser browser) {
			browser.setCursor(CefCursorType.POINTER);
			browser.setCursorChangeListener((cursor) -> {
				pad.activeCursor = cursor;
			});
		}
	}
	
	@Override
	public void onClose() {
		super.onClose();
		removed();
	}
}
