package com.jwhybrid;

import net.minecraft.util.RandomSource;

public enum Species {
    //        id             имя             hp   dmg  speed  w     h     scale aggr   weight dna  coins income
    RAPTOR("raptor", "Раптор", 30, 5, 0.38, 0.9f, 1.3f, 1.0f, true, 30, 100, 300, 4),
    DILOPHOSAURUS("dilophosaurus", "Дилофозавр", 25, 4, 0.34, 0.8f, 1.2f, 0.9f, true, 30, 80, 250, 3),
    TRICERATOPS("triceratops", "Трицератопс", 70, 8, 0.25, 1.6f, 1.9f, 1.7f, false, 15, 250, 800, 10),
    ANKYLOSAURUS("ankylosaurus", "Анкилозавр", 80, 7, 0.22, 1.6f, 1.3f, 1.3f, false, 15, 250, 800, 10),
    TREX("trex", "Тираннозавр", 120, 14, 0.30, 2.0f, 3.0f, 2.6f, true, 5, 600, 2500, 30);

    public final String id, ru;
    public final double hp, dmg, speed;
    public final float width, height, scale;
    public final boolean aggressive;
    public final int weight, dnaCost, coinCost, income;

    Species(String id, String ru, double hp, double dmg, double speed, float w, float h, float scale,
            boolean aggressive, int weight, int dnaCost, int coinCost, int income) {
        this.id = id; this.ru = ru; this.hp = hp; this.dmg = dmg; this.speed = speed;
        this.width = w; this.height = h; this.scale = scale; this.aggressive = aggressive;
        this.weight = weight; this.dnaCost = dnaCost; this.coinCost = coinCost; this.income = income;
    }

    public static Species byName(String name) {
        for (Species s : values()) if (s.id.equalsIgnoreCase(name)) return s;
        return null;
    }

    public static Species byOrdinal(int i) {
        Species[] v = values();
        return v[Math.max(0, Math.min(i, v.length - 1))];
    }

    public static Species random(RandomSource r) {
        int total = 0;
        for (Species s : values()) total += s.weight;
        int roll = r.nextInt(total);
        for (Species s : values()) {
            roll -= s.weight;
            if (roll < 0) return s;
        }
        return RAPTOR;
    }
}
