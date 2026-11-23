package com.skycasino.item;

import com.skycasino.client.ClientScreenOpener;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LootCaseItem extends Item {
    private final boolean vip;

    public LootCaseItem(boolean vip, Properties properties) {
        super(properties);
        this.vip = vip;
    }

    public boolean isVip() {
        return vip;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return vip;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(vip ? ChatFormatting.GOLD : ChatFormatting.AQUA);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientScreenOpener.openLootCase(vip, player.blockPosition()));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(vip ? "tooltip.skycasino.case_vip" : "tooltip.skycasino.case_classic")
                .withStyle(vip ? ChatFormatting.GOLD : ChatFormatting.GRAY));
    }
}
