package kr.dja.aldarEconomy.trade;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;

import kr.dja.aldarEconomy.api.SystemID;

public class Bank
{//돈을 발급해서 뿌려줌
	public Bank()
	{
		
	}
	
	public void issuanceMoney(SystemID id, int amount)
	{
		
	}
	
	public void consumeMoney(SystemID id, int amount)
	{
		
	}
	
	public void issuanceItem(SystemID id, Location loc, int amount, String cause, String data)
	{
		
	}
	
	public boolean issuanceInventory(SystemID id, HumanEntity player, int amount, String cause, String data)
	{
		return false;
	}
	
	public void issuanceChest(SystemID id, Chest chest, int amount, String cause, String data)
	{
		
	}
	
}
