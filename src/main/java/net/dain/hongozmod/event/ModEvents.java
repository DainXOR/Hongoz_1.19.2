package net.dain.hongozmod.event;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.ModEntityTypes;
import net.dain.hongozmod.entity.custom.ZhongoEntity;
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
            event.put(ModEntityTypes.ZHONGO.get(), ZhongoEntity.setAttributes());
        }
    }
}
