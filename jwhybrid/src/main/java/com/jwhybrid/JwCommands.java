package com.jwhybrid;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;

/** /jw — статистика, /jw shop — цены, /jw create <вид> — создать динозавра за ДНК + монеты (экономика JW The Game). */
public class JwCommands {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("jw")
                .executes(c -> status(c.getSource()))
                .then(Commands.literal("shop").executes(c -> shop(c.getSource())))
                .then(Commands.literal("create").then(Commands.argument("species", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                Arrays.stream(Species.values()).map(s -> s.id), b))
                        .executes(c -> create(c.getSource(), StringArgumentType.getString(c, "species"))))));
    }

    private static int status(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        StringBuilder sb = new StringBuilder("§6Монеты: §f" + JwData.coins(p) + "\n§bДНК:");
        for (Species s : Species.values()) {
            sb.append("\n §7").append(s.ru).append(": §f").append(JwData.dna(p, s)).append("/").append(s.dnaCost);
        }
        sb.append("\n§7/jw shop — цены, /jw create <вид> — создать динозавра");
        src.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int shop(CommandSourceStack src) {
        StringBuilder sb = new StringBuilder("§6Лаборатория (ДНК + монеты) и доход в минуту:");
        for (Species s : Species.values()) {
            sb.append("\n §e").append(s.id).append(" §7— ").append(s.ru).append(": §b").append(s.dnaCost)
              .append(" ДНК §6+ ").append(s.coinCost).append(" монет §7(доход ").append(s.income).append("/мин за уровень)");
        }
        src.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int create(CommandSourceStack src, String id) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        Species s = Species.byName(id);
        if (s == null) { src.sendFailure(Component.literal("Неизвестный вид. Смотри /jw shop")); return 0; }
        if (JwData.dna(p, s) < s.dnaCost) {
            src.sendFailure(Component.literal("Мало ДНК: " + JwData.dna(p, s) + "/" + s.dnaCost + ". Сканируй диких динозавров сканером."));
            return 0;
        }
        if (JwData.coins(p) < s.coinCost) {
            src.sendFailure(Component.literal("Мало монет: " + JwData.coins(p) + "/" + s.coinCost));
            return 0;
        }
        JwData.addDna(p, s, -s.dnaCost);
        JwData.addCoins(p, -s.coinCost);
        var cap = DinoCapsuleItem.make(s, 1);
        if (!p.getInventory().add(cap)) p.drop(cap, false);
        src.sendSuccess(() -> Component.literal("§aСоздана капсула: " + s.ru + ". Нажми ПКМ по блоку, чтобы выпустить."), false);
        return 1;
    }
}
