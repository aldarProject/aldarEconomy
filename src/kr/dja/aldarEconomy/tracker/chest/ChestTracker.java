package kr.dja.aldarEconomy.tracker.chest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;

import kr.dja.aldarEconomy.EconomyUtil;
import kr.dja.aldarEconomy.IntLocation;
import kr.dja.aldarEconomy.api.token.APITokenManager;
import kr.dja.aldarEconomy.api.token.SystemID;
import kr.dja.aldarEconomy.bank.TradeTracker;
import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.chest.ChestEconomyChild;
import kr.dja.aldarEconomy.dataObject.chest.ChestEconomyStorage;
import kr.dja.aldarEconomy.dataObject.chest.ChestWallet;
import kr.dja.aldarEconomy.dataObject.enderChest.EnderChestEconomyStorage;
import kr.dja.aldarEconomy.dataObject.player.PlayerEconomyStorage;
import kr.dja.aldarEconomy.tracker.item.ItemTracker;

public class ChestTracker
{// 창고에 누가 얼마 넣었고 누가 얼마 뺐는지 추적
	private final EconomyUtil util;
	private final ChestEconomyStorage chestStorage;
	private final PlayerEconomyStorage playerStorage;
	private final EnderChestEconomyStorage enderChestStorage;;
	private final TradeTracker tradeTracker;
	private final Logger logger;
	private final ItemTracker itemTracker;
	
	private final Set<HumanEntity> closeChestItemDropCheck;
	private final OpenedChestMoneyInfo openedChestInfo;
	
	public ChestTracker(ItemTracker itemTracker, EconomyUtil util, ChestEconomyStorage chestStorage, PlayerEconomyStorage playerStorage, EnderChestEconomyStorage enderChestStorage, TradeTracker tradeTracker, Logger logger)
	{
		this.util = util;
		this.chestStorage = chestStorage;
		this.playerStorage = playerStorage;
		this.enderChestStorage = enderChestStorage;
		this.tradeTracker = tradeTracker;
		this.logger = logger;
		this.itemTracker = itemTracker;
		this.openedChestInfo = new OpenedChestMoneyInfo(this.util);
		this.closeChestItemDropCheck = new HashSet<>();
	}
	
	public void onPlayerIncreaseMoney(HumanEntity player, int amount)
	{
		OpenedChestMoneyMember pinfo = this.openedChestInfo.getOpenedChestInfo(this.getPlayerTopInv(player));
		if(pinfo != null)
		{
			int pMoney = pinfo.playerMoneyMap.getOrDefault(player, -1);
			if(pMoney != -1)
			{
				pinfo.playerMoneyMap.put(player, pMoney + amount);
			}
		}
	}
	
	public void onPlayerDecreaseMoney(HumanEntity player, int amount)
	{
		OpenedChestMoneyMember pinfo = this.openedChestInfo.getOpenedChestInfo(this.getPlayerTopInv(player));
		if(pinfo != null)
		{
			int pMoney = pinfo.playerMoneyMap.getOrDefault(player, -1);
			if(pMoney != -1)
			{
				pinfo.playerMoneyMap.put(player, pMoney - amount);
			}
			this.chestMoneyCounting(pinfo);
		}
	}
	
	public void onChestMoneyToPlayer(Inventory chest, HumanEntity player, int amount)
	{
		Inventory ptop = this.getPlayerTopInv(player);
		if(chest.equals(ptop))
		{
			return;
		}
		
		OpenedChestMoneyMember info = this.openedChestInfo.getOpenedChestInfo(chest);
		this.onPlayerIncreaseMoney(player, amount);
		if(info != null)
		{
			info.chestMoney -= amount;
			this.chestMoneyCounting(info);
		}
		ChestEconomyChild map = this.findAndAlignChestMap(chest.getHolder());
		
		this.chestToPlayer(map, chest, player, amount);
	}
	
	public void onPlayerMoneyToChest(HumanEntity player, Inventory chest, int amount)
	{
		Inventory ptop = this.getPlayerTopInv(player);
		if(chest.equals(ptop))
		{
			return;
		}
		
		OpenedChestMoneyMember info = this.openedChestInfo.getOpenedChestInfo(chest);
		this.onPlayerDecreaseMoney(player, amount);
		if(info != null)
		{
			info.chestMoney += amount;
		}
		
		ChestEconomyChild map = this.findAndAlignChestMap(chest.getHolder());
		this.playerToChest(player, map, amount);
	}
	
	public void onIssuanceToChest(SystemID system, Inventory chest, int amount)
	{
		OpenedChestMoneyMember info = this.openedChestInfo.getOpenedChestInfo(chest);
		if(info != null)
		{
			info.chestMoney += amount;
		}
		ChestEconomyChild map = this.findAndAlignChestMap(chest.getHolder());
		this.chestStorage.increaseEconomy(map, system.uuid, amount, DependType.SYSTEM);
	}
	
	private Inventory getPlayerTopInv(HumanEntity player)
	{
		InventoryView v = player.getOpenInventory();
		if(v != null) return v.getTopInventory();
		return null;
	}
	
	public void onConsumeFromChest(Inventory chest, int amount, SystemID id, String cause, String args)
	{
		OpenedChestMoneyMember info = this.openedChestInfo.getOpenedChestInfo(chest);
		if(info != null)
		{
			info.chestMoney -= amount;
			this.chestMoneyCounting(info);
		}
		ChestEconomyChild map = this.findAndAlignChestMap(chest.getHolder());
		IntLocation intLoc = new IntLocation(chest.getLocation());
		if(map.getTotalMoney() < amount)
		{
			int chestMoney = this.util.getInventoryMoney(chest);
			int diff = chestMoney - map.getTotalMoney() + amount;
			this.chestStorage.increaseEconomy(map, APITokenManager.SYSTEM_TOKEN.uuid, diff, DependType.SYSTEM);
			this.tradeTracker.forceRebalancing(null, diff, "ON_CONSUME_FROM_CHEST", intLoc);
		}
		List<ChestWallet> list = new LinkedList<>(map.eMap.values());
		Collections.sort(list);
		int leftMoney = amount;
		for(ChestWallet wallet : list)
		{
			UUID depend = wallet.depend;
			int money = wallet.getMoney();
			if(leftMoney - money <= 0)
			{
				this.chestStorage.decreaseEconomy(map, depend, leftMoney);
				this.tradeTracker.normalConsume(id, depend, wallet.ownerType, leftMoney, intLoc, cause, args);
				leftMoney = 0;
				break;
			}
			else
			{
				this.chestStorage.decreaseEconomy(map, depend, money);
				this.tradeTracker.normalConsume(id, depend, wallet.ownerType, money, intLoc, cause, args);
				leftMoney -= money;
			}
		}
	}
	
	public void onDestroyBlock(Container con)
	{
		Inventory chest = con.getInventory();
		if(chest.getType() == InventoryType.ENDER_CHEST)
		{
			
		}
		else
		{
			OpenedChestMoneyMember info = this.openedChestInfo.getOpenedChestInfo(chest);
			Location bLoc = con.getLocation();
			ChestEconomyChild map = this.findAndAlignChestMap(chest.getHolder());
			int discountAmount = 0;
			DoubleChestInventory doubleChest = this.getDoubleChestInfo(chest);
			IntLocation intLoc = new IntLocation(bLoc);
			if(doubleChest != null)
			{
				Inventory left = doubleChest.getLeftSide();
				Inventory right = doubleChest.getRightSide();
				if(left.getLocation().equals(bLoc))
				{
					discountAmount = this.util.getInventoryMoney(left);
				}
				else if(right.getLocation().equals(bLoc))
				{
					discountAmount = this.util.getInventoryMoney(right);
				}
				this.chestStorage.delKey(intLoc);
			}
			else
			{
				discountAmount = this.util.getInventoryMoney(chest);
			}
			
			if(discountAmount != 0)
			{
				if(map.getTotalMoney() < discountAmount)
				{
					int chestMoney = this.util.getInventoryMoney(chest);
					int diff = chestMoney - map.getTotalMoney();
					this.chestStorage.increaseEconomy(map, APITokenManager.SYSTEM_TOKEN.uuid, diff, DependType.SYSTEM);
					this.tradeTracker.forceRebalancing(null, diff, "ON_DESTROY_BLOCK", intLoc);
					//logger.log(Level.WARNING, String.format("ChestTracker.onDestroyBlock(): %s 존재하지 않는 돈 꺼냄(%d)", intLoc, diff));
				}
				if(info != null) info.chestMoney -= discountAmount;
				DestroyChestResult r = this.destoryChestCheckEconomy(map, discountAmount);
				// 창고를 부술때는 chestClose이벤트가 먼저 발생하므로 계산 필요 없
				this.itemTracker.onChestBreak(r);
			}
		}
		
	}
	
	public void onPlayerGainMoney(HumanEntity player, int amount)
	{
		Inventory openInv = player.getOpenInventory().getTopInventory();
		if(openInv == null) return;
		
		if(openInv.getType() == InventoryType.ENDER_CHEST)
		{
			this.enderChestCounting(player);
		}
		else
		{
			OpenedChestMoneyMember info = this.openedChestInfo.getOpenedChestInfo(openInv);
			if(info == null) return;
			int playerMoney = info.playerMoneyMap.getOrDefault(player, -1);
			if(playerMoney == -1)
			{
				this.logger.log(Level.WARNING, "accessChestGainItem 추적 실패");
				return;
			}
			
			info.playerMoneyMap.put(player, playerMoney + amount);
		}
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
		if(openInv.getType() == InventoryType.ENDER_CHEST)
		{
			this.enderChestCounting(player);
		}
		else
		{
			OpenedChestMoneyMember info = this.openedChestInfo.getOpenedChestInfo(openInv);
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
	}
	
	public void onOpenEconomyChest(Inventory chest, HumanEntity player)
	{
		if(chest.getType() == InventoryType.ENDER_CHEST)
		{
			UUID playerUID = player.getUniqueId();
			int chestMoney = this.util.getInventoryMoney(chest);
			int dbMoney = this.enderChestStorage.getMoney(playerUID);
			if(dbMoney != chestMoney)
			{
				if(dbMoney < chestMoney)
				{//chestMoney가 더 클때(즉, db의 돈을 더해야함)
					this.enderChestStorage.increaseEconomy(playerUID, chestMoney - dbMoney);
					this.tradeTracker.forceRebalancing(playerUID, chestMoney - dbMoney, "ENDERCHEST_OPEN", new IntLocation(player.getLocation()));
				}
				else
				{//chestMoney가 더 작을때(즉, db의 돈을 빼야함)
					this.enderChestStorage.decreaseEconomy(playerUID, dbMoney - chestMoney);
					this.tradeTracker.forceRebalancing(playerUID, chestMoney - dbMoney, "ENDERCHEST_OPEN", new IntLocation(player.getLocation()));
				}
			}
			
		}
		else
		{
			OpenedChestMoneyMember info = this.openedChestInfo.getOpenedChestInfo(chest);
			if(info == null)
			{
				info = this.openedChestInfo.createOpenedChestInfo(chest);
			}
			int playerMoney = this.util.getInventoryMoney(player.getInventory());
			info.playerMoneyMap.put(player, playerMoney);
		}
		
	}

	public void onCloseEconomyChest(Inventory chest, HumanEntity player)
	{
		if(chest.getType() == InventoryType.ENDER_CHEST)
		{
			this.enderChestCounting(player);
		}
		else
		{
			if(this.util.isMoney(player.getItemOnCursor()) != null)
			{
				this.closeChestItemDropCheck.add(player);
			}
			
			OpenedChestMoneyMember info = this.openedChestInfo.getOpenedChestInfo(chest);

			if(info == null)
			{
				this.logger.log(Level.WARNING, "closeChest 추적 실패");
				return;
			}
			
			this.chestMoneyCounting(info);
			info.playerMoneyMap.remove(player);
			if(info.playerMoneyMap.isEmpty())
			{
				this.openedChestInfo.removeOpenedChestInfo(chest);
			}
		}
		
	}
	
	private void enderChestCounting(HumanEntity player)
	{
		Inventory enderChest = player.getEnderChest();
		UUID playerUID = player.getUniqueId();
		int chestNow = this.util.getInventoryMoney(enderChest);
		int chestBefore = this.enderChestStorage.getMoney(playerUID);
		if(chestNow - chestBefore == 0) return;
		int amount;
		if(chestNow - chestBefore > 0)
		{// 엔더체스트에 돈 집어넣음
			amount = chestNow - chestBefore;
			int playerStorageMoney = this.playerStorage.getMoney(playerUID);
			if(playerStorageMoney < amount)
			{
				int invMoney = this.util.getPlayerInventoryMoney(player);
				this.playerStorage.increaseEconomy(playerUID, invMoney - playerStorageMoney + amount);
				this.tradeTracker.forceRebalancing(playerUID, invMoney - playerStorageMoney + amount, "PLAYER_TO_ENDERCHEST", new IntLocation(player.getLocation()));
			}
			this.playerStorage.decreaseEconomy(playerUID, amount);
			this.enderChestStorage.increaseEconomy(playerUID, amount);
		}
		else
		{// 엔더체스트에서 돈 뺌
			amount = chestBefore - chestNow;
			this.enderChestStorage.decreaseEconomy(playerUID, amount);
			this.playerStorage.increaseEconomy(playerUID, amount);
		}
	}
	
	private void chestToPlayer(ChestEconomyChild map, Inventory inv, HumanEntity player, int amount)
	{
		UUID playerUID = player.getUniqueId();
		IntLocation chestLoc = new IntLocation(player.getLocation());
		if(map.getTotalMoney() < amount)
		{
			int diff = this.util.getInventoryMoney(inv) - map.getTotalMoney() + amount;
			this.chestStorage.increaseEconomy(map, playerUID, diff, DependType.PLAYER);
			this.tradeTracker.forceRebalancing(playerUID, diff, "CHEST_TO_PLAYER", chestLoc);
			//logger.log(Level.WARNING, String.format("ChestTracker.chestToPlayer(): %s 존재하지 않는 돈 꺼냄(%d)", player.getName(), diff));
		}
		
		int playerMoney = map.getMoney(playerUID);
		int otherMoney = amount - playerMoney;
		if(otherMoney <= 0)
		{// 창고에서 자신이 넣은만큼만 꺼내갔을 경우
			this.chestStorage.decreaseEconomy(map, playerUID, amount);
		}
		else
		{// 창고에 남이 넣은 돈까지 꺼내가는 경우
			this.chestStorage.decreaseEconomy(map, playerUID, playerMoney);
			// 만약 플레이어가 넣은 돈보다 많이 꺼내갔을 경우 가장 적은 돈을 넣은 플레이어의 돈부터 가져가도록 함.
			int leftMoney = otherMoney;
			
			List<ChestWallet> list = new LinkedList<>(map.eMap.values());
			Collections.sort(list);
			for(ChestWallet wallet : list)
			{
				int money = wallet.getMoney();
				if(wallet.depend.equals(playerUID)) continue;
				if(leftMoney - money <= 0)
				{
					this.chestStorage.decreaseEconomy(map, wallet.depend, leftMoney);
					this.tradeTracker.tradeLog(playerUID, DependType.PLAYER, wallet.depend, wallet.ownerType, leftMoney, chestLoc, TradeTracker.SYSTEM_CAUSE, "CHEST_TRADE");
					leftMoney = 0;
					break;
				}
				else
				{
					this.chestStorage.decreaseEconomy(map, wallet.depend, money);
					this.tradeTracker.tradeLog(playerUID, DependType.PLAYER, wallet.depend, wallet.ownerType, money, chestLoc, TradeTracker.SYSTEM_CAUSE, "CHEST_TRADE");
					leftMoney -= money;
				}
			}
		}
		this.playerStorage.increaseEconomy(playerUID, amount);
	}
	
	private void playerToChest(HumanEntity player, ChestEconomyChild map, int amount)
	{
		UUID playerUID = player.getUniqueId();
		int playerStorageMoney = this.playerStorage.getMoney(playerUID);
		if(playerStorageMoney < amount)
		{
			int invMoney = this.util.getPlayerInventoryMoney(player);
			this.playerStorage.increaseEconomy(playerUID, invMoney - playerStorageMoney + amount);
			this.tradeTracker.forceRebalancing(playerUID, invMoney - playerStorageMoney + amount, "PLAYER_TO_CHEST", new IntLocation(player.getLocation()));
		}
		this.playerStorage.decreaseEconomy(playerUID, amount);
		this.chestStorage.increaseEconomy(map, playerUID, amount, DependType.PLAYER);
		//Bukkit.getServer().broadcastMessage(String.format("PlayerToChest %s +%d(%d)", player.getName(), amount, map.getTotalMoney()));
	}
	
	private DestroyChestResult destoryChestCheckEconomy(ChestEconomyChild map, int amount)
	{
		//Bukkit.getServer().broadcastMessage(String.format("ChestBreak %d", amount));
		List<ChestWallet> list = new LinkedList<>(map.eMap.values());
		Collections.sort(list);
		int leftMoney = amount;
		List<DestroyChestResultMember> resultMembers = new ArrayList<>();

		for(ChestWallet wallet : list)
		{
			UUID depend = wallet.depend;
			int money = wallet.getMoney();
			if(leftMoney - money <= 0)
			{
				this.chestStorage.decreaseEconomy(map, depend, leftMoney);
				resultMembers.add(new DestroyChestResultMember(depend, wallet.ownerType, leftMoney));
				//Bukkit.getServer().broadcastMessage(String.format("ChestToField %s (%d)",depend, leftMoney));
				leftMoney = 0;
				break;
			}
			else
			{
				this.chestStorage.decreaseEconomy(map, depend, money);
				resultMembers.add(new DestroyChestResultMember(depend, wallet.ownerType, money));
				//Bukkit.getServer().broadcastMessage(String.format("ChestToField %s (%d)",depend, money));
				leftMoney -= money;
			}
		}
		DestroyChestResult r = new DestroyChestResult(amount - leftMoney, resultMembers);
		return r;
	}
	
	private ChestEconomyChild findAndAlignChestMap(InventoryHolder holder)
	{
		ChestEconomyChild map = null;
		if(holder instanceof DoubleChest)
		{
			DoubleChest chest = (DoubleChest)holder;
			
			DoubleChestInventory inv = (DoubleChestInventory) chest.getInventory();
			IntLocation lLoc = new IntLocation(inv.getLeftSide().getLocation());
			IntLocation rLoc = new IntLocation(inv.getRightSide().getLocation());
			ChestEconomyChild mapL = this.chestStorage.eMap.get(lLoc);
			ChestEconomyChild mapR = this.chestStorage.eMap.get(rLoc);
			if(mapL != null && mapR != null)
			{
				map = mapL;
			}
			else if(mapL == null && mapR != null)
			{
				map = mapR;
				this.chestStorage.appendKey(lLoc, mapR);
			}
			else if(mapL != null && mapR == null)
			{
				map = mapL;
				this.chestStorage.appendKey(rLoc, mapL);
			}
			else
			{
				map = this.chestStorage.createEconomyChild(lLoc);
				this.chestStorage.appendKey(rLoc, map);
			}
			
		}
		else if(holder instanceof Chest)
		{
			IntLocation loc = new IntLocation(((Chest) holder).getLocation());
			map = this.chestStorage.eMap.get(loc);
			if(map == null)
			{
				map = this.chestStorage.createEconomyChild(loc);
			}
			return map;
		}
		else
		{
			logger.log(Level.WARNING, String.format("ChestTracker.findAndAlignChestMap()구현되지 않은 인벤토리 %s", holder.getClass().getName()));
		}
		return map;

	}
	
	
	private void chestMoneyCounting(OpenedChestMoneyMember info)
	{
		int beforeChestMoney = info.chestMoney;
		info.chestMoney = this.util.getInventoryMoney(info.masterInven);
		int diff = info.chestMoney - beforeChestMoney;
		InventoryHolder holder = info.masterInven.getHolder();
		ChestEconomyChild map = this.findAndAlignChestMap(holder);
		for(HumanEntity lookupPlayer: info.playerMoneyMap.keySet())
		{
			int playerBeforeMoney = info.playerMoneyMap.get(lookupPlayer);
			int playerNowMoney = this.util.getPlayerInventoryMoney(lookupPlayer);
			if(playerBeforeMoney - playerNowMoney > 0)
			{
				this.playerToChest(lookupPlayer, map, playerBeforeMoney - playerNowMoney);
				diff += playerNowMoney - playerBeforeMoney;
				info.playerMoneyMap.put(lookupPlayer, playerNowMoney);
			}
			
		}
		for(HumanEntity lookupPlayer: info.playerMoneyMap.keySet())
		{
			int playerBeforeMoney = info.playerMoneyMap.get(lookupPlayer);
			int playerNowMoney = this.util.getPlayerInventoryMoney(lookupPlayer);
			if(playerBeforeMoney - playerNowMoney < 0)
			{
				this.chestToPlayer(map, info.masterInven, lookupPlayer, playerNowMoney - playerBeforeMoney);
				diff += playerNowMoney - playerBeforeMoney;
				info.playerMoneyMap.put(lookupPlayer, playerNowMoney);
			}
			
		}
		if(diff != 0)
		{
			this.logger.log(Level.WARNING, String.format("moneyCounting 오류 (%d)", diff));
		}
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
}
