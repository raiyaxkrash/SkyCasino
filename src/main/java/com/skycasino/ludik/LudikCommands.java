package com.skycasino.ludik;

import com.skycasino.SkyCasinoMod;
import com.skycasino.economy.ChipEconomy;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SkyCasinoMod.MOD_ID)
public class LudikCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("ludik")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            p.sendSystemMessage(Component.literal(
                                    "§6§l[Статус Лудика] §eАзарт: §f" + LudikData.getAddiction(p) + "% "
                                            + "§7| §aСерия побед: §f" + LudikData.getWinStreak(p)
                                            + " §7| §cДолг: §f" + ChipEconomy.formatChips(LudikData.getDebt(p))
                                            + " §7| §5Ломка: §f" + (LudikData.isInWithdrawal(p) ? "ДА (Ломбард открыт!)" : "Нет")
                            ));
                            return 1;
                        })
                        .then(Commands.literal("lomka").requires(s -> s.hasPermission(2)).executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            LudikData.setAddiction(p, Math.max(50, LudikData.getAddiction(p)));
                            LudikData.setForcedWithdrawal(p, true);
                            LudikManager.syncToClient(p);
                            p.sendSystemMessage(Component.literal("§5[Тест] Ломка активирована! Теперь можно сдавать предметы в Ломбард."));
                            return 1;
                        }))
        );
    }
}
