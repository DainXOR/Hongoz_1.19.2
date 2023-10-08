package net.dain.hongozmod.colony.role;

import net.dain.hongozmod.colony.PlaceTag;
import net.minecraft.core.BlockPos;

public interface ColonyExplorer extends ColonyMember {
    void rememberPlace(BlockPos pos, PlaceTag tag);
}
