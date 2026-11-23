package com.skycasino.network;

import com.skycasino.SkyCasinoMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SkyCasinoMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int nextId() {
        return packetId++;
    }

    public static void register() {
        CHANNEL.messageBuilder(CashierActionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(CashierActionPacket::decode)
                .encoder(CashierActionPacket::encode)
                .consumerMainThread(CashierActionPacket::handle)
                .add();

        CHANNEL.messageBuilder(SlotSpinRequestPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SlotSpinRequestPacket::decode)
                .encoder(SlotSpinRequestPacket::encode)
                .consumerMainThread(SlotSpinRequestPacket::handle)
                .add();

        CHANNEL.messageBuilder(SlotSpinResultPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SlotSpinResultPacket::decode)
                .encoder(SlotSpinResultPacket::encode)
                .consumerMainThread(SlotSpinResultPacket::handle)
                .add();

        CHANNEL.messageBuilder(BlackjackActionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(BlackjackActionPacket::decode)
                .encoder(BlackjackActionPacket::encode)
                .consumerMainThread(BlackjackActionPacket::handle)
                .add();

        CHANNEL.messageBuilder(BlackjackStatePacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BlackjackStatePacket::decode)
                .encoder(BlackjackStatePacket::encode)
                .consumerMainThread(BlackjackStatePacket::handle)
                .add();

        CHANNEL.messageBuilder(RouletteSpinRequestPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RouletteSpinRequestPacket::decode)
                .encoder(RouletteSpinRequestPacket::encode)
                .consumerMainThread(RouletteSpinRequestPacket::handle)
                .add();

        CHANNEL.messageBuilder(RouletteSpinResultPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RouletteSpinResultPacket::decode)
                .encoder(RouletteSpinResultPacket::encode)
                .consumerMainThread(RouletteSpinResultPacket::handle)
                .add();

        CHANNEL.messageBuilder(FortuneWheelSpinRequestPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(FortuneWheelSpinRequestPacket::decode)
                .encoder(FortuneWheelSpinRequestPacket::encode)
                .consumerMainThread(FortuneWheelSpinRequestPacket::handle)
                .add();

        CHANNEL.messageBuilder(FortuneWheelSpinResultPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(FortuneWheelSpinResultPacket::decode)
                .encoder(FortuneWheelSpinResultPacket::encode)
                .consumerMainThread(FortuneWheelSpinResultPacket::handle)
                .add();

        CHANNEL.messageBuilder(CrashActionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(CrashActionPacket::decode)
                .encoder(CrashActionPacket::encode)
                .consumerMainThread(CrashActionPacket::handle)
                .add();

        CHANNEL.messageBuilder(CrashStatePacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CrashStatePacket::decode)
                .encoder(CrashStatePacket::encode)
                .consumerMainThread(CrashStatePacket::handle)
                .add();

        CHANNEL.messageBuilder(MinesActionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MinesActionPacket::decode)
                .encoder(MinesActionPacket::encode)
                .consumerMainThread(MinesActionPacket::handle)
                .add();

        CHANNEL.messageBuilder(MinesStatePacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MinesStatePacket::decode)
                .encoder(MinesStatePacket::encode)
                .consumerMainThread(MinesStatePacket::handle)
                .add();

        CHANNEL.messageBuilder(PrizeShopBuyPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PrizeShopBuyPacket::decode)
                .encoder(PrizeShopBuyPacket::encode)
                .consumerMainThread(PrizeShopBuyPacket::handle)
                .add();

        CHANNEL.messageBuilder(LootCaseOpenRequestPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(LootCaseOpenRequestPacket::decode)
                .encoder(LootCaseOpenRequestPacket::encode)
                .consumerMainThread(LootCaseOpenRequestPacket::handle)
                .add();

        CHANNEL.messageBuilder(LootCaseOpenResultPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(LootCaseOpenResultPacket::decode)
                .encoder(LootCaseOpenResultPacket::encode)
                .consumerMainThread(LootCaseOpenResultPacket::handle)
                .add();

        CHANNEL.messageBuilder(LudikActionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(LudikActionPacket::decode)
                .encoder(LudikActionPacket::encode)
                .consumerMainThread(LudikActionPacket::handle)
                .add();

        CHANNEL.messageBuilder(LudikSyncPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(LudikSyncPacket::decode)
                .encoder(LudikSyncPacket::encode)
                .consumerMainThread(LudikSyncPacket::handle)
                .add();
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}
