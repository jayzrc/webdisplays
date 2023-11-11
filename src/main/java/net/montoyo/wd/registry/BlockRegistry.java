package net.montoyo.wd.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.montoyo.wd.block.BlockKeyboardLeft;
import net.montoyo.wd.block.BlockKeyboardRight;
import net.montoyo.wd.block.BlockPeripheral;
import net.montoyo.wd.block.BlockScreen;
import net.montoyo.wd.core.DefaultPeripheral;

public class BlockRegistry {
    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
    }

    public static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "webdisplays");

    public static final RegistryObject<BlockScreen> SCREEN_BLOCk = BLOCKS.register("screen", () -> new BlockScreen(BlockBehaviour.Properties.copy(Blocks.STONE)));

    public static final RegistryObject<BlockKeyboardLeft> KEYBOARD_BLOCK = BlockRegistry.BLOCKS.register("kb_left", BlockKeyboardLeft::new);
    public static final RegistryObject<BlockKeyboardRight> blockKbRight = BLOCKS.register("kb_right", BlockKeyboardRight::new);

    public static final RegistryObject<BlockPeripheral> REDSTONE_CONTROL_BLOCK = BlockRegistry.BLOCKS.register("redctrl", () -> new BlockPeripheral(DefaultPeripheral.REDSTONE_CONTROLLER));
    public static final RegistryObject<BlockPeripheral> REMOTE_CONTROLLER_BLOCK = BlockRegistry.BLOCKS.register("rctrl", () -> new BlockPeripheral(DefaultPeripheral.REMOTE_CONTROLLER));
    public static final RegistryObject<BlockPeripheral> SERVER_BLOCK = BlockRegistry.BLOCKS.register("server", () -> new BlockPeripheral(DefaultPeripheral.SERVER));
}
