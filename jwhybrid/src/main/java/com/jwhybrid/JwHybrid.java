package com.jwhybrid;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(JwHybrid.MODID)
public class JwHybrid {
    public static final String MODID = "jwhybrid";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Item> SCANNER = ITEMS.register("dna_scanner",
            () -> new DnaScannerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CAPSULE = ITEMS.register("dino_capsule",
            () -> new DinoCapsuleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<EntityType<DinoEntity>> DINO = ENTITIES.register("dino",
            () -> EntityType.Builder.of(DinoEntity::new, MobCategory.CREATURE)
                    .sized(1.0f, 1.5f).clientTrackingRange(10).build("dino"));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("Jurassic Hybrid"))
                    .icon(() -> new ItemStack(SCANNER.get()))
                    .displayItems((params, out) -> {
                        out.accept(SCANNER.get());
                        for (Species s : Species.values()) out.accept(DinoCapsuleItem.make(s, 1));
                    }).build());

    public JwHybrid() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        ENTITIES.register(bus);
        TABS.register(bus);
    }
}
