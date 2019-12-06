package kr.dja.aldarEconomy.tracker.item;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemEconomyChild;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemEconomyStorage;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemWallet;
import kr.dja.aldarEconomy.dataObject.player.PlayerEconomyStorage;

public class ItemTracker
{
	private final ItemEconomyStorage itemStorage;
	private final PlayerEconomyStorage playerStorage;
	private final Logger logger;
	
	public ItemTracker(ItemEconomyStorage itemStorage, PlayerEconomyStorage playerStorage, Logger logger)
	{
		this.itemStorage = itemStorage;
		this.playerStorage = playerStorage;
		this.logger = logger;
	}
	
	public void onPlayerGainMoney(HumanEntity player, Item item, int amount)
	{
		Bukkit.getServer().broadcastMessage("gainItem");
		UUID itemUID = item.getUniqueId();
		ItemEconomyChild child = this.itemStorage.eMap.get(itemUID);
		if(child == null)
		{
			Location loc = item.getLocation();
			logger.log(Level.WARNING, String.format("ItemTracker.playerGainMoney() 아이템 추적 실패 (%s), %s,%d,%d,%d)"
					, player.getName(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
			return;
		}
		int diff = child.getTotalMoney() - amount;
		if(diff != 0)
		{
			logger.log(Level.WARNING, String.format("ItemTracker.playerGainMoney() 돈 액수 차이 (%s) %d"
					, player.getName(), diff));
		}
		for(ItemWallet wallet : child.eMap.values())
		{
			this.itemStorage.decreaseEconomy(itemUID, wallet.depend, wallet.getMoney());
		}
		this.playerStorage.increaseEconomy(player.getUniqueId(), amount);
	}
	
	public void onPlayerDropMoney(HumanEntity player, Item item, int amount)
	{
		Bukkit.getServer().broadcastMessage("dropItem");
		this.itemStorage.increaseEconomy(item.getUniqueId(), player.getUniqueId(), DependType.PLAYER, amount);
	}
	
	
	public void onMoneyMerge(Item target, Item source)
	{
		UUID targetUID = target.getUniqueId();
		UUID sourceUID = source.getUniqueId();
		ItemEconomyChild sourceMap = this.itemStorage.eMap.get(sourceUID);
		if(sourceMap == null)
		{
			Location loc = source.getLocation();
			logger.log(Level.WARNING, String.format("ItemTracker.moneyMerge() 아이템 추적 실패, %s,%d,%d,%d)"
					, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
			return;
		}
		
		for(ItemWallet wallet : sourceMap.eMap.values())
		{
			this.itemStorage.decreaseEconomy(sourceUID, wallet.depend, wallet.getMoney());
			this.itemStorage.increaseEconomy(targetUID, wallet.depend, wallet.dependType, wallet.getMoney());
		}
	}

	public void onMoneyDespawn(Item item, int amount)
	{
		int diff = amount;
		UUID itemUID = item.getUniqueId();
		ItemEconomyChild child = this.itemStorage.eMap.get(itemUID);
		if(child == null)
		{
			Location loc = item.getLocation();
			logger.log(Level.WARNING, String.format("ItemTracker.moneyDespawn() 아이템 추적 실패, %s,%d,%d,%d)"
					, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
			return;
		}
		for(ItemWallet wallet : child.eMap.values())
		{
			int money = wallet.getMoney();
			diff -= money;
			this.itemStorage.decreaseEconomy(itemUID, wallet.depend, money);
		}
		
		if(diff != 0)
		{
			logger.log(Level.WARNING, String.format("ItemTracker.moneyDespawn() 돈 액수 차이 %d"
					, diff));
		}
	}
	

}
