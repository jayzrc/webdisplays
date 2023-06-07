package net.montoyo.wd.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.montoyo.wd.block.BlockKeyboardLeft;
import net.montoyo.wd.block.BlockKeyboardRight;
import net.montoyo.wd.block.BlockPeripheral;
import net.montoyo.wd.block.BlockScreen;
import net.montoyo.wd.core.DefaultPeripheral;

public class BlockInit {

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
    }

    public static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "webdisplays");

    public static final RegistryObject<BlockScreen> blockScreen = BLOCKS.register("screen", () -> new BlockScreen(BlockBehaviour.Properties.of(Material.STONE)));

    public static final RegistryObject<BlockKeyboardLeft> blockKeyBoard = BlockInit.BLOCKS.register("kb_left", BlockKeyboardLeft::new);
    public static final RegistryObject<BlockKeyboardRight> blockKbRight = BLOCKS.register("kb_right", BlockKeyboardRight::new);

    public static final RegistryObject<BlockPeripheral> blockRedControl = BlockInit.BLOCKS.register("redctrl", () -> new BlockPeripheral(DefaultPeripheral.REDSTONE_CONTROLLER));
    public static final RegistryObject<BlockPeripheral> blockRControl = BlockInit.BLOCKS.register("rctrl", () -> new BlockPeripheral(DefaultPeripheral.REMOTE_CONTROLLER));
    public static final RegistryObject<BlockPeripheral> blockServer = BlockInit.BLOCKS.register("server", () -> new BlockPeripheral(DefaultPeripheral.SERVER));
}
