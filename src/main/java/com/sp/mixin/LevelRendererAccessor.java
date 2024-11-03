package com.sp.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {

    @Invoker("renderChunkLayer")
    void invokeRenderLayer(RenderType renderLayer, PoseStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f positionMatrix);

    @Invoker("setupRender")
    void invokeSetupTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator);

    @Invoker("prepareCullFrustum")
    void invokeSetupFrustum(PoseStack matrices, Vec3 pos, Matrix4f projectionMatrix);

    @Accessor("cullingFrustum")
    void setFrustum(Frustum frustum);

    @Accessor("cullingFrustum")
    Frustum getFrustum();

    @Accessor("entityRenderDispatcher")
    EntityRenderDispatcher getEntityRenderDispatcher();

    @Invoker("renderEntity")
    void invokeRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers);

    @Accessor("renderBuffers")
    RenderBuffers getBufferBuilders();

}
