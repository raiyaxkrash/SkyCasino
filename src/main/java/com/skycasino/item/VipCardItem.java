package com.skycasino.item;

import com.skycasino.economy.ChipEconomy;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VipCardItem extends Item {
    public static final String TAG_BALANCE = "ChipBalance";

    public VipCardItem(Properties properties) {
        super(properties);
    }

    public static long getBalance(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? Math.max(0L, tag.getLong(TAG_BALANCE)) : 0L;
    }

    public static void setBalance(ItemStack stack, long balance) {
        stack.getOrCreateTag().putLong(TAG_BALANCE, Math.max(0L, balance));
    }

    public static void addBalance(ItemStack stack, long delta) {
        setBalance(stack, getBalance(stack) + delta);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getBalance(stack) > 0;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(ChatFormatting.GOLD);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack card = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            long loose = ChipEconomy.getLooseChips(sp);
            if (loose > 0) {
                ChipEconomy.clearLooseChips(sp);
                addBalance(card, loose);
                sp.displayClientMessage(
                        Component.translatable("msg.skycasino.vip_deposited",
                                ChipEconomy.formatChips(loose),
                                ChipEconomy.formatChips(getBalance(card)))
                                .withStyle(ChatFormatting.GOLD),
                        true
                );
                level.playSound(null, sp.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.3F);
            } else {
                sp.displayClientMessage(
                        Component.translatable("msg.skycasino.vip_balance",
                                ChipEconomy.formatChips(getBalance(card)))
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
            }
        }
        return InteractionResultHolder.sidedSuccess(card, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.skycasino.vip_balance",
                ChipEconomy.formatChips(getBalance(stack))).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.skycasino.vip_hint").withStyle(ChatFormatting.GRAY));
    }
}
