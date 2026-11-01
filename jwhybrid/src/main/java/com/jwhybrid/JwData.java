package com.jwhybrid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/** Данные игрока: монеты и ДНК. Хранятся в PlayerPersisted (переживают смерть). */
public final class JwData {
    private JwData() {}

    public static CompoundTag root(Player p) {
        CompoundTag pd = p.getPersistentData();
        if (!pd.contains(Player.PERSISTED_NBT_TAG)) pd.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        CompoundTag persisted = pd.getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.contains("jwhybrid")) {
            CompoundTag n = new CompoundTag();
            n.putInt("coins", 500);
            persisted.put("jwhybrid", n);
        }
        return persisted.getCompound("jwhybrid");
    }

    public static int coins(Player p) { return root(p).getInt("coins"); }
    public static void addCoins(Player p, int n) { root(p).putInt("coins", Math.max(0, coins(p) + n)); }

    public static int dna(Player p, Species s) { return root(p).getInt("dna_" + s.id); }
    public static void addDna(Player p, Species s, int n) { root(p).putInt("dna_" + s.id, Math.max(0, dna(p, s) + n)); }
}
