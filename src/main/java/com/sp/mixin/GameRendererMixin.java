package com.sp.mixin;

import com.sp.VeilShadows;
import com.sp.VeilShadowsClient;
import com.sp.render.ShadowMapRenderer;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//This is just to distort the shadow map

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Unique private static final ResourceLocation shadowSolid = new ResourceLocation(VeilShadows.MOD_ID, "shadowmap/rendertype_solid");
    @Unique private static final ResourceLocation shadowEntity = new ResourceLocation(VeilShadows.MOD_ID, "shadowmap/rendertype_entity");


    @Inject(method = {
            "getRendertypeSolidShader",
            "getRendertypeCutoutShader",
            "getRendertypeCutoutMippedShader"
            }, at = @At("HEAD"), cancellable = true)
    private static void setSolidShader(CallbackInfoReturnable<ShaderInstance> cir) {
        if(ShadowMapRenderer.isRenderingShadowMap()) {
            foundry.veil.api.client.render.shader.program.ShaderProgram shader = VeilRenderSystem.setShader(shadowSolid);
            if (shader == null) {
                return;
            }
            cir.setReturnValue(shader.toShaderInstance());
        }
    }

    @Inject(method = {
            "getRendertypeEntityTranslucentShader",
            "getRendertypeEntitySolidShader",
            "getRendertypeEntityCutoutShader",
            "getRendertypeEntityCutoutNoCullShader",
            "getRendertypeEntityTranslucentCullShader"
    }, at = @At("TAIL"), cancellable = true)
    private static void setEntityShader(CallbackInfoReturnable<ShaderInstance> cir) {
        if(ShadowMapRenderer.isRenderingShadowMap()) {
            foundry.veil.api.client.render.shader.program.ShaderProgram shader = VeilRenderSystem.setShader(shadowEntity);
            if (shader == null) {
                return;
            }
            cir.setReturnValue(shader.toShaderInstance());
        }
    }

}
