package com.jwhybrid;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;

public class DinoRenderer extends MobRenderer<DinoEntity, DinoModel> {
    private static final EnumMap<Species, ResourceLocation> TEX = new EnumMap<>(Species.class);
    static {
        for (Species s : Species.values())
            TEX.put(s, new ResourceLocation(JwHybrid.MODID, "textures/entity/" + s.id + ".png"));
    }

    public DinoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DinoModel(ctx.bakeLayer(DinoModel.LAYER)), 0.8f);
    }

    @Override
    public ResourceLocation getTextureLocation(DinoEntity e) { return TEX.get(e.getSpecies()); }

    @Override
    protected void scale(DinoEntity e, PoseStack ps, float partial) {
        float s = e.getSpecies().scale;
        ps.scale(s, s, s);
        this.shadowRadius = 0.6f * s;
    }
}
