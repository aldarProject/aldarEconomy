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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import kr.dja.aldarEconomy.api.SystemID;
import kr.dja.aldarEconomy.economyState.data.MultipleEconomyMap;
import kr.dja.aldarEconomy.economyState.data.EconomyMap;
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
	
	public void playerToItem(Player player, Item item, int amount)
	{
		
	}
	
	public void itemToPlayer(Item item, Player player, int amount)
	{
		
	}
	
	public void chestToPlayer(Block chest, Player player, int amount)
	{
		Location chestLoc = chest.getLocation();
		UUID playerUID = player.getUniqueId();
		EconomyMap<UUID> chestUsers = this.chestDependEconomy.eMap.get(chestLoc);
		if(chestUsers == null)
		{//유저가 존재하지도 않는 돈을 꺼내가려고 할 때
			logger.log(Level.WARNING, String.format("EconomyDataStorage.chestToPlayer(): %s %s 존재하지 않는 돈 꺼냄(%d)", player.getName(), chestLoc, amount));
			return;
		}
		
		int playerMoney = this.chestDependEconomy.getMoney(chestLoc, playerUID);
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
				logger.log(Level.WARNING, String.format("EconomyDataStorage.chestToPlayer(): %s %s 존재하는 돈보다 많이 꺼냄(%d)", player.getName(), chestLoc, chestUsers.getTotalMoney() - amount));
				return;
			}
			// 지분에 맞춰 창고 장부를 변경해줌.
			// ex) A가 20원 넣고 B가 100원 넣고 C가 50원 넣었을 때 A가 80원을 뽑아가면
			// A는 20원 소모, B는 40원 소모, C는 20원 소모
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
	
	public void playerToDoubleChest(Player player, Block leftSide, Block rightSide, int amount)
	{
		
	}
	
	public void playerToChest(Player player, Block chest, int amount)
	{
		this.chestDependEconomy.increaseEconomy(chest.getLocation(), player.getUniqueId(), amount);
		Bukkit.getServer().broadcastMessage(String.format("PlayerToChest %s %s (%d)", player.getName(), chest.getLocation(), amount));
	}
	
	public void ItemDestroy(Item item)
	{
		
	}


}



