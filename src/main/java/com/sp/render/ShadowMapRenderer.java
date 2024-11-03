package com.sp.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import com.sp.VeilShadows;
import com.sp.mixin.LevelRendererAccessor;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ShadowMapRenderer {
    private static boolean renderingShadowMap;


    public static void renderShadowMap(Camera camera, float tickDelta, Level world){
        Minecraft client = Minecraft.getInstance();
        LevelRendererAccessor accessor = (LevelRendererAccessor) client.levelRenderer;
        Vec3 cameraPos = camera.getPosition();
        PoseStack shadowModelView = createShadowModelView(cameraPos.x, cameraPos.y, cameraPos.z, tickDelta, true);
        Matrix4f shadowProjMat = createProjMat();
        Matrix4f backupProjMat = RenderSystem.getProjectionMatrix();

        int width = client.getMainRenderTarget().width;
        int height = client.getMainRenderTarget().height;
        Frustum frustum;

        AdvancedFbo shadowMap = VeilRenderSystem.renderer().getFramebufferManager().getFramebuffer(new ResourceLocation(VeilShadows.MOD_ID, "shadowmap"));
        if(shadowMap != null) {
            RenderSystem.setProjectionMatrix(shadowProjMat, VertexSorting.ORTHOGRAPHIC_Z);

            shadowMap.bind(true);
            setRenderingShadowMap(true);


            frustum = new Frustum(shadowModelView.last().pose(), shadowProjMat);
            frustum.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
            accessor.setFrustum(frustum);
            accessor.invokeSetupTerrain(camera, frustum, false, false);
            accessor.invokeRenderLayer(RenderType.cutout(), shadowModelView, cameraPos.x, cameraPos.y, cameraPos.z, shadowProjMat);
            accessor.invokeRenderLayer(RenderType.cutoutMipped(), shadowModelView, cameraPos.x, cameraPos.y, cameraPos.z, shadowProjMat);
            accessor.invokeRenderLayer(RenderType.solid(), shadowModelView, cameraPos.x, cameraPos.y, cameraPos.z, shadowProjMat);

            if(client.level != null) {
                MultiBufferSource.BufferSource immediate = accessor.getBufferBuilders().bufferSource();

                for(Entity entity : client.level.entitiesForRendering()){
                    if(accessor.getEntityRenderDispatcher().shouldRender(entity, accessor.getFrustum(), cameraPos.x, cameraPos.y, cameraPos.z) || entity.isSpectator()){
                        accessor.invokeRenderEntity(entity, cameraPos.x, cameraPos.y, cameraPos.z, tickDelta, shadowModelView, immediate);
                    }
                }

                immediate.endBatch();

            }
            setRenderingShadowMap(false);
            AdvancedFbo.unbind();
            RenderSystem.viewport(0, 0, width, height);

            RenderSystem.setProjectionMatrix(backupProjMat, VertexSorting.DISTANCE_TO_ORIGIN);

        }
    }

    /**
     The "do interval" bit was taken from the Iris Shadow Matrices class in order to keep the Shadows from flashing
     <a href="https://github.com/IrisShaders/Iris/blob/3fc94e8f41535feebce0bcb4235eff4a809f5eea/common/src/main/java/net/irisshaders/iris/shadows/ShadowMatrices.java">HERE</a>
     */
    public static PoseStack createShadowModelView(double CameraX, double CameraY, double CameraZ, float tickDelta, boolean doInterval){
        PoseStack shadowModelView = new PoseStack();

        shadowModelView.last().normal().identity();
        shadowModelView.last().pose().identity();

        shadowModelView.last().pose().translate(0.0f, 0.0f, -100.0f);
        rotateShadowModelView(shadowModelView.last().pose(), tickDelta);

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

    //Global Light Rotation
    public static void rotateShadowModelView(Matrix4f shadowModelView, float tickDelta){
        Minecraft client = Minecraft.getInstance();

        //Comment these if shadows are following the sun
        shadowModelView.rotate(Axis.XP.rotationDegrees(45f));
        shadowModelView.rotate(Axis.YP.rotationDegrees(45f));

        //Un-comment these to make the shadows follow the sun
//        shadowModelView.rotate(Axis.XP.rotationDegrees(80.0F));
//        float j = Mth.sin(client.level.getSunAngle(tickDelta));
//        shadowModelView.rotate(Axis.ZN.rotationDegrees((float)Math.toDegrees(j)));
    }

    public static Matrix4f createProjMat(){
        return orthographicMatrix(160, 0.05f, 256.0f);
    }


    /**
     Also taken from Iris lol. Turns out their orthographic Matrix is better
     <a href="https://github.com/IrisShaders/Iris/blob/3fc94e8f41535feebce0bcb4235eff4a809f5eea/common/src/main/java/net/irisshaders/iris/shadows/ShadowMatrices.java">HERE</a>
     */
    public static Matrix4f orthographicMatrix(float halfPlaneLength, float nearPlane, float farPlane) {
        return new Matrix4f(
                1.0f / halfPlaneLength, 0f, 0f, 0f,
                0f, 1.0f / halfPlaneLength, 0f, 0f,
                0f, 0f, 2.0f / (nearPlane - farPlane), 0f,
                0f, 0f, -(farPlane + nearPlane) / (farPlane - nearPlane), 1f
        );
    }

    public static boolean isRenderingShadowMap() {
        return renderingShadowMap;
    }

    public static void setRenderingShadowMap(boolean l) {
        renderingShadowMap = l;
    }

}
