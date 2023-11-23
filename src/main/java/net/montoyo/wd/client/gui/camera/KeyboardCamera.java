package net.montoyo.wd.client.gui.camera;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.montoyo.wd.client.js.WDRouter;
import net.montoyo.wd.entity.ScreenData;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.BlockSide;

import java.util.concurrent.CompletableFuture;

public class KeyboardCamera {
    private static TileEntityScreen tes;
    private static BlockSide side;

    private static double oxCrd = -1;
    private static double xCrd = -1;
    private static double nxCrd = -1;
    private static double oyCrd = -1;
    private static double yCrd = -1;
    private static double nyCrd = -1;

    protected static void updateCrd(JsonObject obj) {
        if (obj.getAsJsonPrimitive("exists").getAsBoolean()) {
            ScreenData scr = tes.getScreen(side);
            if (scr != null) {
                nxCrd = obj.getAsJsonPrimitive("x").getAsDouble() + obj.getAsJsonPrimitive("w").getAsDouble() / 2;
                nyCrd = obj.getAsJsonPrimitive("y").getAsDouble() + obj.getAsJsonPrimitive("h").getAsDouble() / 2;

                nxCrd /= scr.resolution.x;
                nxCrd *= scr.size.x;

                nyCrd /= scr.resolution.y;
                nyCrd = 1 - nyCrd;
                nyCrd *= scr.size.y;
            }
        }
    }

    private static WDRouter.Task<JsonObject> activeTask;
    private static long futureStart = 0;

    protected static void pollElement() {
        if (activeTask != null) return;

        TileEntityScreen teTmp = tes;
        BlockSide sdTmp = side;

        // async nonsense can occur here
        if (teTmp == null || sdTmp == null) return;

        ScreenData scr = teTmp.getScreen(sdTmp);
        if (scr != null) {
            activeTask = WDRouter.INSTANCE.requestJson(
                    scr.browser, "ActiveElement", """
                                                        try {
                                                        let focusedElement = document.activeElement;
                                                        if (focusedElement == null) {
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
                                                        }
                            """.replace("\n", "")
            ).thenAccept((o1) -> {
                System.out.println(o1);
                updateCrd(o1);
                activeTask = null;
            });
            futureStart = System.currentTimeMillis();
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

        if (xCrd < 0) return;
        if (yCrd < 0) return;

        // TODO: implement

        double focalX = tes.getBlockPos().getX() +
                side.right.x * (xCrd - 1) + side.up.x * yCrd;
        double focalY = tes.getBlockPos().getY() +
                side.right.y * (xCrd - 1) + side.up.y * yCrd;
        double focalZ = tes.getBlockPos().getZ() +
                side.right.z * (xCrd - 1) + side.up.z * yCrd;

        double pct = (delay - event.getPartialTick()) / 8f;
        pct *= pct;
        focalX = Mth.lerp(pct,
                focalX,
                tes.getBlockPos().getX() +
                        side.right.x * (oxCrd - 1) + side.up.x * oyCrd
        );
        focalY = Mth.lerp(pct,
                focalY,
                tes.getBlockPos().getY() +
                        side.right.y * (oxCrd - 1) + side.up.y * oyCrd
        );
        focalZ = Mth.lerp(pct,
                focalZ,
                tes.getBlockPos().getZ() +
                        side.right.z * (oxCrd - 1) + side.up.z * oyCrd
        );

//        focalX -= side.forward.x * 0.5f;
//        focalY -= side.forward.y * 0.5f;
//        focalZ -= side.forward.z * 0.5f;

        float[] angle = lookAt(
                event.getCamera().getEntity(),
                EntityAnchorArgument.Anchor.EYES,
                new Vec3(focalX, focalY, focalZ)
        );

        double scl = 20;

        double mx = Minecraft.getInstance().mouseHandler.xpos();
        mx /= Minecraft.getInstance().getWindow().getWidth();
        mx *= scl;
        mx -= (scl / 2);
        mx = signedSqrt(mx);
        angle[1] += mx;

        double my = Minecraft.getInstance().mouseHandler.ypos();
        my /= Minecraft.getInstance().getWindow().getHeight();
        my *= scl;
        my -= (scl / 2);
        my = signedSqrt(my);
        angle[0] += my;

        float xRot = event.getYaw(); // left right
        float yRot = event.getPitch(); // up down

        event.setYaw(angle[1]);
        event.setPitch(angle[0]);
    }

    public static void focus(TileEntityScreen screen, BlockSide side) {
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

            if (nxCrd == -1) {
                if (activeTask == null) {
                    delay = 1;
                    pollElement();
                    return;
                }
            }

            if (oxCrd == xCrd && delay > 2) {
                delay = 2;
            }

            delay--;
            if (delay == 0) {
                oxCrd = xCrd;
                xCrd = nxCrd;
                oyCrd = yCrd;
                yCrd = nyCrd;
                pollElement();
            } else if (delay < 0) {
                oxCrd = xCrd;
                xCrd = nxCrd;
                oyCrd = yCrd;
                yCrd = nyCrd;
                delay = 8;
            }
        }
    }
}
