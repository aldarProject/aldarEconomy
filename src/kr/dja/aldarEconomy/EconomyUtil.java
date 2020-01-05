package kr.dja.aldarEconomy;


import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import kr.dja.aldarEconomy.coininfo.CoinInfo;
import kr.dja.aldarEconomy.coininfo.CoinMetadata;

public class EconomyUtil
{
	private final CoinInfo moneyInfo;

	public EconomyUtil(CoinInfo moneyInfo)
	{
		this.moneyInfo = moneyInfo;
	}
	
	public CoinMetadata isMoney(ItemStack itemStack)
	{
		if(itemStack == null) return null;
		for(CoinMetadata coin : this.moneyInfo.moneyList)
		{	
			if(itemStack.isSimilar(coin.itemStack))
			{
				return coin;
			}
		}
		return null;
	}
	
	public CoinMetadata getMoneyMeta(ItemStack stack)
	{
		for(CoinMetadata meta : this.moneyInfo.moneyList)
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
		CoinMetadata meta = this.isMoney(stack);
		if(meta == null) return 0;
		return meta.value * stack.getAmount();
	}
	
	public boolean isAllowdInventory(Inventory inv)
	{
		InventoryHolder holder = inv.getHolder();
		if(holder instanceof DoubleChest)
		{
			return true;
		}
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
		
		if(!(holder instanceof Container))
		{
			return false;
		}

		return true;
	}
	
	public boolean isSystemModifiableChest(Inventory inv)
	{
		if(this.isAllowdInventory(inv))
		{
			if(inv.getType() != InventoryType.ENDER_CHEST)
			{
				return true;
			}
		}
		return false;
	}
	
	public int getInventoryMoney(Inventory inv)
	{
		int sum = 0;
		for(ItemStack stack : inv.getContents())
		{
			CoinMetadata moneyInfo = this.isMoney(stack);
			if(moneyInfo == null) continue;
			sum += moneyInfo.value * stack.getAmount();
		}
		return sum;
	}
	
	
	public int getPlayerInventoryMoney(HumanEntity p)
	{
		int sum = this.getInventoryMoney(p.getInventory());
		ItemStack cursorStack = p.getItemOnCursor();
		CoinMetadata moneyMeta = this.isMoney(cursorStack);
		if(moneyMeta != null)
		{
			sum += moneyMeta.value * cursorStack.getAmount();
		}
		return sum;
	}
	
}