package squeek.veganoption.items;

import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import squeek.veganoption.ModInfo;
import squeek.veganoption.helpers.BlockHelper;
import squeek.veganoption.helpers.FluidHelper;

public class ItemWashableWheat extends Item
{
	public static IIcon flourIcon;
	public static IIcon doughIcon;
	public static IIcon partiallyWashedIcon;
	public static IIcon fullyWashedIcon;
	public static final int META_FLOUR = 0;
	public static final int META_DOUGH = META_FLOUR + 1;
	public static final int META_UNWASHED_START = META_DOUGH + 1;
	public static final int NUM_WASHES_NEEDED = 4;
	public static final int META_UNWASHED_END = META_UNWASHED_START + NUM_WASHES_NEEDED;
	public static final int META_RAW = META_UNWASHED_END;

	public ItemWashableWheat()
	{
		super();
		setHasSubtypes(true);
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem)
	{
		tryWash(entityItem);
		return super.onEntityItemUpdate(entityItem);
	}

	public static boolean isReadyToCook(ItemStack itemStack)
	{
		return getPercentWashed(itemStack) >= 1;
	}

	public static boolean isUnwashed(ItemStack itemStack)
	{
		int meta = itemStack.getItemDamage();
		return meta >= META_UNWASHED_START && meta < META_UNWASHED_END;
	}

	public static ItemStack wash(ItemStack itemStack, int amount)
	{
		int newMeta = Math.min(META_RAW, itemStack.getItemDamage() + amount);
		itemStack.setItemDamage(newMeta);
		return itemStack;
	}

	public static float getPercentWashed(ItemStack itemStack)
	{
		return (float) (itemStack.getItemDamage() - META_UNWASHED_START) / NUM_WASHES_NEEDED;
	}

	@Override
	public boolean showDurabilityBar(ItemStack itemStack)
	{
		return isUnwashed(itemStack);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack itemStack)
	{
		return 1.0f - getPercentWashed(itemStack);
	}

	public static boolean tryWash(EntityItem entityItem)
	{
		if (entityItem == null || entityItem.worldObj.isRemote || entityItem.getEntityItem() == null)
			return false;

		if (!isReadyToCook(entityItem.getEntityItem()))
		{
			BlockHelper.BlockPos fluidBlockPos = BlockHelper.blockPos(entityItem.worldObj, MathHelper.floor_double(entityItem.posX), MathHelper.floor_double(entityItem.posY), MathHelper.floor_double(entityItem.posZ));
			FluidStack consumedFluid = FluidHelper.consumeExactFluid(fluidBlockPos, FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME);

			if (consumedFluid != null)
			{
				EntityItem entityItemToWash = entityItem;
				ItemStack doughToWash = entityItemToWash.getEntityItem();

				if (entityItemToWash.getEntityItem().stackSize > 1)
				{
					doughToWash = entityItem.getEntityItem().splitStack(1);
					entityItemToWash = new EntityItem(entityItemToWash.worldObj, entityItemToWash.posX, entityItemToWash.posY, entityItemToWash.posZ, doughToWash);
					entityItemToWash.delayBeforeCanPickup = 10;
					entityItemToWash.worldObj.spawnEntityInWorld(entityItemToWash);
				}

				ItemStack washedItemStack = wash(doughToWash, 1);
				entityItemToWash.setEntityItemStack(washedItemStack);

				return true;
			}
		}
		return false;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		String baseName = super.getUnlocalizedName(itemStack);
		switch (itemStack.getItemDamage())
		{
			case META_FLOUR:
				return baseName + ".wheatFlour";
			case META_DOUGH:
				return baseName + ".wheatDough";
			case META_RAW:
				return baseName + ".seitanRaw";
			default:
				return baseName + ".seitanRawUnwashed";
		}
	}

	@Override
	public IIcon getIconFromDamage(int meta)
	{
		switch (meta)
		{
			case META_FLOUR:
				return flourIcon;
			case META_DOUGH:
				return doughIcon;
			case META_RAW:
				return fullyWashedIcon;
			default:
				return partiallyWashedIcon;
		}
	}

	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		flourIcon = iconRegister.registerIcon(ModInfo.MODID_LOWER + ":wheat_flour");
		doughIcon = iconRegister.registerIcon(ModInfo.MODID_LOWER + ":wheat_dough");
		partiallyWashedIcon = iconRegister.registerIcon(ModInfo.MODID_LOWER + ":seitan_raw_unwashed");
		fullyWashedIcon = iconRegister.registerIcon(ModInfo.MODID_LOWER + ":seitan_raw");
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List subItems)
	{
		subItems.add(new ItemStack(item, 1, META_FLOUR));
		subItems.add(new ItemStack(item, 1, META_DOUGH));
		subItems.add(new ItemStack(item, 1, META_UNWASHED_START));
		subItems.add(new ItemStack(item, 1, META_RAW));
	}
}
