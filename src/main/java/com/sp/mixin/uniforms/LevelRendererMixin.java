package com.sp.mixin.uniforms;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sp.VeilShadowsClient;
import com.sp.util.UniformStuff;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/Uniform;set(F)V", ordinal = 3, shift = At.Shift.BY, by = 2))
    public void uniformInject(RenderType renderType, PoseStack poseStack, double d, double e, double f, Matrix4f matrix4f, CallbackInfo ci, @Local ShaderInstance shaderProgram){
        if(shaderProgram instanceof UniformStuff) {
            if (((UniformStuff) shaderProgram).getOrthoMatrix() != null) {
                Matrix4f matrix = VeilShadowsClient.createProjMat();
                ((UniformStuff) shaderProgram).getOrthoMatrix().set(matrix);
            }

            if (((UniformStuff) shaderProgram).getViewMatrix() != null) {
                PoseStack shadowModelView = VeilShadowsClient.createShadowModelView(d, e, f, true);

                ((UniformStuff) shaderProgram).getViewMatrix().set(shadowModelView.last().pose());
            }

            if (((UniformStuff) shaderProgram).getLightAngle() != null) {
                Matrix4f shadowModelView = VeilShadowsClient.createShadowModelView(d, e, f, false).last().pose();
                //shadowModelView.identity();
                //shadowModelView.rotate(Axis.XP.rotationDegrees(25.0f * Mth.sin(RenderSystem.getShaderGameTime() * 200) + 90.0f));
                Vector4f lightPosition = new Vector4f(0.0f, 0.0f, 1.0f, 0.0f);
                lightPosition.mul(shadowModelView.invert());

                Vector3f shadowLightDirection = new Vector3f(lightPosition.x(), lightPosition.y(), lightPosition.z());

                ((UniformStuff) shaderProgram).getLightAngle().set(shadowLightDirection);
            }
        }
    }

}
