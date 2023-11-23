package net.montoyo.wd.client.gui.camera;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.montoyo.wd.client.js.WDRouter;
import net.montoyo.wd.config.CommonConfig;
import net.montoyo.wd.entity.ScreenBlockEntity;
import net.montoyo.wd.entity.ScreenData;
import net.montoyo.wd.utilities.data.BlockSide;
import net.montoyo.wd.utilities.data.Rotation;

public class KeyboardCamera {
    private static ScreenBlockEntity tes;
    private static BlockSide side;

    private static double oxCrd = -1;
    private static double xCrd = -1;
    private static double nxCrd = -1;
    private static double oyCrd = -1;
    private static double yCrd = -1;
    private static double nyCrd = -1;

    private static double nextX = -1;
    private static double nextY = -1;

    protected static Vec2 pxToHit(ScreenData scr, Vec2 dst) {
        float cx, cy;
        if (scr.rotation.isVertical) {
            cy = dst.x;
            cx = dst.y;
        } else {
            cx = dst.x;
            cy = dst.y;
        }

        cx /= (float) scr.resolution.x;
        cy /= (float) scr.resolution.y;

        switch (scr.rotation) {
            case ROT_270:
                cx = 1.0f - cx;
                break;

            case ROT_180:
                cx = 1.0f - cx;
                cy = 1.0f - cy;
                break;

            case ROT_90:
                cy = 1.0f - cy;
                break;
        }

        if (side != BlockSide.BOTTOM)
            cy = 1.0f - cy;

        float swInverse = (((float) scr.size.x) - 4.f / 16.f);
        float shInverse = (((float) scr.size.y) - 4.f / 16.f);

        cx *= swInverse;
        cy *= shInverse;

        if(side.right.x > 0 || side.right.z > 0)
            cx += 1.f;

        if(side == BlockSide.TOP || side == BlockSide.BOTTOM)
            cy -= 1.f;

        return new Vec2(cx + (2 / 16f), cy + (2 / 16f));
    }

    protected static void updateCrd(JsonObject obj) {
        if (obj.getAsJsonPrimitive("exists").getAsBoolean()) {
            ScreenData scr = tes.getScreen(side);
            if (scr != null) {
                nextX = obj.getAsJsonPrimitive("x").getAsDouble() + obj.getAsJsonPrimitive("w").getAsDouble() / 2;
                nextY = obj.getAsJsonPrimitive("y").getAsDouble() + obj.getAsJsonPrimitive("h").getAsDouble() / 2;

                Vec2 c = pxToHit(scr, new Vec2((float) nextX, (float) nextY));

                nextX = c.x;
                nextY = c.y;

//                if (side.right.x > 0)
//                    nextX += 1.f;
//                if (side.right.z > 0)
//                    nextX += 1.f;
//                if (side == BlockSide.BOTTOM)
//                    nextY -= 1.f;
            }
        } else {
            nextX = -1;
            nextY = -1;
        }
    }

    private static WDRouter.Task<JsonObject> activeTask;
    private static long futureStart = 0;

    protected static void pollElement() {
        if (activeTask != null) return;

        ScreenBlockEntity teTmp = tes;
        BlockSide sdTmp = side;

        // async nonsense can occur here
        if (teTmp == null || sdTmp == null) return;

        ScreenData scr = teTmp.getScreen(sdTmp);
        if (scr != null) {
//@formatter:off
activeTask = WDRouter.INSTANCE.requestJson(scr.browser, "ActiveElement", """
try {
    let focusedElement = document.activeElement;
    if (focusedElement == null || focusedElement == document.body) {
        window.cefQuery({
          request: 'WebDisplays_ActiveElement{exists: false}',
          onSuccess: function(response) {},
          onFailure: function(error_code, error_message) {}
        });
    } else {
        let bodyRect = document.body.getBoundingClientRect();
        let elemRect = focusedElement.getBoundingClientRect();
        
        window.cefQuery({
          request: 'WebDisplays_ActiveElement{exists: true,'+
            'x: ' + (elemRect.left) + ',' +
            'y: ' + (elemRect.top) + ',' +
            'w: ' + ((elemRect.right - elemRect.left)) + ',' +
            'h: ' + ((elemRect.bottom - elemRect.top)) +
          '}',
          onSuccess: function(response) {},
          onFailure: function(error_code, error_message) {}
        });
    }
} catch (err) {
    console.error(err);
    window.cefQuery({
      request: 'WebDisplays_ActiveElement{exists: false}',
      onSuccess: function(response) {},
      onFailure: function(error_code, error_message) {}
    });
}""".replace("\n", "")
).thenAccept((o1) -> {
    updateCrd(o1);
    activeTask = null;
});
futureStart = System.currentTimeMillis();
//@formatter:on
        }
    }

    protected static double signedSqrt(double v) {
        double sv = Math.signum(v);
        v *= sv;
        return Math.sqrt(v) * sv;
    }

    public static void updateCamera(ViewportEvent.ComputeCameraAngles event) {
        if (futureStart != 0) {
            if (futureStart < System.currentTimeMillis() - 5000 || tes == null) {
                WDRouter.Task<?> active = activeTask;
                if (active != null) {
                    active.cancel();
                    activeTask = null;
                    futureStart = 0;
                }
            }
        }

        if (tes == null) {
            xCrd = -1;
            yCrd = -1;
            return; // nothing to do
        }

        if (xCrd == -1) return;
        if (yCrd == -1) return;

        // TODO: implement

        double coxCrd = Mth.lerp(0.5 * event.getPartialTick(), oxCrd, xCrd);
        double coyCrd = Mth.lerp(0.5 * event.getPartialTick(), oyCrd, yCrd);

        double focalX = tes.getBlockPos().getX() +
                side.right.x * (coxCrd - 1) + side.up.x * coyCrd + Math.abs(side.forward.x) * 0.5;
        double focalY = tes.getBlockPos().getY() +
                side.right.y * (coxCrd - 1) + side.up.y * coyCrd + Math.abs(side.forward.y) * 0.5;
        double focalZ = tes.getBlockPos().getZ() +
                side.right.z * (coxCrd - 1) + side.up.z * coyCrd + Math.abs(side.forward.z) * 0.5;

        focalX += side.forward.x * 0.5f;
        focalY += side.forward.y * 0.5f;
        focalZ += side.forward.z * 0.5f;

        float[] angle = lookAt(
                event.getCamera().getEntity(),
                EntityAnchorArgument.Anchor.EYES,
                new Vec3(focalX, focalY, focalZ)
        );

        float scl = 20;

        double mx = Minecraft.getInstance().mouseHandler.xpos();
        mx /= Minecraft.getInstance().getWindow().getWidth();

        double my = Minecraft.getInstance().mouseHandler.ypos();
        my /= Minecraft.getInstance().getWindow().getHeight();

        Vec2 v2 = new Vec2((float) mx, (float) my);
        v2 = v2.normalized().scale(Mth.sqrt(v2.length()));

//        angle[1] += (v2.x - 0.5f) * scl;
//        angle[0] += (v2.y - 0.5f) * scl;

        float xRot = event.getYaw(); // left right
        float yRot = event.getPitch(); // up down

        event.setYaw(angle[1]);
        event.setPitch(angle[0]);
    }

    public static void focus(ScreenBlockEntity screen, BlockSide side) {
        KeyboardCamera.tes = screen;
        KeyboardCamera.side = side;
    }

    public static float[] lookAt(Entity entity, EntityAnchorArgument.Anchor pAnchor, Vec3 pTarget) {
        Vec3 vec3 = pAnchor.apply(entity);
        double d0 = pTarget.x - vec3.x;
        double d1 = pTarget.y - vec3.y;
        double d2 = pTarget.z - vec3.z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        float xr = (Mth.wrapDegrees((float) (-(Mth.atan2(d1, d3) * (double) (180F / (float) Math.PI)))));
        float yr = (Mth.wrapDegrees((float) (Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F));
        return new float[]{xr, yr};
    }

    protected static int delay = 8;

    public static void gameTick(TickEvent.ClientTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.END)) {
            if (activeTask != null)
                return;

            if (side == null) {
                delay = 1;
                oxCrd = -1;
                oyCrd = -1;
                xCrd = -1;
                yCrd = -1;
                nxCrd = -1;
                nyCrd = -1;
                return;
            }

            double anxx = nextX;
            double anxy = nextY;

            if (
                    anxx == -1 || anxy == -1 ||
                            nxCrd == -1 || nyCrd == -1 ||
                            oxCrd == -1 || oyCrd == -1 ||
                            xCrd == -1 || yCrd == -1
            ) {
                ScreenData data = tes.getScreen(side);
                if (data == null)
                    return;

                anxx = data.size.x / 2.0;
                anxy = data.size.y / 2.0;

                if (nxCrd == -1) {
                    oxCrd = xCrd = nxCrd = anxx;
                    oyCrd = yCrd = nyCrd = anxy;
                }
            }

            if (activeTask == null)
                pollElement();

            nxCrd = anxx;
            nyCrd = anxy;

            oxCrd = Mth.lerp(0.5, oxCrd, xCrd);
            xCrd = Mth.lerp(0.15, xCrd, nxCrd);

            oyCrd = Mth.lerp(0.5, oyCrd, yCrd);
            yCrd = Mth.lerp(0.15, yCrd, nyCrd);
        }
    }
}
