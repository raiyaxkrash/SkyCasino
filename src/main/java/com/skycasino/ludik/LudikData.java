package com.skycasino.ludik;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class LudikData {
    private static final String ROOT_TAG = "SkyCasinoLudik";

    public static CompoundTag getTag(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_TAG)) {
            persistent.put(ROOT_TAG, new CompoundTag());
        }
        return persistent.getCompound(ROOT_TAG);
    }

    public static int getAddiction(ServerPlayer p) { return getTag(p).getInt("Addiction"); }
    public static void setAddiction(ServerPlayer p, int v) { getTag(p).putInt("Addiction", Math.max(0, Math.min(100, v))); }

    public static long getLastBetTick(ServerPlayer p) { return getTag(p).getLong("LastBetTick"); }
    public static void setLastBetTick(ServerPlayer p, long t) { getTag(p).putLong("LastBetTick", t); }

    public static int getWinStreak(ServerPlayer p) { return getTag(p).getInt("WinStreak"); }
    public static void setWinStreak(ServerPlayer p, int v) { getTag(p).putInt("WinStreak", Math.max(0, v)); }

    public static int getLossStreak(ServerPlayer p) { return getTag(p).getInt("LossStreak"); }
    public static void setLossStreak(ServerPlayer p, int v) { getTag(p).putInt("LossStreak", Math.max(0, v)); }

    public static long getDebt(ServerPlayer p) { return getTag(p).getLong("Debt"); }
    public static void setDebt(ServerPlayer p, long v) { getTag(p).putLong("Debt", Math.max(0L, v)); }

    public static long getDebtDueTick(ServerPlayer p) { return getTag(p).getLong("DebtDueTick"); }
    public static void setDebtDueTick(ServerPlayer p, long t) { getTag(p).putLong("DebtDueTick", t); }

    public static long getLastDodepTick(ServerPlayer p) { return getTag(p).getLong("LastDodepTick"); }
    public static void setLastDodepTick(ServerPlayer p, long t) { getTag(p).putLong("LastDodepTick", t); }

    public static int getTotalSpins(ServerPlayer p) { return getTag(p).getInt("TotalSpins"); }
    public static void incTotalSpins(ServerPlayer p) { getTag(p).putInt("TotalSpins", getTotalSpins(p) + 1); }

    public static boolean isForcedWithdrawal(ServerPlayer p) { return getTag(p).getBoolean("ForcedWithdrawal"); }
    public static void setForcedWithdrawal(ServerPlayer p, boolean b) { getTag(p).putBoolean("ForcedWithdrawal", b); }

    public static boolean isInWithdrawal(ServerPlayer player) {
        if (isForcedWithdrawal(player)) return true;
        int addiction = getAddiction(player);
        if (addiction < 10) return false;
        long now = player.level().getGameTime();
        long last = getLastBetTick(player);
        if (last <= 0) return false;
        // High addiction triggers withdrawal faster: 2 min at 60+, 3 min at 10-59
        long threshold = addiction >= 60 ? 2400L : 3600L;
        return (now - last) >= threshold;
    }

    public static int getWithdrawalStage(ServerPlayer player) {
        if (!isInWithdrawal(player)) return 0;
        if (isForcedWithdrawal(player)) return 2;
        long elapsed = player.level().getGameTime() - getLastBetTick(player);
        return elapsed >= 6000L ? 2 : 1; // Stage 2 after 5 min
    }

    public static double getHotHandBonus(ServerPlayer player) {
        int ws = getWinStreak(player);
        if (ws >= 7) return 0.60; // +60%!
        if (ws >= 5) return 0.40; // +40%!
        if (ws >= 3) return 0.25; // +25%!
        return 0.0;
    }
}
