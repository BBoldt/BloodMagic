package WayofTime.bloodmagic.item;

import java.util.List;
import java.util.Map.Entry;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import WayofTime.bloodmagic.BloodMagic;
import WayofTime.bloodmagic.api.Constants;
import WayofTime.bloodmagic.api.livingArmour.LivingArmourHandler;
import WayofTime.bloodmagic.api.livingArmour.LivingArmourUpgrade;
import WayofTime.bloodmagic.api.util.helper.NBTHelper;
import WayofTime.bloodmagic.item.armour.ItemLivingArmour;
import WayofTime.bloodmagic.livingArmour.LivingArmour;
import WayofTime.bloodmagic.util.helper.TextHelper;

public class ItemUpgradeTome extends Item
{
    public ItemUpgradeTome()
    {
        super();

        setCreativeTab(BloodMagic.tabUpgradeTome);
        setUnlocalizedName(Constants.Mod.MODID + ".upgradeTome");
        setRegistryName(Constants.BloodMagicItem.UPGRADE_TOME.getRegName());
        setHasSubtypes(true);
        setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (world.isRemote)
        {
            return stack;
        }
        LivingArmourUpgrade upgrade = this.getUpgrade(stack);
        if (upgrade == null)
        {
            return stack;
        }

        ItemStack chestStack = player.getCurrentArmor(2);
        if (chestStack != null && chestStack.getItem() instanceof ItemLivingArmour)
        {
            LivingArmour armour = ItemLivingArmour.getLivingArmour(chestStack);
            if (armour == null)
            {
                return stack;
            }

            if (armour.upgradeArmour(player, upgrade))
            {
                ItemLivingArmour.armourMap.put(chestStack, armour);
//                ((ItemLivingArmour) chestStack.getItem()).setLivingArmour(stack, armour, false);
                stack.stackSize--;
            }
        }
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item id, CreativeTabs creativeTab, List<ItemStack> list)
    {
        for (Entry<String, Integer> entry : LivingArmourHandler.upgradeMaxLevelMap.entrySet())
        {
            String key = entry.getKey();
            int maxLevel = entry.getValue();
            for (int i = 0; i < maxLevel; i++)
            {
                ItemStack stack = new ItemStack(this);
                setKey(stack, key);
                setLevel(stack, i);
                list.add(stack);
            }
        }
    }

    public LivingArmourUpgrade getUpgrade(ItemStack stack)
    {
        String key = getKey(stack);
        int level = getLevel(stack);

        return LivingArmourHandler.generateUpgradeFromKey(key, level);
    }

    public void setKey(ItemStack stack, String key)
    {
        NBTHelper.checkNBT(stack);
        NBTTagCompound tag = stack.getTagCompound();

        tag.setString("key", key);
    }

    public String getKey(ItemStack stack)
    {
        NBTHelper.checkNBT(stack);
        NBTTagCompound tag = stack.getTagCompound();

        return tag.getString("key");
    }

    public void setLevel(ItemStack stack, int level)
    {
        NBTHelper.checkNBT(stack);
        NBTTagCompound tag = stack.getTagCompound();

        tag.setInteger("level", level);
    }

    public int getLevel(ItemStack stack)
    {
        NBTHelper.checkNBT(stack);
        NBTTagCompound tag = stack.getTagCompound();

        return tag.getInteger("level");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
    {
//        tooltip.addAll(Arrays.asList(TextHelper.cutLongString(TextHelper.localizeEffect("tooltip.BloodMagic.livingArmour"))));
        LivingArmourUpgrade upgrade = this.getUpgrade(stack);
        if (upgrade != null)
        {
            tooltip.add(TextHelper.localizeEffect("tooltip.BloodMagic.livingArmour.upgrade.level", TextHelper.localize(upgrade.getUnlocalizedName()), upgrade.getUpgradeLevel() + 1));
        }
    }
}
