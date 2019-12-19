package kr.dja.aldarEconomy.tracker.item;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import kr.dja.aldarEconomy.EconomyUtil;
import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemEconomyChild;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemEconomyStorage;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemWallet;
import kr.dja.aldarEconomy.dataObject.player.PlayerEconomyStorage;
import kr.dja.aldarEconomy.setting.MoneyMetadata;
import kr.dja.aldarEconomy.tracker.chest.DestroyChestResult;
import kr.dja.aldarEconomy.tracker.chest.DestroyChestResultMember;

public class ItemTracker
{
	private final ItemEconomyStorage itemStorage;
	private final PlayerEconomyStorage playerStorage;
	private final Logger logger;
	
	private final Plugin plugin;
	private final EconomyUtil util;
	
	private Queue<MoneyItemSpawnCacheData> itemDropCheckMoneyQueue;
	private Stack<MoneyItemInfo> itemDropCheckStack;
	private int moneyRemain;
	private MoneyItemSpawnCacheData moneyItemSpawnCacheData;
	private final Runnable nextTickRunnable;
	private boolean hasNextTask;
	
	public ItemTracker(Plugin plugin, EconomyUtil util, ItemEconomyStorage itemStorage, PlayerEconomyStorage playerStorage, Logger logger)
	{
		
		this.itemStorage = itemStorage;
		this.playerStorage = playerStorage;
		this.logger = logger;
		
		this.plugin = plugin;
		this.util = util;
		this.itemDropCheckMoneyQueue = new LinkedList<>();
		this.itemDropCheckStack = new Stack<>();
		this.nextTickRunnable = this::nextTick;
		this.hasNextTask = false;
	}
	
	public void onPlayerDeathDropMoney(HumanEntity player, int dropMoney)
	{
		this.itemDropCheckMoneyQueue.add(new MoneyItemSpawnCacheData(MoneyItemSpawnCacheData.ENTITY_DEATH, player.getUniqueId(), dropMoney));
		if(!this.hasNextTask)
		{
			Bukkit.getScheduler().runTask(this.plugin, this.nextTickRunnable);
			this.hasNextTask = true;
		}
	}
	
	public void onChestBreak(DestroyChestResult result)
	{
		this.itemDropCheckMoneyQueue.add(new MoneyItemSpawnCacheData(MoneyItemSpawnCacheData.DESTORY_CHEST, result));
		this.moneyRemain = 0;
		if(!this.hasNextTask)
		{
			Bukkit.getScheduler().runTask(this.plugin, this.nextTickRunnable);
			this.hasNextTask = true;
		}
	}
	
	private void nextTick()
	{//오류가 누적되지 않도록 해줌.
		Bukkit.getServer().broadcastMessage("nextTick");
		this.hasNextTask = false;
		this.itemDropCheckMoneyQueue.clear();
		this.itemDropCheckStack.clear();
		this.moneyItemSpawnCacheData = null;
	}

	public void onItemSpawn(Item item, MoneyMetadata moneyMeta)
	{
		ItemStack itemStack = item.getItemStack();
		
		
		if(this.moneyRemain == 0)
		{
			if(this.itemDropCheckMoneyQueue.isEmpty())
			{
				return;
			}
			this.moneyItemSpawnCacheData = this.itemDropCheckMoneyQueue.poll();
			this.moneyRemain = this.moneyItemSpawnCacheData.dropMoney;
			Bukkit.getServer().broadcastMessage("dequeue: " + this.moneyRemain);
		}
		int amount = moneyMeta.value * itemStack.getAmount();
		this.moneyRemain -= amount;
		this.itemDropCheckStack.add(new MoneyItemInfo(item, amount, moneyMeta));
		if(this.moneyRemain == 0)
		{
			this.onItemSpawnAssignData(this.itemDropCheckStack, this.moneyItemSpawnCacheData);
			/*Bukkit.getServer().broadcastMessage("다찾음 queueSize:" + this.itemDropCheckMoneyQueue.size());
			
			for(MoneyItemInfo info : this.itemDropCheckStack)
			{
				
				Bukkit.getServer().broadcastMessage(String.format("item:%s, type:%s, amount:%s", info.item.getUniqueId(), info.moneyMeta.name, info.amount));
			}*/
			this.itemDropCheckStack.clear();
		}
	}
	
	private void onItemSpawnAssignData(List<MoneyItemInfo> moneyItemInfo, MoneyItemSpawnCacheData data)
	{
		switch(this.moneyItemSpawnCacheData.type)
		{
		case MoneyItemSpawnCacheData.DESTORY_CHEST:
			int leftMoney = data.chestResult.members.get(0).discountAmount;
			int chestResultIndex = 0;
			int moneyItemInfoIndex = 0;
			while(moneyItemInfo.size() > moneyItemInfoIndex)
			{
				leftMoney = 
				
			}
			break;
		case MoneyItemSpawnCacheData.ENTITY_DEATH:
			for(MoneyItemInfo info : moneyItemInfo)
			{
				this.itemStorage.increaseEconomy(info.item.getUniqueId(), data.entityDeathResultUID, DependType.PLAYER, info.amount);
				Bukkit.getServer().broadcastMessage(String.format("fromPlayerDeath item:%s, type:%s, amount:%s", info.item.getUniqueId(), info.moneyMeta.name, info.amount));
			}
			break;
		}
		
		
	}
	
	
	public void onPlayerGainMoney(HumanEntity player, Item item, int amount)
	{
		//Bukkit.getServer().broadcastMessage("gainItem " + item.getUniqueId());
		UUID itemUID = item.getUniqueId();
		ItemEconomyChild child = this.itemStorage.eMap.get(itemUID);
		if(child == null)
		{
			Location loc = item.getLocation();
			logger.log(Level.WARNING, String.format("ItemTracker.playerGainMoney() 아이템 추적 실패 (%s), %s,%d,%d,%d)"
					, player.getName(), loc.getWorld().getName(), (int)loc.getX(), (int)loc.getY(), (int)loc.getZ()));
			
			child = this.itemStorage.increaseEconomy(itemUID, player.getUniqueId(), DependType.PLAYER, amount);
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
		//Bukkit.getServer().broadcastMessage("drop " + item.getUniqueId());
		UUID playerUID = player.getUniqueId();
		this.playerStorage.decreaseEconomy(playerUID, amount);
		this.itemStorage.increaseEconomy(item.getUniqueId(), playerUID, DependType.PLAYER, amount);
	}
	
	
	public void onMoneyMerge(Item target, Item source)
	{
		
		UUID targetUID = target.getUniqueId();
		UUID sourceUID = source.getUniqueId();
		//Bukkit.getServer().broadcastMessage("merge" + sourceUID + " " + targetUID);
		ItemEconomyChild sourceChild = this.itemStorage.eMap.get(sourceUID);
		if(sourceChild == null)
		{
			Location loc = source.getLocation();
			logger.log(Level.WARNING, String.format("ItemTracker.moneyMerge() 아이템 추적 실패, %s,%d,%d,%d)"
					, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
			return;
		}
		
		for(ItemWallet wallet : sourceChild.eMap.values())
		{
			int money = wallet.getMoney();
			this.itemStorage.decreaseEconomy(sourceUID, wallet.depend, money);
			this.itemStorage.increaseEconomy(targetUID, wallet.depend, wallet.ownerType, money);
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
