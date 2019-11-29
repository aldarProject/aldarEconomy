package kr.dja.aldarEconomy.tracker.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import kr.dja.aldarEconomy.ConstraintChecker;
import kr.dja.aldarEconomy.data.EconomyDataStorage;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagString;

public class ItemTracker
{
	public static final String ID_TAG = "AldarEconomy";
	public static final String SEP = "/";
	
	private final ConstraintChecker checker;
	private final EconomyDataStorage storage;
	private final Logger logger;
	
	public ItemTracker(ConstraintChecker checker, EconomyDataStorage storage, Logger logger)
	{
		this.checker = checker;
		this.storage = storage;
		this.logger = logger;
	}
	
	public void playerGainMoney(HumanEntity player, Item item)
	{
		Bukkit.getServer().broadcastMessage("gainItem");
		//item.setItemStack(this.untagging(item.getItemStack()));
	}
	
	public void playerDropMoney(HumanEntity player, Item item)
	{
		Bukkit.getServer().broadcastMessage("dropItem");
		ItemStack stack = item.getItemStack();
		//item.setItemStack(this.tagging(player.getUniqueId(), stack));
	}
	
	
	public void modifyDrops(Block b, Map<HumanEntity, Integer> moneyMap)
	{
		b.getDrops();
	}

	public void moneyDespawn(Item item)
	{
		
	}
	

}
