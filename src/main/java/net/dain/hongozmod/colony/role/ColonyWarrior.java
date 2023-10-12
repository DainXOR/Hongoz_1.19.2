package net.dain.hongozmod.colony.role;

import net.minecraft.core.BlockPos;

import javax.swing.text.html.parser.Entity;

public interface ColonyWarrior extends ColonyMember {
    void patrol(BlockPos pos, int radius);
    void patrol(Entity entity, int radius);
}
