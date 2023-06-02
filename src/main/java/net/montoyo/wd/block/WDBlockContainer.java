/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.BaseEntityBlock;

public abstract class WDBlockContainer extends BaseEntityBlock {

    protected static BlockItem itemBlock;

    public WDBlockContainer(Properties arg) {
        super(arg);
    }

    public BlockItem getItem() {
        return itemBlock;
    }

}
