package net.dain.hongozmod.ai;

import net.dain.hongozmod.ai.custom.HunterEntitySensor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.function.Supplier;

public class ModSensorType<U extends Sensor<?>> {

    public static final ModSensorType<HunterEntitySensor> HUNTER_ENTITY_SENSOR = register("hunter_entity_sensor", HunterEntitySensor::new);

    private final Supplier<U> factory;

    public ModSensorType(Supplier<U> factory) {
        this.factory = factory;
        Sensing
    }

    public U create() {
        return this.factory.get();
    }

    private static <U extends Sensor<?>> ModSensorType<U> register(String pKey, Supplier<U> pSensorSupplier) {
        return Registry.register(Registry.SENSOR_TYPE_REGISTRY, new ResourceLocation(pKey), new ModSensorType(pSensorSupplier));
    }
}
