package net.dain.hongozmod.event;

import com.mojang.logging.LogUtils;
import net.dain.hongozmod.HongozMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
/*
public class ClientEvents {

    @Mod.EventBusSubscriber(modid = HongozMod.MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents{

        @SubscribeEvent
        public static void onRegister(RegisterEvent event){
            //event.register();
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event){
            LogUtils.getLogger().debug("Hmmmmmmmmmmmmm");
        }

        @SubscribeEvent
        public static void onAnimationChange(Event event){

            //if(event)
            ModNetworking.sendToServer(new FungiEggC2SPacket());
        }
    }

    @Mod.EventBusSubscriber(modid = HongozMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvent{

        @SubscribeEvent
        public static void onEventRegister(RegisterEvent event){
            //event.register();
        }
    }
}
*/