package com.jwhybrid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JwHybrid.MODID)
public class JwEvents {
    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) {
        JwCommands.register(e.getDispatcher());
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        Player p = e.getEntity();
        CompoundTag root = JwData.root(p);
        if (!root.getBoolean("starter")) {
            root.putBoolean("starter", true);
            p.getInventory().add(new ItemStack(JwHybrid.SCANNER.get()));
            p.sendSystemMessage(Component.literal("§6[Jurassic Hybrid] §fТебе выдан ДНК-сканер и 500 монет. Команда: §e/jw"));
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone e) {
        CompoundTag old = e.getOriginal().getPersistentData();
        if (old.contains(Player.PERSISTED_NBT_TAG)) {
            e.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG,
                    old.getCompound(Player.PERSISTED_NBT_TAG).copy());
        }
    }
}
