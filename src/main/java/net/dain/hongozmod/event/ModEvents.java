package net.dain.hongozmod.event;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.ModEntityTypes;
import net.dain.hongozmod.entity.custom.CroaktarEntity;
import net.dain.hongozmod.entity.custom.HonziadeEntity;
import net.dain.hongozmod.entity.custom.HordenEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ModEvents {
    @Mod.EventBusSubscriber(modid = HongozMod.MOD_ID)
    public static class ForgeEvents {

    }

    @Mod.EventBusSubscriber(modid = HongozMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {

        @SubscribeEvent
        public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.ZHONGO.get(), CroaktarEntity.setAttributes());
            event.put(ModEntityTypes.HORDEN.get(), HordenEntity.setAttributes());
            event.put(ModEntityTypes.CROAKTAR.get(), CroaktarEntity.setAttributes());
            event.put(ModEntityTypes.HONZIADE.get(), HonziadeEntity.setAttributes());
        }
    }
}
