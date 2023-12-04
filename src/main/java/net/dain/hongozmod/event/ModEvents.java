package net.dain.hongozmod.event;

import net.dain.hongozmod.HongozMod;
import net.dain.hongozmod.entity.ModEntityTypes;
import net.dain.hongozmod.entity.custom.*;
import net.dain.hongozmod.entity.custom.hunter.HunterEntity;
import net.dain.hongozmod.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ModEvents {
    @Mod.EventBusSubscriber(modid = HongozMod.MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void anvilEvent(AnvilUpdateEvent event){
            if(event.getLeft().is(ModItems.RAW_WOLFRAMITE.get()) && event.getRight().is(Items.COAL)){
                ItemStack result = ModItems.WOLFRAMITE_DUST.get().getDefaultInstance();
                result.setCount(2);
                event.setOutput(result);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = HongozMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {

        @SubscribeEvent
        public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.FUNGI_EGG.get(), FungiEgg.setAttributes());
            event.put(ModEntityTypes.MAGGOT.get(), Maggot.setAttributes());
            event.put(ModEntityTypes.BEACON.get(), BeaconEntity.setAttributes());

            event.put(ModEntityTypes.ZHONGO.get(), ZhongoEntity.setAttributes());
            event.put(ModEntityTypes.HORDEN.get(), HordenEntity.setAttributes());
            event.put(ModEntityTypes.CROAKTAR.get(), CroaktarEntity.setAttributes());
            event.put(ModEntityTypes.HONZIADE.get(), HonziadeEntity.setAttributes());
            event.put(ModEntityTypes.HUNTER.get(), HunterEntity.setAttributes());

            event.put(ModEntityTypes.EVO_CROAKTAR.get(), EvoCroaktar.setAttributes());
            event.put(ModEntityTypes.HONZIADE_QUEEN.get(), HonziadeEntity.Queen.setAttributes());

        }



    }
}
