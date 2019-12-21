package kr.dja.aldarEconomy.tracker.chest;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

import kr.dja.aldarEconomy.EconomyUtil;

class OpenedChestMoneyInfo
{
	private final Map<Inventory, OpenedChestMoneyMember> openedChestInv;
	private EconomyUtil util;
	
	OpenedChestMoneyInfo(EconomyUtil util)
	{
		this.openedChestInv = new HashMap<>();
		this.util = util;
	}
	
	public OpenedChestMoneyMember getOpenedChestInfo(Inventory chest)
	{
		OpenedChestMoneyMember info = this.openedChestInv.get(chest);

		if(info == null)
		{
			if(chest instanceof DoubleChestInventory)
			{
				DoubleChestInventory dchest = (DoubleChestInventory)chest;
				info = this.openedChestInv.get(dchest.getLeftSide());
				if(info == null) info = this.openedChestInv.get(dchest.getRightSide());
			}
		}
		if(info != null && chest.getHolder() != null)
		{
			info.masterInven = chest.getHolder().getInventory();
		}
		
		return info;
	}
	
	public OpenedChestMoneyMember createOpenedChestInfo(Inventory chest)
	{
		OpenedChestMoneyMember info = new OpenedChestMoneyMember();
		if(chest instanceof DoubleChestInventory)
		{
			DoubleChestInventory dchest = (DoubleChestInventory)chest;
			this.openedChestInv.put(dchest.getLeftSide(), info);
			this.openedChestInv.put(dchest.getRightSide(), info);
		}
		else
		{
			this.openedChestInv.put(chest, info);
		}
		info.masterInven = chest.getHolder().getInventory();
		info.chestMoney = this.util.getInventoryMoney(info.masterInven);
		return info;
	}
	
	public void removeOpenedChestInfo(Inventory chest)
	{
		if(chest instanceof DoubleChestInventory)
		{
			DoubleChestInventory dchest = (DoubleChestInventory)chest;
			this.openedChestInv.remove(dchest.getLeftSide());
			this.openedChestInv.remove(dchest.getRightSide());
		}
		else
		{
			this.openedChestInv.remove(chest);
		}
	}

}
