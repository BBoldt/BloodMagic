package WayofTime.bloodmagic.util.handler;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import WayofTime.bloodmagic.ConfigHandler;
import WayofTime.bloodmagic.api.BloodMagicAPI;
import WayofTime.bloodmagic.api.Constants;
import WayofTime.bloodmagic.api.event.ItemBindEvent;
import WayofTime.bloodmagic.api.event.SacrificeKnifeUsedEvent;
import WayofTime.bloodmagic.api.event.TeleposeEvent;
import WayofTime.bloodmagic.api.iface.IBindable;
import WayofTime.bloodmagic.api.livingArmour.LivingArmourUpgrade;
import WayofTime.bloodmagic.api.soul.IDemonWill;
import WayofTime.bloodmagic.api.soul.IDemonWillWeapon;
import WayofTime.bloodmagic.api.soul.PlayerDemonWillHandler;
import WayofTime.bloodmagic.api.util.helper.BindableHelper;
import WayofTime.bloodmagic.api.util.helper.NBTHelper;
import WayofTime.bloodmagic.api.util.helper.PlayerHelper;
import WayofTime.bloodmagic.block.BlockAltar;
import WayofTime.bloodmagic.entity.projectile.EntitySentientArrow;
import WayofTime.bloodmagic.item.ItemAltarMaker;
import WayofTime.bloodmagic.item.armour.ItemLivingArmour;
import WayofTime.bloodmagic.item.gear.ItemPackSacrifice;
import WayofTime.bloodmagic.livingArmour.LivingArmour;
import WayofTime.bloodmagic.livingArmour.tracker.StatTrackerArrowShot;
import WayofTime.bloodmagic.livingArmour.tracker.StatTrackerDigging;
import WayofTime.bloodmagic.livingArmour.tracker.StatTrackerHealthboost;
import WayofTime.bloodmagic.livingArmour.tracker.StatTrackerMeleeDamage;
import WayofTime.bloodmagic.livingArmour.tracker.StatTrackerPhysicalProtect;
import WayofTime.bloodmagic.livingArmour.tracker.StatTrackerSelfSacrifice;
import WayofTime.bloodmagic.livingArmour.upgrade.LivingArmourUpgradeArrowShot;
import WayofTime.bloodmagic.livingArmour.upgrade.LivingArmourUpgradeDigging;
import WayofTime.bloodmagic.livingArmour.upgrade.LivingArmourUpgradeSelfSacrifice;
import WayofTime.bloodmagic.registry.ModBlocks;
import WayofTime.bloodmagic.registry.ModItems;
import WayofTime.bloodmagic.registry.ModPotions;
import WayofTime.bloodmagic.util.ChatUtil;
import WayofTime.bloodmagic.util.Utils;
import WayofTime.bloodmagic.util.helper.TextHelper;

import com.google.common.base.Strings;

public class EventHandler
{
    Random random = new Random();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event)
    {
        if (event.entityLiving instanceof EntityPlayer)
        {
            EntityPlayer entityPlayer = (EntityPlayer) event.entityLiving;
            if (event.entityLiving.isPotionActive(ModPotions.boost))
            {
                entityPlayer.stepHeight = 1.0f;
            } else
            {
                entityPlayer.stepHeight = 0.5f;
            }
        }
    }

    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event)
    {
        int chestIndex = 2;

        if (event.entity.worldObj.isRemote)
            return;

        if (event.source.getEntity() instanceof EntityPlayer && !PlayerHelper.isFakePlayer((EntityPlayer) event.source.getEntity()))
        {
            EntityPlayer player = (EntityPlayer) event.source.getEntity();

            if (player.getCurrentArmor(chestIndex) != null && player.getCurrentArmor(chestIndex).getItem() instanceof ItemPackSacrifice)
            {
                ItemPackSacrifice pack = (ItemPackSacrifice) player.getCurrentArmor(chestIndex).getItem();

                boolean shouldSyphon = pack.getStoredLP(player.getCurrentArmor(chestIndex)) < pack.CAPACITY;
                float damageDone = event.entityLiving.getHealth() < event.ammount ? event.ammount - event.entityLiving.getHealth() : event.ammount;
                int totalLP = Math.round(damageDone * ConfigHandler.sacrificialPackConversion);

                if (shouldSyphon)
                    pack.addLP(player.getCurrentArmor(chestIndex), totalLP);
            }
        }
    }

    @SubscribeEvent
    public void onAnvil(AnvilUpdateEvent event)
    {
        if (ConfigHandler.thaumcraftGogglesUpgrade)
        {
            if (event.left.getItem() == ModItems.livingArmourHelmet && event.right.getItem() == Constants.Compat.THAUMCRAFT_GOGGLES && !event.right.isItemDamaged())
            {
                ItemStack output = event.left.copy();
                output = NBTHelper.checkNBT(output);
                output.getTagCompound().setBoolean(Constants.Compat.THAUMCRAFT_HAS_GOGGLES, true);
                event.cost = 1;

                event.output = output;
            }
        }
    }

    @SubscribeEvent
    public void onBucketFill(FillBucketEvent event)
    {
        if (event.current.getItem() != Items.bucket)
            return;

        ItemStack result = null;

        Block block = event.world.getBlockState(event.target.getBlockPos()).getBlock();

        if (block != null && (block.equals(ModBlocks.lifeEssence)) && block.getMetaFromState(event.world.getBlockState(event.target.getBlockPos())) == 0)
        {
            event.world.setBlockToAir(event.target.getBlockPos());
            result = new ItemStack(ModItems.bucketEssence);
        }

        if (result == null)
            return;

        event.result = result;
        event.setResult(Event.Result.ALLOW);
    }

    @SubscribeEvent
    public void harvestEvent(PlayerEvent.HarvestCheck event)
    {
        if (event.block != null && event.block instanceof BlockAltar && event.entityPlayer != null && event.entityPlayer instanceof EntityPlayerMP && event.entityPlayer.getCurrentEquippedItem() != null && event.entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemAltarMaker)
        {
            ItemAltarMaker altarMaker = (ItemAltarMaker) event.entityPlayer.getCurrentEquippedItem().getItem();
            ChatUtil.sendNoSpam(event.entityPlayer, TextHelper.localizeEffect("chat.BloodMagic.altarMaker.destroy", altarMaker.destroyAltar(event.entityPlayer)));
        }
    }

    @SubscribeEvent
    public void onTelepose(TeleposeEvent event)
    {
        if (ConfigHandler.teleposerBlacklist.contains(event.initialStack) || ConfigHandler.teleposerBlacklist.contains(event.finalStack))
            event.setCanceled(true);

        if (BloodMagicAPI.getTeleposerBlacklist().contains(event.initialStack) || BloodMagicAPI.getTeleposerBlacklist().contains(event.finalStack))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent event)
    {
        if (event.modID.equals(Constants.Mod.MODID))
            ConfigHandler.syncConfig();
    }

    @SubscribeEvent
    public void blockBreakEvent(BlockEvent.BreakEvent event)
    {
        EntityPlayer player = event.getPlayer();
        if (player != null)
        {
            if (LivingArmour.hasFullSet(player))
            {
                ItemStack chestStack = player.getCurrentArmor(2);
                if (chestStack != null && chestStack.getItem() instanceof ItemLivingArmour)
                {
                    LivingArmour armour = ItemLivingArmour.armourMap.get(chestStack);

                    if (armour != null)
                    {
                        StatTrackerDigging.incrementCounter(armour);
                        LivingArmourUpgradeDigging.hasDug(armour);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void interactEvent(PlayerInteractEvent event)
    {
        if (event.world.isRemote)
            return;

        EntityPlayer player = event.entityPlayer;

        if (PlayerHelper.isFakePlayer(player))
            return;

        if (event.useBlock == Result.DENY && event.useItem != Result.DENY)
        {
            ItemStack held = player.getHeldItem();
            if (held != null && held.getItem() instanceof IBindable)
            {
                held = NBTHelper.checkNBT(held);
                IBindable bindable = (IBindable) held.getItem();
                if (Strings.isNullOrEmpty(bindable.getOwnerUUID(held)))
                {
                    if (bindable.onBind(player, held))
                    {
                        String uuid = PlayerHelper.getUUIDFromPlayer(player).toString();
                        ItemBindEvent toPost = new ItemBindEvent(player, uuid, held);
                        if (MinecraftForge.EVENT_BUS.post(toPost) || toPost.getResult() == Result.DENY)
                            return;

                        BindableHelper.setItemOwnerUUID(held, uuid);
                        BindableHelper.setItemOwnerName(held, player.getDisplayNameString());
                    }
                } else if (bindable.getOwnerUUID(held).equals(PlayerHelper.getUUIDFromPlayer(player).toString()) && !bindable.getOwnerName(held).equals(player.getDisplayNameString()))
                    BindableHelper.setItemOwnerName(held, player.getDisplayNameString());
            }
        }
    }

    @SubscribeEvent
    public void selfSacrificeEvent(SacrificeKnifeUsedEvent event)
    {
        EntityPlayer player = event.player;

        if (LivingArmour.hasFullSet(player))
        {
            ItemStack chestStack = player.getCurrentArmor(2);
            LivingArmour armour = ItemLivingArmour.armourMap.get(chestStack);
            if (armour != null)
            {
                StatTrackerSelfSacrifice.incrementCounter(armour);
                LivingArmourUpgrade upgrade = ItemLivingArmour.getUpgrade(Constants.Mod.MODID + ".upgrade.selfSacrifice", chestStack);

                if (upgrade instanceof LivingArmourUpgradeSelfSacrifice)
                {
                    double modifier = ((LivingArmourUpgradeSelfSacrifice) upgrade).getSacrificeModifier();

                    event.lpAdded = (int) (event.lpAdded * (1 + modifier));
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityHealed(LivingHealEvent event)
    {
        EntityLivingBase healedEntity = event.entityLiving;
        if (!(healedEntity instanceof EntityPlayer))
        {
            return;
        }

        EntityPlayer player = (EntityPlayer) healedEntity;

        if (LivingArmour.hasFullSet(player))
        {
            ItemStack chestStack = player.getCurrentArmor(2);
            LivingArmour armour = ItemLivingArmour.armourMap.get(chestStack);
            if (armour != null)
                StatTrackerHealthboost.incrementCounter(armour, event.amount);
        }
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingAttackEvent event)
    {
        DamageSource source = event.source;
        Entity sourceEntity = event.source.getEntity();
        EntityLivingBase attackedEntity = event.entityLiving;

        if (attackedEntity.hurtResistantTime > 0)
        {
            return;
        }

        if (attackedEntity instanceof EntityPlayer)
        {
            EntityPlayer attackedPlayer = (EntityPlayer) attackedEntity;

            // Living Armor Handling
            if (LivingArmour.hasFullSet(attackedPlayer))
            {
                float amount = Math.min(Utils.getModifiedDamage(attackedPlayer, event.source, event.ammount), attackedPlayer.getHealth());
                ItemStack chestStack = attackedPlayer.getCurrentArmor(2);
                LivingArmour armour = ItemLivingArmour.armourMap.get(chestStack);
                if (armour != null)
                {
                    if (sourceEntity != null && !source.isMagicDamage())
                    {
                        // Add resistance to the upgrade that protects against non-magic damage
                        StatTrackerPhysicalProtect.incrementCounter(armour, amount);
                    }
                }
            }
        }

        if (sourceEntity instanceof EntitySentientArrow)
        {
            // Soul Weapon handling
            ((EntitySentientArrow) sourceEntity).reimbursePlayer();
        }

        if (sourceEntity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) sourceEntity;

            // Living Armor Handling
            if (LivingArmour.hasFullSet(player))
            {
                float amount = Math.min(Utils.getModifiedDamage(attackedEntity, event.source, event.ammount), attackedEntity.getHealth());
                ItemStack chestStack = player.getCurrentArmor(2);
                LivingArmour armour = ItemLivingArmour.armourMap.get(chestStack);
                if (armour != null)
                {
                    if (!source.isProjectile())
                    {
                        StatTrackerMeleeDamage.incrementCounter(armour, amount);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onArrowFire(ArrowLooseEvent event)
    {
        World world = event.entityPlayer.worldObj;
        ItemStack stack = event.bow;
        EntityPlayer player = event.entityPlayer;

        if (LivingArmour.hasFullSet(player))
        {
            ItemStack chestStack = player.getCurrentArmor(2);
            LivingArmour armour = ItemLivingArmour.armourMap.get(chestStack);
            if (armour != null)
            {
                StatTrackerArrowShot.incrementCounter(armour);

                LivingArmourUpgrade upgrade = ItemLivingArmour.getUpgrade(Constants.Mod.MODID + ".upgrade.arrowShot", chestStack);
                if (upgrade instanceof LivingArmourUpgradeArrowShot)
                {
                    int i = event.charge;
                    float f = (float) i / 20.0F;
                    f = (f * f + f * 2.0F) / 3.0F;

                    if ((double) f < 0.1D)
                    {
                        return;
                    }

                    if (f > 1.0F)
                    {
                        f = 1.0F;
                    }

                    int numberExtra = ((LivingArmourUpgradeArrowShot) upgrade).getExtraArrows();
                    for (int n = 0; n < numberExtra; n++)
                    {
                        EntityArrow entityarrow = new EntityArrow(world, player, f * 2.0F);

                        double velocityModifier = 0.6 * f;
                        entityarrow.motionX += (random.nextDouble() - 0.5) * velocityModifier;
                        entityarrow.motionY += (random.nextDouble() - 0.5) * velocityModifier;
                        entityarrow.motionZ += (random.nextDouble() - 0.5) * velocityModifier;

                        if (f == 1.0F)
                        {
                            entityarrow.setIsCritical(true);
                        }

                        int j = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);

                        if (j > 0)
                        {
                            entityarrow.setDamage(entityarrow.getDamage() + (double) j * 0.5D + 0.5D);
                        }

                        int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);

                        if (k > 0)
                        {
                            entityarrow.setKnockbackStrength(k);
                        }

                        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) > 0)
                        {
                            entityarrow.setFire(100);
                        }

                        entityarrow.canBePickedUp = 2;

                        if (!world.isRemote)
                        {
                            world.spawnEntityInWorld(entityarrow);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event)
    {
        EntityLivingBase attackedEntity = event.entityLiving;
        DamageSource source = event.source;
        Entity entity = source.getEntity();

        if (attackedEntity.isPotionActive(ModPotions.soulSnare))
        {
            PotionEffect eff = attackedEntity.getActivePotionEffect(ModPotions.soulSnare);
            int lvl = eff.getAmplifier();

            double amountOfSouls = random.nextDouble() * (lvl + 1) * (lvl + 1) * 5;
            ItemStack soulStack = ((IDemonWill) ModItems.monsterSoul).createWill(0, amountOfSouls);
            event.drops.add(new EntityItem(attackedEntity.worldObj, attackedEntity.posX, attackedEntity.posY, attackedEntity.posZ, soulStack));
        }

        if (entity != null && entity instanceof EntityLivingBase)
        {
            EntityLivingBase attackingEntity = (EntityLivingBase) entity;
            ItemStack heldStack = attackingEntity.getHeldItem();
            if (heldStack != null && heldStack.getItem() instanceof IDemonWillWeapon)
            {
                List<ItemStack> droppedSouls = ((IDemonWillWeapon) heldStack.getItem()).getRandomDemonWillDrop(attackedEntity, attackingEntity, heldStack, event.lootingLevel);
                if (!droppedSouls.isEmpty())
                {
                    for (ItemStack soulStack : droppedSouls)
                    {
                        event.drops.add(new EntityItem(attackedEntity.worldObj, attackedEntity.posX, attackedEntity.posY, attackedEntity.posZ, soulStack));
                    }
                }
            }

            if (heldStack != null && heldStack.getItem() == ModItems.boundSword && !(attackedEntity instanceof EntityAnimal))
            {
                for (int i = 0; i <= EnchantmentHelper.getLootingModifier(attackingEntity); i++)
                {
                    if (this.random.nextDouble() < 0.2)
                    {
                        event.drops.add(new EntityItem(attackedEntity.worldObj, attackedEntity.posX, attackedEntity.posY, attackedEntity.posZ, new ItemStack(ModItems.bloodShard, 1, 0)));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event)
    {
        ItemStack stack = event.item.getEntityItem();
        if (stack != null && stack.getItem() instanceof IDemonWill)
        {
            EntityPlayer player = event.entityPlayer;

            ItemStack remainder = PlayerDemonWillHandler.addDemonWill(player, stack);

            if (remainder == null || ((IDemonWill) stack.getItem()).getWill(stack) < 0.0001 || PlayerDemonWillHandler.isDemonWillFull(player))
            {
                stack.stackSize = 0;
                event.setResult(Result.ALLOW);
            }
        }
    }
}
