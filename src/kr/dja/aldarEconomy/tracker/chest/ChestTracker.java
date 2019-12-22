package kr.dja.aldarEconomy.tracker.chest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import kr.dja.aldarEconomy.EconomyUtil;
import kr.dja.aldarEconomy.api.APITokenManager;
import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.IntLocation;
import kr.dja.aldarEconomy.dataObject.chest.ChestEconomyChild;
import kr.dja.aldarEconomy.dataObject.chest.ChestEconomyStorage;
import kr.dja.aldarEconomy.dataObject.chest.ChestWallet;
import kr.dja.aldarEconomy.dataObject.player.PlayerEconomyStorage;
import kr.dja.aldarEconomy.tracker.item.ItemTracker;
import kr.dja.aldarEconomy.trade.TradeTracker;

public class ChestTracker
{// 창고에 누가 얼마 넣었고 누가 얼마 뺐는지 추적
	private final EconomyUtil util;
	private final ChestEconomyStorage chestDependEconomy;
	private final PlayerEconomyStorage playerDependEconomy;
	private final TradeTracker tradeTracker;
	private final Logger logger;
	private final ItemTracker itemTracker;
	
	private final Set<HumanEntity> closeChestItemDropCheck;
	private final OpenedChestMoneyInfo openedChestInfo;
	
	
	public ChestTracker(ItemTracker itemTracker, EconomyUtil util, ChestEconomyStorage chestDependEconomy, PlayerEconomyStorage playerDependEconomy, TradeTracker tradeTracker, Logger logger)
	{
		this.util = util;
		this.chestDependEconomy = chestDependEconomy;
		this.playerDependEconomy = playerDependEconomy;
		this.tradeTracker = tradeTracker;
		this.logger = logger;
		this.itemTracker = itemTracker;
		this.openedChestInfo = new OpenedChestMoneyInfo(this.util);
		this.closeChestItemDropCheck = new HashSet<>();
	}
	
	public boolean isOpenedEconomyChest(Inventory chest)
	{
		return this.openedChestInfo.getOpenedChestInfo(chest) != null;
	}
	
	public void onDestroyBlock(Container con)
	{
		Inventory chest = con.getInventory();
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
			this.chestDependEconomy.delKey(intLoc);
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
				this.chestDependEconomy.increaseEconomy(map, APITokenManager.SYSTEM_TOKEN.uuid, diff, DependType.SYSTEM);
				this.tradeTracker.internalSystemLog(APITokenManager.SYSTEM_TOKEN.uuid, DependType.SYSTEM, diff, TradeTracker.ARGSTYPE_SYSTEM_FORCE_ISSUANCE, "ON_DESTROY_BLOCK,"+intLoc.toString());
				logger.log(Level.WARNING, String.format("ChestTracker.onDestroyBlock(): %s 존재하지 않는 돈 꺼냄(%d)", intLoc, diff));
			}
			if(info != null) info.chestMoney -= discountAmount;
			DestroyChestResult r = this.destoryChestCheckEconomy(map, discountAmount);
			this.itemTracker.onChestBreak(r);
		}
		Bukkit.getServer().broadcastMessage(map.toString() +"\n"+ intLoc.toString());
		
	}
	
	public void onPlayerGainMoney(HumanEntity player, int amount)
	{
		Inventory openInv = player.getOpenInventory().getTopInventory();
		if(openInv == null) return;
		
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
	
	public void onPlayerDropMoney(HumanEntity player, int amount)
	{
		if(this.closeChestItemDropCheck.contains(player))
		{
			this.closeChestItemDropCheck.remove(player);
			return;
		}
		
		Inventory openInv = player.getOpenInventory().getTopInventory();
		if(openInv == null) return;
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
	
	public void onOpenChest(Inventory chest, HumanEntity player)
	{
		OpenedChestMoneyMember info = this.openedChestInfo.getOpenedChestInfo(chest);
		if(info == null)
		{
			info = this.openedChestInfo.createOpenedChestInfo(chest);
		}
		int playerMoney = this.util.getInventoryMoney(player.getInventory());
		info.playerMoneyMap.put(player, playerMoney);
	}

	public void onCloseChest(Inventory chest, HumanEntity player)
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
	
	private void chestToPlayer(ChestEconomyChild map, Inventory inv, HumanEntity player, int amount)
	{
		UUID playerUID = player.getUniqueId();
		if(map.getTotalMoney() < amount)
		{
			int diff = this.util.getInventoryMoney(inv) - map.getTotalMoney() + amount;
			this.chestDependEconomy.increaseEconomy(map, playerUID, diff, DependType.PLAYER);
			this.tradeTracker.internalSystemLog(playerUID, DependType.PLAYER, diff, TradeTracker.ARGSTYPE_SYSTEM_FORCE_ISSUANCE, "CHEST_TO_PLAYER");
			logger.log(Level.WARNING, String.format("ChestTracker.chestToPlayer(): %s 존재하지 않는 돈 꺼냄(%d)", player.getName(), diff));
		}
		
		int playerMoney = map.getMoney(playerUID);
		int otherMoney = amount - playerMoney;
		if(otherMoney <= 0)
		{// 창고에서 자신이 넣은만큼만 꺼내갔을 경우
			this.chestDependEconomy.decreaseEconomy(map, playerUID, amount);
			Bukkit.getServer().broadcastMessage(String.format("ChestToPlayer %s (%d)",Bukkit.getPlayer(playerUID).getName(), amount));
		}
		else
		{// 창고에 남이 넣은 돈까지 꺼내가는 경우
			Bukkit.getServer().broadcastMessage(map + " " + playerUID + " " + playerMoney);
			this.chestDependEconomy.decreaseEconomy(map, playerUID, playerMoney);
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
					this.chestDependEconomy.decreaseEconomy(map, key, leftMoney);
					this.tradeTracker.tradeLog(playerUID, DependType.PLAYER, key, DependType.PLAYER, leftMoney, TradeTracker.ARGSTYPE_SENDMONEY_CHEST_TRADE, "CHEST_TRADE");
					Bukkit.getServer().broadcastMessage(String.format("PlayerChestTrade %s to %s -%d(%d)",Bukkit.getPlayer(key).getName(), Bukkit.getPlayer(playerUID).getName(), leftMoney, map.getTotalMoney()));
					leftMoney = 0;
					break;
				}
				else
				{
					this.chestDependEconomy.decreaseEconomy(map, key, money);
					this.tradeTracker.tradeLog(playerUID, DependType.PLAYER, key, DependType.PLAYER, money, TradeTracker.ARGSTYPE_SENDMONEY_CHEST_TRADE, "CHEST_TRADE");
					Bukkit.getServer().broadcastMessage(String.format("PlayerChestTrade %s to %s -%d(%d)",Bukkit.getPlayer(key).getName(), Bukkit.getPlayer(playerUID).getName(), money, map.getTotalMoney()));
					leftMoney -= money;
				}
			}
		}
		this.playerDependEconomy.increaseEconomy(playerUID, amount);
	}
	
	private void playerToChest(HumanEntity player, ChestEconomyChild map, int amount)
	{
		UUID playerUID = player.getUniqueId();
		int remain = this.playerDependEconomy.decreaseEconomy(playerUID, amount);
		if(remain < 0)
		{
			int diff = this.util.getPlayerInventoryMoney(player);
			int realIssuance = diff + Math.abs(remain);
			logger.log(Level.WARNING, String.format("ChestTracker.playerToChest(): %s 존재하지 않는 돈 꺼냄 인벤남음%d, 실제발급(%d) %d", player.getName(), diff, realIssuance, remain));
			if(diff != 0)
			{
				this.playerDependEconomy.increaseEconomy(playerUID, diff);
				this.tradeTracker.internalSystemLog(player.getUniqueId(), DependType.PLAYER, realIssuance, TradeTracker.ARGSTYPE_SYSTEM_FORCE_ISSUANCE, "PLAYER_TO_CHEST");
			}
		}
		this.chestDependEconomy.increaseEconomy(map, playerUID, amount, DependType.PLAYER);
		Bukkit.getServer().broadcastMessage(String.format("PlayerToChest %s +%d(%d)", player.getName(), amount, map.getTotalMoney()));
	}
	
	private DestroyChestResult destoryChestCheckEconomy(ChestEconomyChild map, int amount)
	{
		Bukkit.getServer().broadcastMessage(String.format("ChestBreak %d", amount));
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
				this.chestDependEconomy.decreaseEconomy(map, depend, leftMoney);
				resultMembers.add(new DestroyChestResultMember(depend, wallet.ownerType, leftMoney));
				Bukkit.getServer().broadcastMessage(String.format("ChestToField %s (%d)",depend, leftMoney));
				leftMoney = 0;
				break;
			}
			else
			{
				this.chestDependEconomy.decreaseEconomy(map, depend, money);
				resultMembers.add(new DestroyChestResultMember(depend, wallet.ownerType, money));
				Bukkit.getServer().broadcastMessage(String.format("ChestToField %s (%d)",depend, money));
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
			ChestEconomyChild mapL = this.chestDependEconomy.eMap.get(lLoc);
			ChestEconomyChild mapR = this.chestDependEconomy.eMap.get(rLoc);
			if(mapL != null && mapR != null)
			{
				map = mapL;
			}
			else if(mapL == null && mapR != null)
			{
				map = mapR;
				this.chestDependEconomy.appendKey(lLoc, mapR);
			}
			else if(mapL != null && mapR == null)
			{
				map = mapL;
				this.chestDependEconomy.appendKey(rLoc, mapL);
			}
			else
			{
				map = this.chestDependEconomy.createEconomyChild(lLoc);
				this.chestDependEconomy.appendKey(rLoc, map);
			}
			
		}
		else if(holder instanceof Chest)
		{
			IntLocation loc = new IntLocation(((Chest) holder).getLocation());
			map = this.chestDependEconomy.eMap.get(loc);
			if(map == null)
			{
				map = this.chestDependEconomy.createEconomyChild(loc);
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
