package com.sp;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sp.mixin.LevelRendererAccessor;
import com.sp.util.MatrixMath;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.platform.VeilEventPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class VeilShadowsClient implements ClientModInitializer {
    static boolean renderingShadowMap;

    @Override
    public void onInitializeClient() {

        VeilEventPlatform.INSTANCE.onVeilRenderTypeStageRender(((stage, levelRenderer, bufferSource, poseStack, projectionMatrix, renderTick, partialTicks, camera, frustum) -> {
            if(stage == VeilRenderLevelStageEvent.Stage.AFTER_SKY) {
                if (camera != null) {
                    renderShadowMap(camera);
                }
            }
        }));

    }


    private void renderShadowMap(Camera camera){
        Minecraft client = Minecraft.getInstance();
        LevelRendererAccessor accessor = (LevelRendererAccessor) client.levelRenderer;
        Vec3 cameraPos = camera.getPosition();
        PoseStack shadowModelView = createShadowModelView(cameraPos.x, cameraPos.y, cameraPos.z, true);
        Matrix4f shadowProjMat = createProjMat();
        int width = client.getMainRenderTarget().viewWidth;
        int height = client.getMainRenderTarget().viewHeight;
        Frustum frustum;

        AdvancedFbo shadowMap = VeilRenderSystem.renderer().getFramebufferManager().getFramebuffer(new ResourceLocation(VeilShadows.MOD_ID, "shadowmap"));
        if(shadowMap != null) {
            shadowMap.bind(true);
            setRenderingShadowMap(true);

            frustum = new Frustum(shadowModelView.last().pose(), shadowProjMat);
            frustum.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
            accessor.setFrustum(frustum);
            accessor.invokeSetupTerrain(camera, frustum, false, false);
            accessor.invokeRenderLayer(RenderType.cutout(), shadowModelView, cameraPos.x, cameraPos.y, cameraPos.z, shadowProjMat);
            accessor.invokeRenderLayer(RenderType.cutoutMipped(), shadowModelView, cameraPos.x, cameraPos.y, cameraPos.z, shadowProjMat);
            accessor.invokeRenderLayer(RenderType.solid(), shadowModelView, cameraPos.x, cameraPos.y, cameraPos.z, shadowProjMat);

            setRenderingShadowMap(false);
            AdvancedFbo.unbind();
            RenderSystem.viewport(0, 0, width, height);

        }
    }

    public static PoseStack createShadowModelView(double CameraX, double CameraY, double CameraZ, boolean doInterval){
        PoseStack shadowModelView = new PoseStack();

        shadowModelView.last().normal().identity();
        shadowModelView.last().pose().identity();

        shadowModelView.last().pose().translate(0.0f, 0.0f, -100.0f);

        //Change this to change the angle of the shadows
        shadowModelView.mulPose(Axis.XP.rotationDegrees(45f));
        shadowModelView.mulPose(Axis.YP.rotationDegrees(45f));


        /**
         This bit was taken from the Iris Shadow Matrices class in order to keep the Shadows from flashing
         https://github.com/IrisShaders/Iris/blob/3fc94e8f41535feebce0bcb4235eff4a809f5eea/common/src/main/java/net/irisshaders/iris/shadows/ShadowMatrices.java
         */
        if(doInterval) {
            float offsetX = (float) CameraX % 2.0f;
            float offsetY = (float) CameraY % 2.0f;
            float offsetZ = (float) CameraZ % 2.0f;

            float halfIntervalSize = 1.0f;

            offsetX -= halfIntervalSize;
            offsetY -= halfIntervalSize;
            offsetZ -= halfIntervalSize;
            shadowModelView.last().pose().translate(offsetX, offsetY, offsetZ);
        }
        return shadowModelView;
    }

    public static Matrix4f createProjMat(){
        return MatrixMath.orthographicMatrix(160, 0.05f, 256.0f);
    }


    public static boolean isRenderingShadowMap() {
        return renderingShadowMap;
    }

    public static void setRenderingShadowMap(boolean l) {
        renderingShadowMap = l;
    }

}
