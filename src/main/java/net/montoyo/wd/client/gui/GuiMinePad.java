/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.client.ClientProxy;
import net.montoyo.wd.utilities.BlockSide;
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

    @Override
    public void init() {
        vw = ((double) width) - 32.0f;
        vh = vw / WebDisplays.PAD_RATIO;
        vx = 16.0f;
        vy = (((double) height) - vh) / 2.0f;
        
        this.width = (int) vw;
        this.height = (int) vh;
        
        super.init();
    }

    private static void addRect(BufferBuilder bb, double x, double y, double w, double h) {
        bb.vertex(x, y, 0.0).color(255, 255, 255, 255).endVertex();
        bb.vertex(x + w, y, 0.0).color(255, 255, 255, 255).endVertex();
        bb.vertex(x + w, y + h, 0.0).color(255, 255, 255, 255).endVertex();
        bb.vertex(x, y + h, 0.0).color(255, 255, 255, 255).endVertex();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float ptt) {
        renderBackground(poseStack);

        RenderSystem.disableTexture();
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

        RenderSystem.enableTexture();

        if (pad.view != null) {
            pad.view.draw(poseStack, vx, vy + vh, vx + vw, vy);
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
            pad.view.injectKeyTyped((int) codePoint, modifiers);
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
        
        if(keystr.equals("Enter"))
            key = '\r';
        
        if (pad.view != null) { //Inject events into browser
//            System.out.println("Sent keystroke " + keystr);
            if (pressed)
                pad.view.injectKeyPressedByKeyCode(keyCode, key, modifiers);
            else
                pad.view.injectKeyReleasedByKeyCode(keyCode, key, modifiers);
            
            if(key == '\r')
                pad.view.injectKeyTyped(key, 0);
            return true; // Something did happen
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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        mouse(button, true, (int) mouseX, (int) mouseY, 0);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        mouse(-1, false, (int) mouseX, (int) mouseY, amount);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    public void mouse(int btn, boolean pressed, int sx, int sy, double scrollAmount) {
        double mx = (sx - vx) / vw;
        double my = (sy - vy) / vh;

        if (pad.view != null && mx >= 0 && mx <= 1) {
            int wheel = (int) scrollAmount;

            //Scale again according to the webview
            sx = (int) (mx * WebDisplays.INSTANCE.padResX);
            sy = (int) (my * WebDisplays.INSTANCE.padResY);
    
            if (wheel != 0)
                pad.view.injectMouseWheel(sx, sy, (hasControlDown() && ! hasAltDown() && !hasShiftDown()) ? GLFW.GLFW_MOD_CONTROL : 0, wheel, 0);
            else if (btn == -1)
                pad.view.injectMouseMove(sx, sy, 0, sy < 0);
            else
                pad.view.injectMouseButton(sx, sy, 0, btn + 1, pressed, 1);
        }
    }

    public static Optional<Character> getChar(int keyCode, int scanCode) {
        String keystr = GLFW.glfwGetKeyName(keyCode, scanCode);
        if(keystr == null){
            keystr = "\0";
        }
        if(keyCode == GLFW.GLFW_KEY_ENTER){
            keystr = "\n";
        }
        if(keystr.length() == 0){
            return Optional.empty();
        }

        return Optional.of(keystr.charAt(keystr.length() - 1));
    }

    @Override
    public void tick() {
        if(pad.view == null)
            minecraft.setScreen(null); //In case the user dies with the pad in the hand
    }

    @Override
    public boolean isForBlock(BlockPos bp, BlockSide side) {
        return false;
    }

}
