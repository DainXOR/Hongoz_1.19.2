package net.dain.hongozmod.entity.templates;

import software.bernie.geckolib3.core.builder.ILoopType;

public enum LoopType {
    LOOP(ILoopType.EDefaultLoopTypes.LOOP),
    PLAY_ONCE(ILoopType.EDefaultLoopTypes.PLAY_ONCE),
    HOLD_ON_LAST_FRAME(ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME);

    private final ILoopType.EDefaultLoopTypes loopType;

    LoopType(ILoopType.EDefaultLoopTypes loop) {
        this.loopType = loop;
    }

    public ILoopType.EDefaultLoopTypes get() {
        return loopType;
    }
}
