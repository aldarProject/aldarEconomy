package kr.dja.aldarEconomy.economyState;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import kr.dja.aldarEconomy.api.SystemID;
import kr.dja.aldarEconomy.economyState.data.MultipleEconomyMap;
import kr.dja.aldarEconomy.economyState.data.EconomyMap;
import kr.dja.aldarEconomy.economyState.data.EconomyMapChild;
import kr.dja.aldarEconomy.setting.MoneyInfo;
import kr.dja.aldarEconomy.trade.TradeTracker;

public class EconomyDataStorage
{// 이코노미 데이터에 대한 CRUD작업만 수행

	private final MultipleEconomyMap<Location, UUID> chestDependEconomy;
	private final EconomyMap<SystemID> systemDependEconomy;
	private final MultipleEconomyMap<Item, UUID> itemDependEconomyPlayer;
	private final MultipleEconomyMap<Item, System> itemDependEconomySystem;
	private final EconomyMap<UUID> playerDependEconomy;
	private final EconomyMap<UUID> playerEnderChestEconomy;
	
	private final MoneyInfo moneyInfo;
	private final TradeTracker tradeTracker;
	private final Logger logger;
	
	public EconomyDataStorage(MoneyInfo moneyInfo, TradeTracker tradeTracker, Logger logger)
	{
		
		this.chestDependEconomy = new MultipleEconomyMap<>();
		this.systemDependEconomy = new EconomyMap<>();
		this.itemDependEconomyPlayer = new MultipleEconomyMap<>();
		this.itemDependEconomySystem = new MultipleEconomyMap<>();
		this.playerDependEconomy = new EconomyMap<>();
		this.playerEnderChestEconomy = new EconomyMap<>();
		
		this.moneyInfo = moneyInfo;
		this.tradeTracker = tradeTracker;
		this.logger = logger;
	}
	
	public void playerToItem(HumanEntity player, Item item, int amount)
	{
		
	}
	
	public void itemToPlayer(Item item, HumanEntity player, int amount)
	{
		
	}
	
	private void chestToPlayer(EconomyMap<UUID> chestUsers, UUID playerUID, int amount)
	{
		int playerMoney = chestUsers.getMoney(playerUID);
		int otherMoney = amount - playerMoney;
		if(otherMoney <= 0)
		{// 창고에서 자신이 넣은만큼만 꺼내갔을 경우
			chestUsers.decreaseEconomy(playerUID, amount);
			Bukkit.getServer().broadcastMessage(String.format("PlayerAccess %s (%d)",Bukkit.getPlayer(playerUID).getName(), amount));
		}
		else
		{// 창고에 남이 넣은 돈까지 꺼내가는 경우
			chestUsers.decreaseEconomy(playerUID, playerMoney);
			if(chestUsers.getTotalMoney() - amount < 0)
			{
				logger.log(Level.WARNING, String.format("EconomyDataStorage.chestToPlayer(): %s 존재하는 돈보다 많이 꺼냄(%d)", Bukkit.getPlayer(playerUID).getName(), chestUsers.getTotalMoney() - amount));
				return;
			}
			// 만약 플레이어가 넣은 돈보다 많이 꺼내갔을 경우 가장 적은 돈을 넣은 플레이어의 돈부터 가져가도록 함.
			int leftMoney = otherMoney;
			
			List<Map.Entry<UUID, Integer>> list = new LinkedList<>(chestUsers.eMap.entrySet());
			list.sort((o1, o2)->o1.getValue() - o2.getValue());
			
			for(Map.Entry<UUID, Integer> entry : list)
			{
				UUID key = entry.getKey();
				int value = entry.getValue();
				if(key.equals(playerUID)) continue;
				if(leftMoney - value <= 0)
				{
					chestUsers.decreaseEconomy(key, leftMoney);
					Bukkit.getServer().broadcastMessage(String.format("PlayerChestTrade %s to %s (%d)",Bukkit.getPlayer(key).getName(), Bukkit.getPlayer(playerUID).getName(), leftMoney));
					break;
				}
				else
				{
					chestUsers.decreaseEconomy(key, value);
					Bukkit.getServer().broadcastMessage(String.format("PlayerChestTrade %s to %s (%d)",Bukkit.getPlayer(key).getName(), Bukkit.getPlayer(playerUID).getName(), value));
					leftMoney -= value;
				}
			}
		}
	}
	
	public void chestToPlayer(DoubleChest chest, HumanEntity player, int amount)
	{
		EconomyMapChild<Location, UUID> map = this.takeDoubleChest(chest);
		if(map == null)
		{//유저가 존재하지도 않는 돈을 꺼내가려고 할 때
			logger.log(Level.WARNING, String.format("EconomyDataStorage.chestToPlayer(): %s %s 존재하지 않는 돈 꺼냄(%d)", player.getName(), amount));
			return;
		}
		else
		{
			this.chestToPlayer(map, player.getUniqueId(), amount);
		}
	}
	
	public void chestToPlayer(Chest chest, HumanEntity player, int amount)
	{
		EconomyMapChild<Location, UUID> map = this.chestDependEconomy.eMap.get(chest.getLocation());
		if(map == null)
		{//유저가 존재하지도 않는 돈을 꺼내가려고 할 때
			logger.log(Level.WARNING, String.format("EconomyDataStorage.chestToPlayer(): %s %s 존재하지 않는 돈 꺼냄(%d)", player.getName(), amount));
			return;
		}
		this.chestToPlayer(map, player.getUniqueId(), amount);
	}
	
	public void playerToChest(HumanEntity player, Chest chest, int amount)
	{
		this.chestDependEconomy.increaseEconomy(chest.getLocation(), player.getUniqueId(), amount);
		Bukkit.getServer().broadcastMessage(String.format("PlayerToChest %s %s (%d)", player.getName(), chest.getLocation(), amount));
	}
	
	public void playerToChest(HumanEntity player, DoubleChest chest, int amount)
	{
		EconomyMapChild<Location, UUID> map = this.takeDoubleChest(chest);
		EconomyMapChild<Location, UUID> increaseMap = this.chestDependEconomy.increaseEconomy(chest.getLeftSide().getInventory().getLocation(), player.getUniqueId(), amount);
		if(map == null)
		{
			this.chestDependEconomy.appendKey(chest.getRightSide().getInventory().getLocation(), increaseMap);
		}
		Bukkit.getServer().broadcastMessage(String.format("PlayerToChest %s %s (%d)", player.getName(), chest.getLocation(), amount));
	}
	
	public void breakChest(Block chest, int amount)
	{
		Location chestLoc = chest.getLocation();
		EconomyMapChild<Location, UUID> map = this.chestDependEconomy.eMap.get(chestLoc);
		if(map == null)
		{
			logger.log(Level.WARNING, String.format("EconomyDataStorage.breakChest(): %s 상자가 존재하지 않음(%d)", chestLoc, amount));
			return;
		}
		if(map.getTotalMoney() < amount)
		{
			logger.log(Level.WARNING, String.format("EconomyDataStorage.breakChest(): %s 존재하는 돈보다 많이 꺼냄2(%d)", chestLoc, amount - map.getTotalMoney()));
			return;
		}
		Bukkit.getServer().broadcastMessage(String.format("ChestBreak %s", chest.getLocation(), amount));
		// 만약 플레이어가 넣은 돈보다 많이 꺼내갔을 경우 가장 적은 돈을 넣은 플레이어의 돈부터 가져가도록 함.
		
		List<Map.Entry<UUID, Integer>> list = new LinkedList<>(map.eMap.entrySet());
		list.sort((o1, o2)->o1.getValue() - o2.getValue());
		int leftMoney = amount;
		for(Map.Entry<UUID, Integer> entry : list)
		{
			UUID key = entry.getKey();
			int value = entry.getValue();
			if(leftMoney - value <= 0)
			{
				map.decreaseEconomy(key, leftMoney);
				Bukkit.getServer().broadcastMessage(String.format("ChestToField %s (%d)",Bukkit.getPlayer(key).getName(), leftMoney));
				break;
			}
			else
			{
				map.decreaseEconomy(key, value);
				Bukkit.getServer().broadcastMessage(String.format("ChestToField %s (%d)",Bukkit.getPlayer(key).getName(), value));
				leftMoney -= value;
			}
		}
		this.chestDependEconomy.delKey(chestLoc);
	}
	
	private EconomyMapChild<Location, UUID> takeDoubleChest(DoubleChest chest)
	{
		EconomyMapChild<Location, UUID> map;
		DoubleChestInventory inv = (DoubleChestInventory) chest.getInventory();
		Location lLoc = inv.getLeftSide().getLocation();
		Location rLoc = inv.getRightSide().getLocation();
		map = this.chestDependEconomy.eMap.get(lLoc);
		if(map != null)
		{
			if(!this.chestDependEconomy.eMap.containsKey(rLoc))
			{
				this.chestDependEconomy.appendKey(rLoc, map);
			}
		}
		else
		{
			map = this.chestDependEconomy.eMap.get(rLoc);
			if(map != null)
			{
				this.chestDependEconomy.appendKey(lLoc, map);
			}
		}
		return map;
	}
	
	public void ItemDestroy(Item item)
	{
		
	}


}



