package WayofTime.bloodmagic;

import java.io.File;

import WayofTime.bloodmagic.api.BloodMagicAPI;
import WayofTime.bloodmagic.compat.ICompatibility;
import WayofTime.bloodmagic.util.Utils;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import WayofTime.bloodmagic.api.Constants;
import WayofTime.bloodmagic.api.util.helper.LogHelper;
import WayofTime.bloodmagic.client.gui.GuiHandler;
import WayofTime.bloodmagic.network.BloodMagicPacketHandler;
import WayofTime.bloodmagic.proxy.CommonProxy;
import WayofTime.bloodmagic.registry.ModArmourTrackers;
import WayofTime.bloodmagic.registry.ModBlocks;
import WayofTime.bloodmagic.registry.ModCompatibility;
import WayofTime.bloodmagic.registry.ModEntities;
import WayofTime.bloodmagic.registry.ModItems;
import WayofTime.bloodmagic.registry.ModPotions;
import WayofTime.bloodmagic.registry.ModRecipes;
import WayofTime.bloodmagic.registry.ModRituals;
import WayofTime.bloodmagic.registry.ModTranquilityHandlers;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = Constants.Mod.MODID, name = Constants.Mod.NAME, version = Constants.Mod.VERSION, dependencies = Constants.Mod.DEPEND, guiFactory = "WayofTime.bloodmagic.client.gui.config.ConfigGuiFactory")
@Getter
public class BloodMagic
{
    @SidedProxy(serverSide = "WayofTime.bloodmagic.proxy.CommonProxy", clientSide = "WayofTime.bloodmagic.proxy.ClientProxy")
    public static CommonProxy proxy;

    @Mod.Instance(Constants.Mod.MODID)
    public static BloodMagic instance;

    public static CreativeTabs tabBloodMagic = new CreativeTabs(Constants.Mod.MODID + ".creativeTab")
    {
        @Override
        public Item getTabIconItem()
        {
            return ModItems.bloodOrb;
        }
    };

    public static CreativeTabs tabUpgradeTome = new CreativeTabs(Constants.Mod.MODID + ".creativeTabTome")
    {
        @Override
        public Item getTabIconItem()
        {
            return ModItems.upgradeTome;
        }
    };

    private LogHelper logger = new LogHelper(Constants.Mod.MODID);
    private File configDir;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        configDir = new File(event.getModConfigurationDirectory(), "BloodMagic");
        ConfigHandler.init(new File(getConfigDir(), "BloodMagic.cfg"));

        ModBlocks.init();
        ModItems.init();
        ModPotions.init();
        ModEntities.init();
        ModCompatibility.registerModCompat();
        ModCompatibility.loadCompat(ICompatibility.InitializationPhase.PRE_INIT);
        ModTranquilityHandlers.init();

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        BloodMagicPacketHandler.init();

        ModRecipes.init();
        ModRituals.initRituals();
        ModRituals.initImperfectRituals();
        ModArmourTrackers.init();
        ModCompatibility.loadCompat(ICompatibility.InitializationPhase.INIT);
        NetworkRegistry.INSTANCE.registerGuiHandler(BloodMagic.instance, new GuiHandler());

        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        ModRecipes.addCompressionHandlers();
        ModCompatibility.loadCompat(ICompatibility.InitializationPhase.POST_INIT);

        proxy.postInit();
    }

    @Mod.EventHandler
    public void onIMCRecieved(FMLInterModComms.IMCEvent event)
    {
        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
            if (message.key.equals("teleposerBlacklist") && message.isItemStackMessage())
            {
                ItemStack stack = message.getItemStackValue();
                if (stack.getItem() instanceof ItemBlock)
                {
                    Block block = Block.getBlockFromItem(stack.getItem());
                    BloodMagicAPI.addToTeleposerBlacklist(block, stack.getItemDamage());
                }
            }

            if (message.key.equals("sacrificeValue") && message.isStringMessage())
            {
                String[] splitInfo = message.getStringValue().split(";");
                if (splitInfo.length == 2 && Utils.isInteger(splitInfo[1]))
                    BloodMagicAPI.setEntitySacrificeValue(splitInfo[0], Integer.parseInt(splitInfo[1]));
            }

            if (message.key.equals("greenGroveBlacklist") && message.isStringMessage())
            {
                String[] splitInfo = message.getStringValue().split(":");
                if (splitInfo.length == 2)
                {
                    Block block = GameRegistry.findBlock(splitInfo[0], splitInfo[1]);
                    if (block != null)
                        BloodMagicAPI.blacklistFromGreenGrove(block);
                }
            }
        }
    }
}
