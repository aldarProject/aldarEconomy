package kr.dja.aldarEconomy;


import java.util.List;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import kr.dja.aldarEconomy.setting.MoneyInfo;
import kr.dja.aldarEconomy.setting.MoneyMetadata;

public class ConstraintChecker
{
	private final MoneyInfo moneyInfo;
	
	public ConstraintChecker(MoneyInfo moneyInfo)
	{
		this.moneyInfo = moneyInfo;
	}
	
	public MoneyMetadata isMoney(ItemStack itemStack)
	{
		if(itemStack == null) return null;
		for(MoneyMetadata coin : this.moneyInfo.moneyList)
		{	
			if(itemStack.isSimilar(coin.itemStack))
			{
				return coin;
			}
		}
		return null;
	}
	
	public boolean isAllowdInventory(Inventory inv)
	{
		
		switch(inv.getType())
		{
		case ANVIL:
		case BEACON:
		case BREWING:
		//case CRAFTING:
		//case CREATIVE:
		case DISPENSER:
		case DROPPER:
		case ENCHANTING:
		//case ENDER_CHEST:
		case FURNACE:
		case HOPPER:
		case MERCHANT:
		//case PLAYER:
		case SHULKER_BOX:
		//case WORKBENCH:
			return false;
		default:
			break;
		}
		
		switch(inv.getTitle())
		{
		case "Minecart with Chest":
			return false;
			
		}
		
		return true;
	}
}