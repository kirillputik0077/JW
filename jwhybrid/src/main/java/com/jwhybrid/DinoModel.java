package com.jwhybrid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class DinoModel extends EntityModel<DinoEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(JwHybrid.MODID, "dino"), "main");

    private final ModelPart root, head, tail;
    private final ModelPart[] legs = new ModelPart[4];

    public DinoModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.tail = root.getChild("tail");
        for (int i = 0; i < 4; i++) legs[i] = root.getChild("leg" + i);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3f, -3f, -8f, 6f, 6f, 16f), PartPose.offset(0f, 14f, 0f));
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 24)
                .addBox(-2.5f, -3f, -6f, 5f, 5f, 6f), PartPose.offset(0f, 12f, -8f));
        r.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 36)
                .addBox(-1.5f, -1.5f, 0f, 3f, 3f, 12f), PartPose.offset(0f, 13f, 8f));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 52).addBox(-1.5f, 0f, -1.5f, 3f, 8f, 3f);
        r.addOrReplaceChild("leg0", leg, PartPose.offset(-3f, 16f, -5f));
        r.addOrReplaceChild("leg1", leg, PartPose.offset(3f, 16f, -5f));
        r.addOrReplaceChild("leg2", leg, PartPose.offset(-3f, 16f, 5f));
        r.addOrReplaceChild("leg3", leg, PartPose.offset(3f, 16f, 5f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(DinoEntity e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float a = Mth.cos(limbSwing * 0.6662f) * 1.2f * limbSwingAmount;
        legs[0].xRot = a;
        legs[3].xRot = a;
        legs[1].xRot = -a;
        legs[2].xRot = -a;
        tail.yRot = Mth.cos(age * 0.1f) * 0.15f;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(ps, vc, light, overlay, r, g, b, a);
    }
}
