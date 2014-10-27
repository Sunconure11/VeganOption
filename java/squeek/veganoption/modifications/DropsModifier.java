package squeek.veganoption.modifications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.oredict.OreDictionary;
import squeek.veganoption.helpers.RandomHelper;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class DropsModifier
{
	private static final HashMap<BlockSpecifier, DropSpecifier> blockDrops = new HashMap<BlockSpecifier, DropSpecifier>();

	public DropsModifier()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void addDropsToBlock(Block block, Item drop)
	{
		addDropsToBlock(block, new ItemStack(drop));
	}

	public void addDropsToBlock(Block block, ItemStack drop)
	{
		addDropsToBlock(new BlockSpecifier(block), new DropSpecifier(drop));
	}

	public void addDropsToBlock(BlockSpecifier blockSpecifier, DropSpecifier dropSpecifier)
	{
		blockDrops.put(blockSpecifier, dropSpecifier);
	}

	public static class BlockSpecifier
	{
		public final Block block;
		public final int meta;

		public BlockSpecifier(Block block)
		{
			this(block, 0);
		}

		public BlockSpecifier(Block block, int meta)
		{
			this.block = block;
			this.meta = meta;
		}

		public boolean matches(Block block)
		{
			return matches(block, 0);
		}

		public boolean matches(Block block, int meta)
		{
			return blockMatches(block) && metaMatches(block, meta);
		}

		public boolean matches(IBlockAccess world, int x, int y, int z, Block block, int meta)
		{
			return matches(block, meta);
		}

		public boolean blockMatches(Block block)
		{
			return this.block == block;
		}

		public boolean metaMatches(int meta)
		{
			return this.meta == meta || this.meta == OreDictionary.WILDCARD_VALUE;
		}

		public boolean metaMatches(Block block, int meta)
		{
			return metaMatches(meta);
		}
	}

	public static class DropSpecifier
	{
		private final ItemStack itemStack;
		private final float dropChance;
		private final int dropsMin;
		private final int dropsMax;

		public DropSpecifier(ItemStack itemStack)
		{
			this(itemStack, 1f);
		}

		public DropSpecifier(ItemStack itemStack, float dropChance)
		{
			this(itemStack, dropChance, 1, 1);
		}

		public DropSpecifier(ItemStack itemStack, int dropsMin, int dropsMax)
		{
			this(itemStack, 1f, dropsMin, dropsMax);
		}

		public DropSpecifier(ItemStack itemStack, float dropChance, int dropsMin, int dropsMax)
		{
			this.itemStack = itemStack;
			this.dropsMin = dropsMin;
			this.dropsMax = dropsMax;
			this.dropChance = dropChance;
		}

		public boolean shouldDrop(EntityPlayer harvester, int fortuneLevel, boolean isSilkTouching)
		{
			return RandomHelper.random.nextFloat() < dropChance;
		}

		public int amountToDrop(EntityPlayer harvester, int fortuneLevel, boolean isSilkTouching)
		{
			return RandomHelper.getRandomIntFromRange(dropsMin, dropsMax);
		}

		public List<ItemStack> getDrops(EntityPlayer harvester, int fortuneLevel, boolean isSilkTouching)
		{
			List<ItemStack> drops = new ArrayList<ItemStack>();
			if (shouldDrop(harvester, fortuneLevel, isSilkTouching))
			{
				int amountToDrop = amountToDrop(harvester, fortuneLevel, isSilkTouching);
				for (int i = 0; i < amountToDrop; i++)
					drops.add(itemStack.copy());
			}
			return drops;
		}

		public void modifyDrops(List<ItemStack> drops, EntityPlayer harvester, int fortuneLevel, boolean isSilkTouching)
		{
			drops.addAll(getDrops(harvester, fortuneLevel, isSilkTouching));
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onGetHarvestDrops(BlockEvent.HarvestDropsEvent event)
	{
		for (Entry<BlockSpecifier, DropSpecifier> blockDropSpecifier : blockDrops.entrySet())
		{
			if (blockDropSpecifier.getKey().matches(event.world, event.x, event.y, event.z, event.block, event.blockMetadata))
			{
				blockDropSpecifier.getValue().modifyDrops(event.drops, event.harvester, event.fortuneLevel, event.isSilkTouching);
			}
		}
	}
}