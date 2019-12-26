package kr.dja.aldarEconomy;


import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import kr.dja.aldarEconomy.setting.MoneyInfo;
import kr.dja.aldarEconomy.setting.MoneyMetadata;

public class EconomyUtil
{
	private final MoneyInfo moneyInfo;

	public EconomyUtil(MoneyInfo moneyInfo)
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
	
	public MoneyMetadata getMoneyMeta(ItemStack stack)
	{
		for(MoneyMetadata meta : this.moneyInfo.moneyList)
		{
			if(meta.itemStack.isSimilar(stack))
			{
				return meta;
			}
		}
		return null;
	}
	
	public int getValue(ItemStack stack)
	{
		MoneyMetadata meta = this.isMoney(stack);
		if(meta == null) return 0;
		return meta.value * stack.getAmount();
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
	
	public int getInventoryMoney(Inventory inv)
	{
		int sum = 0;
		for(ItemStack stack : inv.getContents())
		{
			MoneyMetadata moneyInfo = this.isMoney(stack);
			if(moneyInfo == null) continue;
			sum += moneyInfo.value * stack.getAmount();
		}
		return sum;
	}
	
	
	public int getPlayerInventoryMoney(HumanEntity p)
	{
		int sum = this.getInventoryMoney(p.getInventory());
		ItemStack cursorStack = p.getItemOnCursor();
		MoneyMetadata moneyMeta = this.isMoney(cursorStack);
		if(moneyMeta != null)
		{
			sum += moneyMeta.value * cursorStack.getAmount();
		}
		return sum;
	}
	
}