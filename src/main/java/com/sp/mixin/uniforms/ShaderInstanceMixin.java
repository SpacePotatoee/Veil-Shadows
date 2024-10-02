package com.sp.mixin.uniforms;


import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.sp.util.UniformStuff;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceMixin implements UniformStuff {

    @Shadow public abstract @Nullable Uniform getUniform(String string);

    @Override
    public Uniform getOrthoMatrix() {
        return orthoMatrix;
    }

    @Override
    public Uniform getViewMatrix() {
        return viewMatrix;
    }

    @Override
    public Uniform getLightAngle() {
        return lightAngle;
    }

    @Unique
    public Uniform orthoMatrix;

    @Unique
    public Uniform viewMatrix;

    @Unique
    public Uniform lightAngle;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void injectUniforms(ResourceProvider resourceProvider, String string, VertexFormat vertexFormat, CallbackInfo ci){
        this.orthoMatrix = this.getUniform("orthoMatrix");
        this.viewMatrix = this.getUniform("viewRix");
        this.lightAngle = this.getUniform("lightAngle");
    }

}
