package com.jwhybrid;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JwHybrid.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(DinoModel.LAYER, DinoModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(JwHybrid.DINO.get(), DinoRenderer::new);
    }
}
