package com.skycasino.item;

import com.skycasino.economy.ChipEconomy;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChipItem extends Item {
    private final long value;
    private final ChatFormatting color;

    public ChipItem(long value, ChatFormatting color, Properties properties) {
        super(properties);
        this.value = value;
        this.color = color;
    }

    public long getValue() {
        return value;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(color);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.skycasino.chip_value", ChipEconomy.formatChips(value))
                .withStyle(ChatFormatting.GOLD));
        if (stack.getCount() > 1) {
            long stackTotal = value * stack.getCount();
            tooltip.add(Component.translatable("tooltip.skycasino.chip_stack_total", ChipEconomy.formatChips(stackTotal))
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
