package kr.dja.aldarEconomy.tracker.item;

import java.util.Collections;
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
import kr.dja.aldarEconomy.dataObject.chest.ChestWallet;
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
	
	public void onPlayerDeathDropMoney(HumanEntity player, int amount)
	{
		UUID playerUID = player.getUniqueId();
		
		int result = this.playerStorage.decreaseEconomy(playerUID, amount);
		if(result < 0)
		{
			int playerMoney = this.util.getPlayerInventoryMoney(player);
			int diff = playerMoney - (amount + result);
			this.tradeTracker.forceIssuance(playerUID, diff, "PLAYER_DEATH", new IntLocation(player.getLocation()));
		}
		this.itemDropCheckMoneyQueue.add(new MoneyItemSpawnCacheData(MoneyItemSpawnCacheData.ENTITY_DEATH, playerUID, amount));
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
		}
		int amount = moneyMeta.value * itemStack.getAmount();
		this.moneyRemain -= amount;
		this.itemDropCheckStack.add(new MoneyItemInfo(item, amount, moneyMeta));
		if(this.moneyRemain == 0)
		{
			this.onItemSpawnAssignData(this.itemDropCheckStack, this.moneyItemSpawnCacheData);
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
			}
			break;
		case MoneyItemSpawnCacheData.ENTITY_DEATH:
			for(MoneyItemInfo info : moneyItemInfoList)
			{
				this.itemStorage.increaseEconomy(info.item.getUniqueId(), data.entityDeathResultUID, DependType.PLAYER, info.amount);
			}
			break;
		}
		
		
	}
	
	
	public void onPlayerGainMoney(HumanEntity player, Item item, int amount)
	{
		UUID itemUID = item.getUniqueId();
		UUID playerUID = player.getUniqueId();
		ItemEconomyChild map = this.itemStorage.eMap.get(itemUID);
		IntLocation intLoc = new IntLocation(item.getLocation());
		if(map == null)
		{
			this.tradeTracker.forceIssuance(playerUID, amount, "PLAYER_GAIN_MONEY", intLoc);
			map = this.itemStorage.increaseEconomy(itemUID, player.getUniqueId(), DependType.PLAYER, amount);
		}
		ItemWallet[] walletArr = new ItemWallet[map.eMap.size()];
		map.eMap.values().toArray(walletArr);
		int playerMoney = this.itemStorage.getMoney(itemUID, playerUID);
		int otherMoney = amount - playerMoney;
		if(otherMoney <= 0)
		{// 자신이 넣은만큼만 꺼내갔을 경우
			this.itemStorage.decreaseEconomy(itemUID, playerUID, amount);
		}
		else
		{// 남이 넣은 돈까지 꺼내가는 경우
			this.itemStorage.decreaseEconomy(itemUID, playerUID, playerMoney);
			// 만약 플레이어가 넣은 돈보다 많이 꺼내갔을 경우 가장 적은 돈을 넣은 플레이어의 돈부터 가져가도록 함.
			int leftMoney = otherMoney;
			
			List<ItemWallet> list = new LinkedList<>(map.eMap.values());
			Collections.sort(list);
			for(ItemWallet wallet : list)
			{
				int money = wallet.getMoney();
				if(wallet.depend.equals(playerUID)) continue;
				if(leftMoney - money <= 0)
				{
					this.itemStorage.decreaseEconomy(itemUID, wallet.depend, leftMoney);
					this.tradeTracker.tradeLog(playerUID, DependType.PLAYER, wallet.depend, wallet.ownerType, leftMoney, "ITEM_TRADE", intLoc);
					leftMoney = 0;
					break;
				}
				else
				{
					this.itemStorage.decreaseEconomy(itemUID, wallet.depend, money);
					this.tradeTracker.tradeLog(playerUID, DependType.PLAYER, wallet.depend, wallet.ownerType, money, "ITEM_TRADE", intLoc);
					leftMoney -= money;
				}
			}
		}
		this.playerStorage.increaseEconomy(playerUID, amount);
	}
	
	public void onPlayerDropMoney(HumanEntity player, Item item, int amount)
	{
		UUID playerUID = player.getUniqueId();
		int result = this.playerStorage.decreaseEconomy(playerUID, amount);
		if(result < 0)
		{
			int playerMoney = this.util.getPlayerInventoryMoney(player);
			this.playerStorage.increaseEconomy(playerUID, playerMoney);
			int diff = playerMoney - (amount + result) + amount;
			this.tradeTracker.forceIssuance(playerUID, diff, "PLAYER_DROP_MONEY", new IntLocation(player.getLocation()));
		}
		this.itemStorage.increaseEconomy(item.getUniqueId(), playerUID, DependType.PLAYER, amount);
	}
	
	
	public void onMoneyMerge(Item target, Item source)
	{
		UUID targetUID = target.getUniqueId();
		UUID sourceUID = source.getUniqueId();
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
		IntLocation intLoc = new IntLocation(item.getLocation());
		for(ItemWallet wallet : walletArr)
		{
			int money = wallet.getMoney();
			
			this.itemStorage.decreaseEconomy(itemUID, wallet.depend, money);
			this.tradeTracker.tradeLog(wallet.depend, wallet.ownerType, APITokenManager.SYSTEM_TOKEN.uuid, DependType.SYSTEM, money, "MONEY_DESPAWN", intLoc);
		}
	}
	
	private ItemEconomyChild trackingFail(Item item)
	{
		IntLocation intLoc = new IntLocation(item.getLocation());

		int amount = this.util.getValue(item.getItemStack());
		ItemEconomyChild child = this.itemStorage.increaseEconomy(item.getUniqueId(), APITokenManager.SYSTEM_TOKEN.uuid, DependType.SYSTEM, amount);
		this.tradeTracker.forceIssuance(null, amount, "ON_MONEY_MERGE", intLoc);
		return child;
	}

}
