package com.jwhybrid;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Механика JW Alive: ПКМ по дикому динозавру = скан и сбор ДНК. Чем слабее динозавр, тем больше ДНК. */
public class DnaScannerItem extends Item {
    public DnaScannerItem(Properties props) { super(props); }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof DinoEntity d) || d.isTame()) return InteractionResult.PASS;
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResult.CONSUME;
        if (!player.level().isClientSide && player instanceof ServerPlayer sp) {
            float weak = 1f - d.getHealth() / d.getMaxHealth();
            int gain = Math.max(1, Math.round(d.getSpecies().dnaCost * 0.08f * (1f + weak * 3f)));
            JwData.addDna(sp, d.getSpecies(), gain);
            sp.getCooldowns().addCooldown(this, 40);
            sp.displayClientMessage(Component.literal("§bСкан: " + d.getSpecies().ru + " §f+" + gain + " ДНК §7(всего "
                    + JwData.dna(sp, d.getSpecies()) + "/" + d.getSpecies().dnaCost + ")"), true);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}
