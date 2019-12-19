package kr.dja.aldarEconomy.tracker.chest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import kr.dja.aldarEconomy.EconomyUtil;
import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.IntLocation;
import kr.dja.aldarEconomy.dataObject.chest.ChestEconomyChild;
import kr.dja.aldarEconomy.dataObject.chest.ChestEconomyStorage;
import kr.dja.aldarEconomy.dataObject.chest.ChestWallet;
import kr.dja.aldarEconomy.dataObject.player.PlayerEconomyStorage;
import kr.dja.aldarEconomy.setting.MoneyMetadata;
import kr.dja.aldarEconomy.tracker.item.ItemTracker;

public class ChestTracker
{// 창고에 누가 얼마 넣었고 누가 얼마 뺐는지 추적
	private final EconomyUtil checker;
	private final ChestEconomyStorage chestDependEconomy;
	private final PlayerEconomyStorage playerDependEconomy;
	private final Logger logger;
	
	private final Map<Inventory, OpenedChestMoneyInfo> openedChestInv;
	private final Set<HumanEntity> closeChestItemDropCheck;
	
	private final ItemTracker itemTracker;
	
	
	public ChestTracker(ItemTracker itemTracker, EconomyUtil util, ChestEconomyStorage chestDependEconomy, PlayerEconomyStorage playerDependEconomy, Logger logger)
	{
		this.checker = util;
		this.chestDependEconomy = chestDependEconomy;
		this.playerDependEconomy = playerDependEconomy;
		this.logger = logger;
		this.openedChestInv = new HashMap<>();
		this.closeChestItemDropCheck = new HashSet<>();
		this.itemTracker = itemTracker;
		
	}
	
	public boolean isOpenedEconomyChest(Inventory chest)
	{
		return this.getMoneyInfo(chest) != null;
	}
	
	public void onDestroyBlock(Block b)
	{
		BlockState bs = b.getState();
		if(!(bs instanceof Container)) return;
		Container con = (Container)bs;
		Inventory chest = con.getInventory();
		OpenedChestMoneyInfo info = this.getMoneyInfo(chest);
		Location bLoc = b.getLocation();
		
		DoubleChestInventory doubleChest = this.getDoubleChestInfo(chest);
		int discountAmount = 0;
		DestroyChestResult r = null;
		if(doubleChest != null)
		{
			Inventory left = doubleChest.getLeftSide();
			Inventory right = doubleChest.getRightSide();
			if(left.getLocation().equals(bLoc))
			{
				discountAmount = this.getInventoryMoney(left);
				if(info != null) info.chestMoney -= discountAmount;
				r = this.destoryChest(b, discountAmount);
			}
			else if(right.getLocation().equals(bLoc))
			{
				discountAmount = this.getInventoryMoney(right);
				if(info != null) info.chestMoney -= discountAmount;
				r = this.destoryChest(b, discountAmount);
			}
		}
		else
		{
			discountAmount = this.getInventoryMoney(chest);
			if(info != null) info.chestMoney -= discountAmount;
			r = this.destoryChest(b, discountAmount);
		}
		if(r != null)
		{
			this.itemTracker.onChestBreak(r);
		}
	}
	
	public void onPlayerGainMoney(HumanEntity player, int amount)
	{
		Inventory openInv = player.getOpenInventory().getTopInventory();
		if(openInv == null) return;
		
		OpenedChestMoneyInfo info = this.getMoneyInfo(openInv);
		if(info == null) return;
		int playerMoney = info.playerMoneyMap.getOrDefault(player, -1);
		if(playerMoney == -1)
		{
			this.logger.log(Level.WARNING, "accessChestGainItem 추적 실패");
			return;
		}
		
		
		info.playerMoneyMap.put(player, playerMoney + amount);
	}
	
	public void onPlayerDropMoney(HumanEntity player, int amount)
	{
		if(this.closeChestItemDropCheck.contains(player))
		{
			this.closeChestItemDropCheck.remove(player);
			return;
		}
		
		Inventory openInv = player.getOpenInventory().getTopInventory();
		if(openInv == null) return;
		OpenedChestMoneyInfo info = this.getMoneyInfo(openInv);
		if(info == null) return;
		int playerMoney = info.playerMoneyMap.getOrDefault(player, -1);
		if(playerMoney == -1)
		{
			this.logger.log(Level.WARNING, "accessChestDropItem 추적 실패");
			return;
		}
		info.playerMoneyMap.put(player, playerMoney - amount);
		this.chestMoneyCounting(info);
	}
	
	public void onOpenChest(Inventory chest, HumanEntity player)
	{
		OpenedChestMoneyInfo info = this.getMoneyInfo(chest);
		if(info == null)
		{
			info = this.createMoneyInfo(chest);
		}
		int playerMoney = this.getInventoryMoney(player.getInventory());
		info.playerMoneyMap.put(player, playerMoney);
	}

	public void onCloseChest(Inventory chest, HumanEntity player)
	{
		if(this.checker.isMoney(player.getItemOnCursor()) != null)
		{
			this.closeChestItemDropCheck.add(player);
		}
		
		OpenedChestMoneyInfo info = this.getMoneyInfo(chest);

		if(info == null)
		{
			this.logger.log(Level.WARNING, "closeChest 추적 실패");
			return;
		}
		
		this.chestMoneyCounting(info);
		info.playerMoneyMap.remove(player);
		if(info.playerMoneyMap.isEmpty())
		{
			this.removeMoneyInfo(chest);
		}
	}
	
	private void chestToPlayerAction(ChestEconomyChild map, UUID playerUID, int amount)
	{
		int playerMoney = map.getMoney(playerUID);
		int otherMoney = amount - playerMoney;
		if(otherMoney <= 0)
		{// 창고에서 자신이 넣은만큼만 꺼내갔을 경우
			map.decreaseEconomy(playerUID, amount);
			this.playerDependEconomy.increaseEconomy(playerUID, amount);
			Bukkit.getServer().broadcastMessage(String.format("ChestToPlayer %s (%d)",Bukkit.getPlayer(playerUID).getName(), amount));
		}
		else
		{// 창고에 남이 넣은 돈까지 꺼내가는 경우
			map.decreaseEconomy(playerUID, playerMoney);
			this.playerDependEconomy.increaseEconomy(playerUID, amount);
			if(map.getTotalMoney() - otherMoney < 0)
			{
				logger.log(Level.WARNING, String.format("EconomyDataStorage.chestToPlayer(): %s 존재하는 돈보다 많이 꺼냄(%d)", Bukkit.getPlayer(playerUID).getName(), map.getTotalMoney() - amount));
				return;
			}
			// 만약 플레이어가 넣은 돈보다 많이 꺼내갔을 경우 가장 적은 돈을 넣은 플레이어의 돈부터 가져가도록 함.
			int leftMoney = otherMoney;
			
			List<ChestWallet> list = new LinkedList<>(map.eMap.values());
			Collections.sort(list);
			
			for(ChestWallet wallet : list)
			{
				UUID key = wallet.depend;
				int money = wallet.getMoney();
				if(key.equals(playerUID)) continue;
				if(leftMoney - money <= 0)
				{
					map.decreaseEconomy(key, leftMoney);
					this.playerDependEconomy.decreaseEconomy(key, leftMoney);
					Bukkit.getServer().broadcastMessage(String.format("PlayerChestTrade %s to %s -%d(%d)",Bukkit.getPlayer(key).getName(), Bukkit.getPlayer(playerUID).getName(), leftMoney, map.getTotalMoney()));
					break;
				}
				else
				{
					map.decreaseEconomy(key, money);
					this.playerDependEconomy.decreaseEconomy(key, money);
					Bukkit.getServer().broadcastMessage(String.format("PlayerChestTrade %s to %s -%d(%d)",Bukkit.getPlayer(key).getName(), Bukkit.getPlayer(playerUID).getName(), money, map.getTotalMoney()));
					leftMoney -= money;
				}
			}
		}
	}
	
	private void doubleChestToPlayer(DoubleChest chest, HumanEntity player, int amount)
	{
		ChestEconomyChild map = this.takeDoubleChest(chest);
		if(map == null)
		{//유저가 존재하지도 않는 돈을 꺼내가려고 할 때
			logger.log(Level.WARNING, String.format("EconomyDataStorage.chestToPlayer(): %s %s 존재하지 않는 돈 꺼냄(%d)", player.getName(), chest.getLocation(), amount));
			return;
		}

		this.chestToPlayerAction(map, player.getUniqueId(), amount);

	}
	
	private void chestToPlayer(Chest chest, HumanEntity player, int amount)
	{
		IntLocation loc = new IntLocation(chest.getLocation());
		ChestEconomyChild map = this.chestDependEconomy.eMap.get(loc);
		if(map == null)
		{//유저가 존재하지도 않는 돈을 꺼내가려고 할 때
			logger.log(Level.WARNING, String.format("EconomyDataStorage.chestToPlayer(): %s %s 존재하지 않는 돈 꺼냄(%d)", player.getName(), amount));
			return;
		}
		this.chestToPlayerAction(map, player.getUniqueId(), amount);
	}
	
	private void playerToChest(HumanEntity player, Chest chest, int amount)
	{
		IntLocation loc = new IntLocation(chest.getLocation());
		ChestEconomyChild map = this.chestDependEconomy.increaseEconomy(loc, player.getUniqueId(), amount, DependType.PLAYER);
		this.playerDependEconomy.decreaseEconomy(player.getUniqueId(), amount);
		Bukkit.getServer().broadcastMessage(String.format("PlayerToChest %s %s +%d(%d)", player.getName(), loc, amount, map.getTotalMoney()));
	}
	
	private void playerToDoubleChest(HumanEntity player, DoubleChest chest, int amount)
	{
		ChestEconomyChild takeMap = this.takeDoubleChest(chest);
		DoubleChestInventory inv = (DoubleChestInventory)chest.getInventory();
		IntLocation leftLoc = new IntLocation(inv.getLeftSide().getLocation());
		IntLocation rightLoc = new IntLocation(inv.getRightSide().getLocation());
		ChestEconomyChild increaseMap = this.chestDependEconomy.increaseEconomy(leftLoc, player.getUniqueId(), amount, DependType.PLAYER);
		if(takeMap == null)
		{
			this.chestDependEconomy.appendKey(rightLoc, increaseMap);
		}
		this.playerDependEconomy.decreaseEconomy(player.getUniqueId(), amount);
		Bukkit.getServer().broadcastMessage(String.format("PlayerToChest %s %s %s +%d(%d)", player.getName(), leftLoc, rightLoc, amount, increaseMap.getTotalMoney()));
	}
	
	private DestroyChestResult destoryChest(Block chest, int amount)
	{
		IntLocation chestLoc = new IntLocation(chest.getLocation());
		ChestEconomyChild map = this.chestDependEconomy.eMap.get(chestLoc);
		if(map == null)
		{
			if(amount > 0)
			{
				logger.log(Level.WARNING, String.format("EconomyDataStorage.breakChest(): %s 상자가 존재하지 않음(%d)", chestLoc, amount));
			}
			return null;
		}

		Bukkit.getServer().broadcastMessage(String.format("ChestBreak %s", chestLoc, amount));
		// 만약 플레이어가 넣은 돈보다 많이 꺼내갔을 경우 가장 적은 돈을 넣은 플레이어의 돈부터 가져가도록 함.
		
		
		List<ChestWallet> list = new LinkedList<>(map.eMap.values());
		Collections.sort(list);
		int leftMoney = amount;
		List<DestroyChestResultMember> resultMembers = new ArrayList<>();

		for(ChestWallet wallet : list)
		{
			UUID user = wallet.depend;
			int money = wallet.getMoney();
			if(leftMoney - money <= 0)
			{
				map.decreaseEconomy(user, leftMoney);
				resultMembers.add(new DestroyChestResultMember(user, leftMoney));
				Bukkit.getServer().broadcastMessage(String.format("ChestToField %s (%d)",Bukkit.getPlayer(user).getName(), leftMoney));
				break;
			}
			else
			{
				map.decreaseEconomy(user, money);
				resultMembers.add(new DestroyChestResultMember(user, money));
				Bukkit.getServer().broadcastMessage(String.format("ChestToField %s (%d)",Bukkit.getPlayer(user).getName(), money));
				leftMoney -= money;
			}
		}
		
		if(leftMoney > 0)
		{
			logger.log(Level.WARNING, String.format("EconomyDataStorage.breakChest(): %s 존재하는 돈보다 많이 꺼냄2(%d)", chestLoc, amount - map.getTotalMoney()));
			
		}
		DestroyChestResult r = new DestroyChestResult(amount - leftMoney, resultMembers);
		this.chestDependEconomy.delKey(chestLoc);
		return r;
	}
	
	private ChestEconomyChild takeDoubleChest(DoubleChest chest)
	{
		ChestEconomyChild map;
		DoubleChestInventory inv = (DoubleChestInventory) chest.getInventory();
		IntLocation lLoc = new IntLocation(inv.getLeftSide().getLocation());
		IntLocation rLoc = new IntLocation(inv.getRightSide().getLocation());
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
	
	private void chestMoneyCounting(OpenedChestMoneyInfo info)
	{
		int beforeChestMoney = info.chestMoney;
		info.chestMoney = this.getInventoryMoney(info.masterInven);
		int diff = info.chestMoney - beforeChestMoney;
		InventoryHolder holder = info.masterInven.getHolder();
		for(HumanEntity lookupPlayer: info.playerMoneyMap.keySet())
		{
			int playerBeforeMoney = info.playerMoneyMap.get(lookupPlayer);
			int playerNowMoney = this.getPlayerInventoryMoney(lookupPlayer);
			if(playerBeforeMoney - playerNowMoney > 0)
			{
				if(holder instanceof DoubleChest)
				{
					this.playerToDoubleChest(lookupPlayer, (DoubleChest)holder, playerBeforeMoney - playerNowMoney);
				}
				else if(holder instanceof Chest)
				{
					this.playerToChest(lookupPlayer, (Chest)holder, playerBeforeMoney - playerNowMoney);
				}
				else
				{
					logger.log(Level.WARNING, String.format("ChestTracker.chestMoneyCounting구현되지 않은 인벤토리 %s", holder.getClass().getName()));
				}			
			}
			else if(playerBeforeMoney - playerNowMoney < 0)
			{
				if(holder instanceof DoubleChest)
				{
					this.doubleChestToPlayer((DoubleChest)holder, lookupPlayer, playerNowMoney - playerBeforeMoney);
				}
				else if(holder instanceof Chest)
				{
					this.chestToPlayer((Chest)holder, lookupPlayer, playerNowMoney - playerBeforeMoney);
				}
				else
				{
					logger.log(Level.WARNING, String.format("ChestTracker.chestMoneyCounting구현되지 않은 인벤토리 %s", holder.getClass().getName()));
				}
			}
			diff += playerNowMoney - playerBeforeMoney;
			info.playerMoneyMap.put(lookupPlayer, playerNowMoney);
			
			
		}
		if(diff != 0)
		{
			this.logger.log(Level.WARNING, String.format("moneyCounting 오류 (%d)", diff));
		}

	}
	
	private int getInventoryMoney(Inventory inv)
	{
		int sum = 0;
		for(ItemStack stack : inv.getContents())
		{
			MoneyMetadata moneyInfo = this.checker.isMoney(stack);
			if(moneyInfo == null) continue;
			sum += moneyInfo.value * stack.getAmount();
		}
		return sum;
	}
	
	private int getPlayerInventoryMoney(HumanEntity p)
	{
		int sum = this.getInventoryMoney(p.getInventory());
		ItemStack cursorStack = p.getItemOnCursor();
		MoneyMetadata moneyMeta = this.checker.isMoney(cursorStack);
		if(moneyMeta != null)
		{
			sum += moneyMeta.value * cursorStack.getAmount();
		}
		return sum;
	}
	
	private DoubleChestInventory getDoubleChestInfo(Inventory chest)
	{
		if(chest instanceof DoubleChestInventory)
		{
			DoubleChestInventory doubleInv = (DoubleChestInventory)chest;
			
			return doubleInv;
		}
		return null;
	}
	
	private OpenedChestMoneyInfo getMoneyInfo(Inventory chest)
	{
		OpenedChestMoneyInfo info = this.openedChestInv.get(chest);

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
	
	private OpenedChestMoneyInfo createMoneyInfo(Inventory chest)
	{
		OpenedChestMoneyInfo info = new OpenedChestMoneyInfo();
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
		info.chestMoney = this.getInventoryMoney(info.masterInven);
		return info;
	}
	
	private void removeMoneyInfo(Inventory chest)
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
