package squeek.veganoption.integration.mfr;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import squeek.veganoption.content.Modifiers;
import squeek.veganoption.content.modules.Composting;
import squeek.veganoption.integration.IntegrationHandler;
import squeek.veganoption.integration.IntegratorBase;
import cpw.mods.fml.common.event.FMLInterModComms;

public class MineFactoryReloaded extends IntegratorBase
{
	@Override
	public void recipes()
	{
		Item milkBottleItem = getItem("milkbottle");
		if (milkBottleItem != null)
		{
			// milk bottle can't use oredict because it's included in the ingredient's 
			// oredict, thus allowing infinite duping
			//
			// don't add a replacement recipe because milk's curative properties are
			// found in VO's soap instead of plant milk, and the only purpose of the milk bottle is
			// the curative effect
			Modifiers.recipes.excludeOutput(new ItemStack(milkBottleItem));
		}
	}

	@Override
	public void init()
	{
		super.init();
		registerFertilizer(new ItemStack(Composting.fertilizer), FertilizerType.GrowPlant);
	}

	// copied from powercrystals.minefactoryreloaded.api.FertilizerType
	public static enum FertilizerType
	{
		/**
		* The fertilizer will fertilize nothing.
		*/
		None,
		/**
		* The fertilizer will fertilize grass.
		*/
		Grass,
		/**
		* The fertilizer will grow a plant.
		*/
		GrowPlant,
		/**
		* The fertilizer will grow magical crops.
		*/
		GrowMagicalCrop,
	}

	/**
	 * registerFertilizer_Standard | An NBTTag with the fert (Item, String identifier), meta (Integer), and
	 * type (Integer, index into FertilizerType.values()) attributes set.
	 */
	public void registerFertilizer(ItemStack itemStack, FertilizerType type)
	{
		NBTTagCompound toSend = new NBTTagCompound();

		toSend.setString("fert", Item.itemRegistry.getNameForObject(itemStack.getItem()));
		toSend.setInteger("meta", itemStack.getItemDamage());
		toSend.setInteger("type", type.ordinal());

		FMLInterModComms.sendMessage(IntegrationHandler.MODID_MINEFACTORY_RELOADED, "registerFertilizer_Standard", toSend);
	}
}
