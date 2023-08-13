package net.dain.hongozmod.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
/*
public class FungiEggC2SPacket {
    public FungiEggC2SPacket(){}

    public FungiEggC2SPacket(FriendlyByteBuf buf){}

    public void toBytes(FriendlyByteBuf buf){

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context= supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = player.getLevel();
        });
        return true;
    }


}
*/