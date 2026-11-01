package com.jwhybrid;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Капсула с динозавром: ПКМ по блоку выпускает прирученного динозавра. Shift+ПКМ пустой рукой по своему динозавру убирает его обратно. */
public class DinoCapsuleItem extends Item {
    public DinoCapsuleItem(Properties props) { super(props); }

    public static ItemStack make(Species s, int level) {
        ItemStack st = new ItemStack(JwHybrid.CAPSULE.get());
        CompoundTag tag = st.getOrCreateTag();
        tag.putInt("Species", s.ordinal());
        tag.putInt("Level", level);
        return st;
    }

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag t = stack.getTag();
        if (t == null) return Component.literal("Пустая капсула");
        return Component.literal("Капсула: " + Species.byOrdinal(t.getInt("Species")).ru + " ур." + Math.max(1, t.getInt("Level")));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player pl = ctx.getPlayer();
        Level lv = ctx.getLevel();
        ItemStack stack = ctx.getItemInHand();
        if (pl == null || stack.getTag() == null) return InteractionResult.PASS;
        if (!lv.isClientSide) {
            Species s = Species.byOrdinal(stack.getTag().getInt("Species"));
            int level = Math.max(1, stack.getTag().getInt("Level"));
            DinoEntity e = JwHybrid.DINO.get().create(lv);
            if (e == null) return InteractionResult.FAIL;
            BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
            e.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, pl.getYRot(), 0);
            e.setSpecies(s);
            e.setDinoLevel(level);
            e.tame(pl);
            e.setHealth(e.getMaxHealth());
            lv.addFreshEntity(e);
            if (!pl.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(lv.isClientSide);
    }
}
