package com.skycasino.ludik;

import com.skycasino.SkyCasinoMod;
import com.skycasino.economy.ChipEconomy;
import com.skycasino.item.ChipItem;
import com.skycasino.item.PocketCasinoItem;
import com.skycasino.item.VipCardItem;
import com.skycasino.network.LudikSyncPacket;
import com.skycasino.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

@Mod.EventBusSubscriber(modid = SkyCasinoMod.MOD_ID)
public class LudikManager {
    private static final Random RANDOM = new Random();

    private static final String[] WHISPERS = {
            "§5§o[Шёпот лудика] Ещё один спин... сейчас точно выпадет 777!",
            "§5§o[Шёпот лудика] Руки дрожат... пора крутануть рулетку!",
            "§5§o[Шёпот лудика] Заложи что-нибудь в Ломбард и отыграешься в ×10!",
            "§5§o[Шёпот лудика] 99% лудиков останавливаются прямо перед Джекпотом...",
            "§5§o[Шёпот лудика] Открой планшет казино... всего одна ставочка..."
    };

    public static void onBetPlaced(ServerPlayer player, long bet) {
        long now = player.level().getGameTime();
        LudikData.setLastBetTick(player, now);
        LudikData.setForcedWithdrawal(player, false);
        LudikData.incTotalSpins(player);

        int addDelta = bet >= 1000 ? 8 : (bet >= 100 ? 5 : 3);
        LudikData.setAddiction(player, LudikData.getAddiction(player) + addDelta);

        // Cure withdrawal immediately on spin & grant dopamine rush
        player.removeEffect(MobEffects.CONFUSION);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        player.removeEffect(MobEffects.WEAKNESS);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 0, false, false));

        syncToClient(player);
    }

    /**
     * Applies Hot Hand bonus, Debt garnishment, and Tilt penalties.
     * Returns the FINAL payout credited to the player (already handles bonus & debt deduction!).
     */
    public static long processOutcomeAndCredit(ServerPlayer player, long bet, long rawPayout, BlockPos pos) {
        if (rawPayout > 0) {
            int ws = LudikData.getWinStreak(player) + 1;
            LudikData.setWinStreak(player, ws);
            LudikData.setLossStreak(player, 0);

            double hotBonus = LudikData.getHotHandBonus(player);
            long finalPayout = Math.round(rawPayout * (1.0 + hotBonus));

            if (hotBonus > 0) {
                int pct = (int) Math.round(hotBonus * 100);
                player.displayClientMessage(
                        Component.literal("§6§l🔥 ГОРЯЧАЯ РУКА (Серия " + ws + ")! Бонус +" + pct + "% к выигрышу!"),
                        true
                );
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 400, 1, false, true));
                ServerLevel sl = player.serverLevel();
                sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.4, 0.5, 0.4, 0.05);
            }

            // Garnish 35% of winnings if player owes debt to the casino!
            long debt = LudikData.getDebt(player);
            if (debt > 0) {
                long garnish = Math.min(debt, Math.max(1L, Math.round(finalPayout * 0.35)));
                finalPayout -= garnish;
                LudikData.setDebt(player, debt - garnish);
                player.sendSystemMessage(Component.literal(
                        "§c[Казино-долг] С выигрыша списано §e" + ChipEconomy.formatChips(garnish)
                                + "§c в счёт долга. Остаток долга: §6" + ChipEconomy.formatChips(debt - garnish)
                ));
            }

            if (finalPayout > 0) {
                ChipEconomy.giveChips(player, finalPayout);
            }
            syncToClient(player);
            return finalPayout;
        } else {
            LudikData.setWinStreak(player, 0);
            int ls = LudikData.getLossStreak(player) + 1;
            LudikData.setLossStreak(player, ls);

            // Hardcore Tilt on 3+ consecutive losses!
            if (ls >= 3) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 1, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false));
                player.displayClientMessage(
                        Component.literal("§4§l💢 ЖЁСТКИЙ ТИЛЬТ! (" + ls + " слива подряд!) Выпей Кофе Анти-Тильт или отыграйся!"),
                        true
                );
                player.level().playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.9F, 0.8F);
            }
            syncToClient(player);
            return 0L;
        }
    }

    public static void takeLoan(ServerPlayer player, long amount) {
        if (amount <= 0 || amount > 50_000) return;
        long currentDebt = LudikData.getDebt(player);
        if (currentDebt + amount > 100_000) {
            player.displayClientMessage(Component.literal("§cЛимит микрозайма исчерпан (макс. 100 000)!"), true);
            return;
        }
        long owed = Math.round(amount * 1.25); // +25% hardcore interest
        LudikData.setDebt(player, currentDebt + owed);
        if (currentDebt == 0) {
            LudikData.setDebtDueTick(player, player.level().getGameTime() + 6000L); // 5 minutes
        }
        ChipEconomy.giveChips(player, amount);
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.9F, 0.8F);
        player.sendSystemMessage(Component.literal(
                "§6[Микрозайм] Выдано §e" + ChipEconomy.formatChips(amount)
                        + "§6 фишек. К возврату (+25%): §c" + ChipEconomy.formatChips(LudikData.getDebt(player))
                        + "§6. Верните за 5 минут, иначе придут Коллекторы!"
        ));
        syncToClient(player);
    }

    public static void repayDebt(ServerPlayer player) {
        long debt = LudikData.getDebt(player);
        if (debt <= 0) return;
        long bal = ChipEconomy.getTotalChips(player);
        long pay = Math.min(debt, bal);
        if (pay <= 0) return;

        if (ChipEconomy.deductChips(player, pay)) {
            LudikData.setDebt(player, debt - pay);
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.3F);
            player.sendSystemMessage(Component.literal(
                    "§a[Микрозайм] Погашено §e" + ChipEconomy.formatChips(pay)
                            + "§a фишек! Остаток долга: §6" + ChipEconomy.formatChips(LudikData.getDebt(player))
            ));
        }
        syncToClient(player);
    }

    public static long evaluatePawnValue(ItemStack stack) {
        if (stack.isEmpty()) return 0L;
        if (stack.getItem() instanceof ChipItem || stack.getItem() instanceof VipCardItem || stack.getItem() instanceof PocketCasinoItem) {
            return 0L;
        }
        // Check if it's already in Cashier exchange list (give 80% pawn rate)
        for (ChipEconomy.ExchangeEntry e : ChipEconomy.getExchangeEntries()) {
            if (stack.is(e.item())) {
                return Math.max(1L, Math.round(e.chipValue() * stack.getCount() * 0.8));
            }
        }

        long base = switch (stack.getRarity()) {
            case EPIC -> 600L;
            case RARE -> 250L;
            case UNCOMMON -> 80L;
            default -> 12L;
        };

        // Modded items get a nice bonus!
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id != null && !id.getNamespace().equals("minecraft")) {
            base = Math.max(base, 45L);
            if (id.getNamespace().equals("allthemodium") || id.getNamespace().equals("allthetweaks")) {
                base = Math.max(base, 2500L);
            } else if (id.getNamespace().equals("mekanism") || id.getNamespace().equals("draconicevolution")) {
                base = Math.max(base, 350L);
            }
        }

        // Enchantment value bonus
        int enchSum = EnchantmentHelper.getEnchantments(stack).values().stream().mapToInt(Integer::intValue).sum();
        base += enchSum * 120L;

        // Tool/armor bonus
        if (stack.isDamageableItem()) {
            base += 100L;
        }

        return Math.max(5L, base * stack.getCount());
    }

    public static void pawnHeldItem(ServerPlayer player) {
        if (!LudikData.isInWithdrawal(player)) {
            player.displayClientMessage(Component.literal("§eЛомбард доступен только во время Ломки!"), true);
            return;
        }
        ItemStack target = player.getMainHandItem();
        boolean fromOffhand = false;
        if (target.isEmpty() || evaluatePawnValue(target) <= 0) {
            target = player.getOffhandItem();
            fromOffhand = true;
        }
        long value = evaluatePawnValue(target);
        if (value <= 0) {
            player.displayClientMessage(Component.literal("§cВозьмите предмет для сдачи в Ломбард в руку (или левую руку)!"), true);
            return;
        }

        Component itemName = target.getHoverName().copy();
        int count = target.getCount();
        if (fromOffhand) {
            player.getInventory().offhand.set(0, ItemStack.EMPTY);
        } else {
            player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
        }
        player.getInventory().setChanged();

        ChipEconomy.giveChips(player, value);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.7F, 1.4F);
        player.sendSystemMessage(Component.literal(
                "§5[Ломбард] Вы сдали §f" + itemName.getString() + " ×" + count + "§5 за §6" + ChipEconomy.formatChips(value) + " фишек§5!"
        ));
        syncToClient(player);
    }

    public static void claimDodep(ServerPlayer player) {
        if (ChipEconomy.getTotalChips(player) > 0) {
            player.displayClientMessage(Component.literal("§eДодеп доступен только когда на счету 0 фишек!"), true);
            return;
        }
        long now = player.level().getGameTime();
        long last = LudikData.getLastDodepTick(player);
        if (last > 0 && (now - last) < 12000L) {
            long secLeft = (12000L - (now - last)) / 20L;
            player.displayClientMessage(Component.literal("§cСледующий додеп через " + secLeft + " сек."), true);
            return;
        }
        LudikData.setLastDodepTick(player, now);
        ChipEconomy.giveChips(player, 100L);
        player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_CELEBRATE, SoundSource.PLAYERS, 0.9F, 1.1F);
        player.server.getPlayerList().broadcastSystemMessage(
                Component.literal("§d§l[ДОДЕП] §f" + player.getName().getString() + " §eслил всё до нуля и получил утешительный додеп §6100 фишек§e!"),
                false
        );
        syncToClient(player);
    }

    private static void spawnCollectors(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        player.sendSystemMessage(Component.literal("§4§l⚠ ВНИМАНИЕ! Вы не вернули долг казино! ПРИШЛИ КОЛЛЕКТОРЫ!"));
        level.playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 1.0F, 0.85F);

        int count = LudikData.getDebt(player) >= 15_000 ? 3 : 2;
        for (int i = 0; i < count; i++) {
            Vindicator collector = EntityType.VINDICATOR.create(level);
            if (collector != null) {
                double angle = i * (Math.PI * 2.0 / count);
                double sx = player.getX() + Math.cos(angle) * 4.5;
                double sz = player.getZ() + Math.sin(angle) * 4.5;
                collector.moveTo(sx, player.getY() + 0.5, sz, 0, 0);
                collector.setCustomName(Component.literal("§4§lКоллектор Казино"));
                collector.setCustomNameVisible(true);
                collector.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
                collector.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                collector.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
                collector.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
                collector.setDropChance(EquipmentSlot.CHEST, 0.0F);
                collector.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 0));
                collector.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 0));
                collector.setTarget(player);
                level.addFreshEntity(collector);
            }
        }
        // Next collector wave in 3 minutes if still unpaid
        LudikData.setDebtDueTick(player, level.getGameTime() + 3600L);
    }

    public static void syncToClient(ServerPlayer player) {
        long now = player.level().getGameTime();
        long debt = LudikData.getDebt(player);
        long due = LudikData.getDebtDueTick(player);
        int secsLeft = debt > 0 ? (int) Math.max(0L, (due - now) / 20L) : 0;

        ItemStack held = player.getMainHandItem();
        if (held.isEmpty() || evaluatePawnValue(held) <= 0) held = player.getOffhandItem();
        long pawnVal = evaluatePawnValue(held);

        ModNetwork.sendToPlayer(new LudikSyncPacket(
                LudikData.getAddiction(player),
                LudikData.getWinStreak(player),
                LudikData.getLossStreak(player),
                debt,
                secsLeft,
                LudikData.getWithdrawalStage(player),
                pawnVal
        ), player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        long now = player.level().getGameTime();
        if (now % 40 != 0) return; // Check every 2 seconds

        // 1. Hot Hand aura particles
        if (LudikData.getWinStreak(player) >= 3) {
            player.serverLevel().sendParticles(ParticleTypes.FLAME,
                    player.getX(), player.getY() + 0.6, player.getZ(), 4, 0.35, 0.4, 0.35, 0.01);
        }

        // 2. Hardcore Withdrawal (Ломка)
        int stage = LudikData.getWithdrawalStage(player);
        if (stage >= 1) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 80, stage - 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0, false, false));
            if (stage >= 2) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, false, false));
                if (now % 200 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false));
                }
            }
            // Whisper every 20 seconds
            if (now % 400 == 0) {
                String whisper = WHISPERS[RANDOM.nextInt(WHISPERS.length)];
                player.displayClientMessage(Component.literal(whisper), true);
                player.level().playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE.value(), SoundSource.PLAYERS, 0.35F, 1.3F);
            }
        }

        // 3. Debt Collector check
        long debt = LudikData.getDebt(player);
        if (debt > 0 && now >= LudikData.getDebtDueTick(player)) {
            spawnCollectors(player);
        }
    }
}
