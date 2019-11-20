package kr.dja.aldarEconomy.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import kr.dja.aldarEconomy.ConstraintChecker;
import kr.dja.aldarEconomy.dao.EconomyStateStorage;
import net.minecraft.server.v1_12_R1.NBTTagCompound;

public class ItemTracker
{
	private final ConstraintChecker checker;
	private final EconomyStateStorage storage;
	private final Logger logger;
	
	public ItemTracker(ConstraintChecker checker, EconomyStateStorage storage, Logger logger)
	{
		this.checker = checker;
		this.storage = storage;
		this.logger = logger;
	}
	
	public void playerGainMoney(HumanEntity player, Item item)
	{
		List<String> lore = stack.getLore();
		if(lore != null)
		{
			for(int i = 0; i < lore.size(); ++i)
			{
				//lore.get(i).split(regex)
			}
		}
	}
	
	public void playerDropMoney(HumanEntity player, Item item)
	{
		ItemStack stack = item.getItemStack();
		List<String> lore = stack.getLore();
		if(lore == null) lore = new ArrayList<>();
		lore.add(String.format("owner/%s/%s", player.getUniqueId(), player.getName()));
		stack.setLore(lore);
	}
	
	public void modifyDrops(Block b, Map<HumanEntity, Integer> moneyMap)
	{
		b.getDrops();
	}

	public void moneyDespawn(Item item)
	{
		
	}
	
	

}
