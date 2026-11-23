package com.skycasino.item;

import com.skycasino.ludik.LudikData;
import com.skycasino.ludik.LudikManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AntiTiltCoffeeItem extends Item {
    public AntiTiltCoffeeItem(Properties properties) {
        super(properties.food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6F).alwaysEat().build()));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(ChatFormatting.AQUA);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack res = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            sp.removeEffect(MobEffects.CONFUSION);
            sp.removeEffect(MobEffects.DARKNESS);
            sp.removeEffect(MobEffects.WEAKNESS);
            sp.removeEffect(MobEffects.DIG_SLOWDOWN);
            sp.removeEffect(MobEffects.HUNGER);
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 0));

            LudikData.setLossStreak(sp, 0);
            LudikData.setForcedWithdrawal(sp, false);
            LudikData.setAddiction(sp, LudikData.getAddiction(sp) - 35);
            LudikData.setLastBetTick(sp, level.getGameTime());

            sp.displayClientMessage(Component.literal("§b☕ Вы выпили Кофе «Анти-Тильт»! Ломка и тильт сняты."), true);
            level.playSound(null, sp.blockPosition(), SoundEvents.WANDERING_TRADER_DRINK_MILK, SoundSource.PLAYERS, 0.9F, 1.2F);
            LudikManager.syncToClient(sp);
        }
        return res;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.skycasino.coffee_hint").withStyle(ChatFormatting.GRAY));
    }
}
