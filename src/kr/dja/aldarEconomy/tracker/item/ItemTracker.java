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
import kr.dja.aldarEconomy.api.APITokenManager;
import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.IntLocation;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemEconomyChild;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemEconomyStorage;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemWallet;
import kr.dja.aldarEconomy.dataObject.player.PlayerEconomyStorage;
import kr.dja.aldarEconomy.setting.MoneyMetadata;
import kr.dja.aldarEconomy.tracker.chest.DestroyChestResult;
import kr.dja.aldarEconomy.tracker.chest.DestroyChestResultMember;
import kr.dja.aldarEconomy.trade.TradeTracker;

public class ItemTracker
{
	private final ItemEconomyStorage itemStorage;
	private final PlayerEconomyStorage playerStorage;
	private final Logger logger;
	private final Plugin plugin;
	private final EconomyUtil util;
	private final TradeTracker tradeTracker;
	
	private Queue<MoneyItemSpawnCacheData> itemDropCheckMoneyQueue;
	private Stack<MoneyItemInfo> itemDropCheckStack;
	private int moneyRemain;
	private MoneyItemSpawnCacheData moneyItemSpawnCacheData;
	private final Runnable nextTickRunnable;
	private boolean hasNextTask;
	
	public ItemTracker(Plugin plugin, EconomyUtil util, ItemEconomyStorage itemStorage, PlayerEconomyStorage playerStorage, TradeTracker tradeTracker, Logger logger)
	{
		this.itemStorage = itemStorage;
		this.playerStorage = playerStorage;
		this.logger = logger;
		this.plugin = plugin;
		this.util = util;
		this.tradeTracker = tradeTracker;
		
		this.moneyRemain = 0;
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
		this.moneyRemain = 0;
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
	
	private void onItemSpawnAssignData(List<MoneyItemInfo> moneyItemInfoList, MoneyItemSpawnCacheData data)
	{
		switch(this.moneyItemSpawnCacheData.type)
		{
		case MoneyItemSpawnCacheData.DESTORY_CHEST:
			
			int chestResultIndex = 0;
			int moneyItemInfoIndex = 0;
			DestroyChestResultMember chestResultMember = null;
			MoneyItemInfo moneyItemInfo = null;
			int chestResultLeft = 0;
			int moneyItemInfoLeft = 0;
			int decreaseMoney;
			while(moneyItemInfoList.size() > moneyItemInfoIndex)
			{
				if(chestResultLeft == 0)
				{
					chestResultMember = data.chestResult.members.get(chestResultIndex);
					chestResultLeft = chestResultMember.discountAmount;
					++chestResultIndex;
				}
				if(moneyItemInfoLeft == 0)
				{
					moneyItemInfo = moneyItemInfoList.get(moneyItemInfoIndex);
					moneyItemInfoLeft = moneyItemInfo.amount;
					++moneyItemInfoIndex;
				}
				
				if(chestResultLeft - moneyItemInfoLeft >= 0)
				{
					chestResultLeft -= moneyItemInfoLeft;
					decreaseMoney = moneyItemInfoLeft;
					moneyItemInfoLeft = 0;
				}
				else
				{
					moneyItemInfoLeft -= chestResultLeft;
					decreaseMoney = chestResultLeft;
					chestResultLeft = 0;
				}
				
				this.itemStorage.increaseEconomy(moneyItemInfo.item.getUniqueId(), chestResultMember.owner, chestResultMember.type, decreaseMoney);
				Bukkit.getServer().broadcastMessage(String.format("fromChestBreak playerName:%s item:%s, type:%s, amount:%s",chestResultMember.owner, moneyItemInfo.item.getUniqueId(), moneyItemInfo.moneyMeta.name, decreaseMoney));
			}
			break;
		case MoneyItemSpawnCacheData.ENTITY_DEATH:
			for(MoneyItemInfo info : moneyItemInfoList)
			{
				this.itemStorage.increaseEconomy(info.item.getUniqueId(), data.entityDeathResultUID, DependType.PLAYER, info.amount);
				Bukkit.getServer().broadcastMessage(String.format("fromPlayerDeath playerName:%s item:%s, type:%s, amount:%s",Bukkit.getPlayer(data.entityDeathResultUID).getName(), info.item.getUniqueId(), info.moneyMeta.name, info.amount));
			}
			break;
		}
		
		
	}
	
	
	public void onPlayerGainMoney(HumanEntity player, Item item, int amount)
	{
		//Bukkit.getServer().broadcastMessage("gainItem " + item.getUniqueId());
		UUID itemUID = item.getUniqueId();
		UUID playerUID = player.getUniqueId();
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
		ItemWallet[] walletArr = new ItemWallet[child.eMap.size()];
		child.eMap.values().toArray(walletArr);
		for(ItemWallet wallet : walletArr)
		{
			if(!wallet.depend.equals(playerUID))
			{
				this.tradeTracker.tradeLog(wallet.depend, wallet.ownerType, playerUID, DependType.PLAYER, wallet.getMoney(), TradeTracker.ARGSTYPE_SENDMONEY_ITEM_TRADE, item.getLocation().toString());
			}
			this.itemStorage.decreaseEconomy(itemUID, wallet.depend, wallet.getMoney());
		}
		this.playerStorage.increaseEconomy(playerUID, amount);
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
		ItemEconomyChild targetChild = this.itemStorage.eMap.get(sourceUID);
		ItemEconomyChild sourceChild = this.itemStorage.eMap.get(sourceUID);
		
		if(targetChild == null)
		{
			targetChild = this.trackingFail(target);
		}
		if(sourceChild == null)
		{
			sourceChild = this.trackingFail(source);
		}
		logger.log(Level.WARNING, "DEBUG!!!! " + sourceChild.eMap);
		ItemWallet[] walletArr = new ItemWallet[sourceChild.eMap.size()];
		sourceChild.eMap.values().toArray(walletArr);
		for(ItemWallet wallet : walletArr)
		{
			int money = wallet.getMoney();
			this.itemStorage.decreaseEconomy(sourceUID, wallet.depend, money);
			this.itemStorage.increaseEconomy(targetUID, wallet.depend, wallet.ownerType, money);
		}
	}

	public void onMoneyDespawn(Item item, int amount)
	{
		
		UUID itemUID = item.getUniqueId();
		ItemEconomyChild child = this.itemStorage.eMap.get(itemUID);
		if(child == null)
		{
			child = this.trackingFail(item);
		}
		ItemWallet[] walletArr = new ItemWallet[child.eMap.size()];
		child.eMap.values().toArray(walletArr);
		for(ItemWallet wallet : walletArr)
		{
			int money = wallet.getMoney();
			
			this.itemStorage.decreaseEconomy(itemUID, wallet.depend, money);
			this.tradeTracker.tradeLog(wallet.depend, wallet.ownerType, APITokenManager.SYSTEM_TOKEN.uuid, DependType.SYSTEM, money, TradeTracker.ARGSTYPE_SENDMONEY_ITEM_TRADE, item.getLocation().toString());
		}
	}
	
	private ItemEconomyChild trackingFail(Item item)
	{
		Location loc = item.getLocation();
		logger.log(Level.WARNING, String.format("ItemTracker.moneyMerge() 아이템 추적 실패: %s)"
				, new IntLocation(loc)));
		int amount = this.util.getValue(item.getItemStack());
		ItemEconomyChild child = this.itemStorage.increaseEconomy(item.getUniqueId(), APITokenManager.SYSTEM_TOKEN.uuid, DependType.SYSTEM, amount);
		this.tradeTracker.internalSystemLog(APITokenManager.SYSTEM_TOKEN.uuid, DependType.SYSTEM, amount, TradeTracker.ARGSTYPE_SYSTEM_FORCE_ISSUANCE, "ON_MONEY_MERGE,"+loc.toString());
		return child;
	}

	

}
