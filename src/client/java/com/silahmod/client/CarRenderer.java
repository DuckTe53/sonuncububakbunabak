package com.silahmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.silahmod.vehicle.CarEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Basit blok geometrili araç renderı; hasar aldıkça gövde blokları çatlak/yanık görünümüne döner. */
public class CarRenderer extends EntityRenderer<CarEntity, CarRenderState> {
    private final BlockRenderDispatcher blocks;
    public CarRenderer(EntityRendererProvider.Context context) { super(context); this.blocks = context.getBlockRenderDispatcher(); }
    @Override public CarRenderState createRenderState() { return new CarRenderState(); }
    @Override public void extractRenderState(CarEntity entity, CarRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.carClass = entity.getEntityData().get(CarEntity.CAR_CLASS);
        state.damageStage = entity.getEntityData().get(CarEntity.DAMAGE_STAGE);
    }
    private void cube(PoseStack pose, MultiBufferSource buffers, int light, BlockState block, float x, float y, float z, float sx, float sy, float sz) {
        pose.pushPose(); pose.translate(x, y, z); pose.scale(sx, sy, sz);
        blocks.renderSingleBlock(block, pose, buffers, light, OverlayTexture.NO_OVERLAY); pose.popPose();
    }
    @Override public void render(CarRenderState state, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose(); pose.translate(-0.5, 0.0, -0.5);
        BlockState body = state.damageStage >= 2 ? Blocks.GRAY_CONCRETE.defaultBlockState() : state.carClass == 2 ? Blocks.BLACK_CONCRETE.defaultBlockState() : state.carClass == 1 ? Blocks.BLUE_CONCRETE.defaultBlockState() : Blocks.RED_CONCRETE.defaultBlockState();
        BlockState glass = state.damageStage > 0 ? Blocks.GRAY_STAINED_GLASS.defaultBlockState() : Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
        boolean skoda = state.carClass == 1;
        // Skoda: daha kısa hatchback gövde; metal tamponlar ve zırhlı yan paneller.
        float length = skoda ? 1.28f : 1.6f;
        float width = skoda ? 0.84f : 1.0f;
        cube(pose,buffers,light,body,0,0.2f,0,width,0.35f,length);
        cube(pose,buffers,light,body,0,0.48f,-0.10f,skoda ? 0.70f : 0.78f,0.30f,skoda ? 0.62f : 0.72f);
        cube(pose,buffers,light,glass,0,0.52f,-0.10f,skoda ? 0.56f : 0.65f,0.20f,skoda ? 0.48f : 0.58f);
        BlockState wheel = Blocks.BLACK_CONCRETE.defaultBlockState();
        float wx = skoda ? 0.36f : 0.42f, wz = skoda ? 0.39f : 0.48f;
        cube(pose,buffers,light,wheel,-wx,0.08f,-wz,0.22f,0.30f,0.24f);
        cube(pose,buffers,light,wheel, wx,0.08f,-wz,0.22f,0.30f,0.24f);
        cube(pose,buffers,light,wheel,-wx,0.08f, wz,0.22f,0.30f,0.24f);
        cube(pose,buffers,light,wheel, wx,0.08f, wz,0.22f,0.30f,0.24f);
        if (skoda) {
            BlockState armor = state.damageStage > 0 ? Blocks.GRAY_CONCRETE.defaultBlockState() : Blocks.IRON_BLOCK.defaultBlockState();
            cube(pose,buffers,light,armor,0,0.19f,-0.69f,0.88f,0.12f,0.10f); // zırhlı ön tampon
            cube(pose,buffers,light,armor,0,0.19f,0.69f,0.88f,0.12f,0.10f); // zırhlı arka tampon
            cube(pose,buffers,light,armor,-0.43f,0.32f,0,0.08f,0.18f,0.80f);
            cube(pose,buffers,light,armor, 0.43f,0.32f,0,0.08f,0.18f,0.80f);
        }
        if (state.damageStage > 0) cube(pose,buffers,light,Blocks.BLACK_CONCRETE.defaultBlockState(),0,0.58f,0.05f,0.12f,0.08f,0.14f);
        pose.popPose(); super.render(state, pose, buffers, light);
    }
}
