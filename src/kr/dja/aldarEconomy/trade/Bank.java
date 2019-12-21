package kr.dja.aldarEconomy.trade;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.bergerkiller.bukkit.common.utils.ItemUtil;

import kr.dja.aldarEconomy.EconomyUtil;
import kr.dja.aldarEconomy.api.SystemID;
import kr.dja.aldarEconomy.data.EconomyDataStorage;
import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.IntLocation;
import kr.dja.aldarEconomy.setting.MoneyInfo;
import kr.dja.aldarEconomy.setting.MoneyMetadata;
import kr.dja.aldarEconomy.tracker.chest.ChestTracker;

public class Bank
{//돈을 발급해서 뿌려줌
	private final double ITEM_DIST = 0.5;
	private final MoneyInfo info;
	private final EconomyUtil util;
	private final EconomyDataStorage dataStorage;
	private final ChestTracker chestTracker;
	
	public Bank(MoneyInfo info, EconomyUtil util, EconomyDataStorage dataStorage, ChestTracker chestTracker)
	{
		this.info = info;
		this.util = util;
		this.dataStorage = dataStorage;
		this.chestTracker = chestTracker;
	}
	
	public void consumeMoney(SystemID id, HumanEntity player, int amount)
	{
		
	}
	
	public boolean issuanceToItem(SystemID id, Location loc, int amount, String cause, String data)
	{
		World w = loc.getWorld();
		
		ItemStack[] moneyItemArr = this.getMoneyItemStack(amount);
		if(moneyItemArr == null) return false;
		
		int sqSide = (int)Math.ceil(Math.sqrt(moneyItemArr.length));
		double offset = (sqSide * ITEM_DIST) / 2.0;
		for(int i = 0; i < sqSide; ++i)
		{
			for(int j = 0; j < sqSide; ++j)
			{
				int index = i * sqSide + j;
				if(index >= moneyItemArr.length) break;
				double xoffset = (i * ITEM_DIST) - offset;
				double zoffset = (j * ITEM_DIST) - offset;
				Location l = new Location(loc.getWorld(), loc.getX() + xoffset, loc.getY(), loc.getZ() + zoffset);
				Item item = w.dropItem(l, moneyItemArr[index]);
				this.dataStorage.itemEconomyStorage.increaseEconomy(
						item.getUniqueId()
						, id.uuid, DependType.SYSTEM
						, this.util.getValue(moneyItemArr[index]));
			}
		}
		
		return true;
	}
	
	public boolean issuanceToPlayer(SystemID id, HumanEntity player, int amount, String cause, String data)
	{
		ItemStack[] moneyItemArr = this.getMoneyItemStack(amount);
		if(moneyItemArr == null) return false;
		Inventory inv = player.getInventory();
		if(!ItemUtil.canTransferAll(moneyItemArr, inv)) return false;
		for(ItemStack item : moneyItemArr)
		{
			ItemUtil.transfer(item, inv, -1);
		}
		this.dataStorage.playerDependEconomy.increaseEconomy(player.getUniqueId(), amount);
		return true;
	}
	
	public boolean issuanceToChest(SystemID id, Chest chest, int amount, String cause, String data)
	{
		ItemStack[] moneyItemArr = this.getMoneyItemStack(amount);
		if(moneyItemArr == null) return false;
		Inventory inv = chest.getInventory();
		if(this.chestTracker.isOpenedEconomyChest(inv)) return false;
		if(!ItemUtil.canTransferAll(moneyItemArr, inv)) return false;
		for(ItemStack item : moneyItemArr)
		{
			ItemUtil.transfer(item, inv, -1);
		}
		IntLocation intLoc = new IntLocation(chest.getLocation());
		//this.dataStorage.chestDependEconomy.createAndIncreaseEconomy(intLoc, id.uuid, amount, DependType.SYSTEM);
		return true;
	}
	
	
	public ItemStack[] getMoneyItemStack(int amount)
	{
		if(amount % this.info.moneyList.get(0).value != 0) return null;
		int coinCntArr[] = new int[this.info.moneyList.size()];
		
		for(int i = this.info.moneyList.size() - 1; i >= 0; --i)
		{
			MoneyMetadata meta = this.info.moneyList.get(i);
			coinCntArr[i] = amount / meta.value;
			amount = amount % meta.value;
		}
		
		List<ItemStack> moneyItemList = new ArrayList<>();
		for(int i = 0; i < this.info.moneyList.size(); ++i)
		{
			MoneyMetadata meta = this.info.moneyList.get(i);
			int itemStackCnt = coinCntArr[i]/meta.maxStack;
			int itemStackRem = coinCntArr[i]%meta.maxStack;
			for(int j = 0; j < itemStackCnt; ++j)
			{
				ItemStack moneyItem = meta.itemStack.clone();
				moneyItem.setAmount(meta.maxStack);
				moneyItemList.add(moneyItem);
			}
			if(itemStackRem != 0)
			{
				ItemStack moneyItem = meta.itemStack.clone();
				moneyItem.setAmount(itemStackRem);
				moneyItemList.add(moneyItem);
			}
		}
		ItemStack itemStackArray[] = new ItemStack[moneyItemList.size()];
		moneyItemList.toArray(itemStackArray);
		return itemStackArray;
	}
}
