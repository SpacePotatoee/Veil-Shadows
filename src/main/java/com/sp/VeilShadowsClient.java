package com.sp;

import com.sp.render.ShadowMapRenderer;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.platform.VeilEventPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class VeilShadowsClient implements ClientModInitializer {
    private Camera camera;
    private static final ResourceLocation SHADOWS = new ResourceLocation(VeilShadows.MOD_ID, "shadows");

    @Override
    public void onInitializeClient() {

        VeilEventPlatform.INSTANCE.onVeilRenderTypeStageRender(((stage, levelRenderer, bufferSource, poseStack, projectionMatrix, renderTick, partialTicks, camera, frustum) -> {
            if(camera != null){
                this.camera = camera;
            }

            Minecraft client = Minecraft.getInstance();
            if(stage == VeilRenderLevelStageEvent.Stage.AFTER_SKY) {
                if (camera != null) {
                    ShadowMapRenderer.renderShadowMap(camera, partialTicks, client.level);
                }
            }
        }));


        VeilEventPlatform.INSTANCE.preVeilPostProcessing(((name, pipeline, context) -> {
            Minecraft client = Minecraft.getInstance();

            if(SHADOWS.equals(name)){
                ShaderProgram shaderProgram = context.getShader(SHADOWS);
                if(shaderProgram != null){
                    setShadowUniforms(shaderProgram, client.level);
                }
            }

        }));

    }

    public void setShadowUniforms(ShaderProgram shaderProgram, Level world){
        Minecraft client = Minecraft.getInstance();
        Matrix4f shadowModelView = new Matrix4f();
        shadowModelView.identity();
        ShadowMapRenderer.rotateShadowModelView(shadowModelView, client.getFrameTime());
        Vector4f lightPosition = new Vector4f(0.0f, 0.0f, 1.0f, 0.0f);
        lightPosition.mul(shadowModelView.invert());

        Vector3f shadowLightDirection = new Vector3f(lightPosition.x(), lightPosition.y(), lightPosition.z());
        shaderProgram.setMatrix("viewMatrix", ShadowMapRenderer.createShadowModelView(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z, client.getFrameTime(), true).last().pose());
        shaderProgram.setMatrix("orthographMatrix", ShadowMapRenderer.createProjMat());
        shaderProgram.setVector("lightAngled", shadowLightDirection);
    }

}
