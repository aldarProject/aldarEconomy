package kr.dja.aldarEconomy.trade;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TradeTracker
{
	public void tradeLog(Player my, Player target, int amount, String description)
	{
		this.tradeLog(my, target, amount, null, null, description);
	}
	
	public void tradeLog(Player my, Player target, int amount, List<ItemStack> myItem, List<ItemStack> targetItem, String description)
	{
		
	}
	
	public void tradeLog(Player my, String systemID, int amount, String description)
	{
		this.tradeLog(my, systemID, amount, null, null, description);
	}
	
	public void tradeLog(Player my, String systemID, int amount, List<ItemStack> myItem, List<ItemStack> targetItem, String description)
	{
		
	}
	
	public void tradeLog(String systemID, Player target, int amount, String description)
	{
		this.tradeLog(systemID, target, amount, null, null, description);
	}
	
	public void tradeLog(String systemID, Player target, int amount, List<ItemStack> myItem, List<ItemStack> targetItem, String description)
	{
		
	}

}
