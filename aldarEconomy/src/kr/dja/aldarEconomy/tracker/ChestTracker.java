package kr.dja.aldarEconomy.tracker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import kr.dja.aldarEconomy.ConstraintChecker;
import kr.dja.aldarEconomy.dao.IntLocation;
import kr.dja.aldarEconomy.dataObject.multiKeyStorage.EconomyMapChild;
import kr.dja.aldarEconomy.dataObject.multiKeyStorage.MultipleEconomyMap;
import kr.dja.aldarEconomy.setting.MoneyMetadata;

public class ChestTracker
{// 창고에 누가 얼마 넣었고 누가 얼마 뺐는지 추적
	private final ConstraintChecker checker;
	private final MultipleEconomyMap<IntLocation, UUID> chestDependEconomy;
	private final Logger logger;
	
	private final Map<Inventory, OpenedChestMoneyInfo> openedChestInv;
	
	public ChestTracker(ConstraintChecker checker, MultipleEconomyMap<IntLocation, UUID> chestDependEconomy, Logger logger)
	{
		this.checker = checker;
		this.chestDependEconomy = chestDependEconomy;
		this.logger = logger;
		this.openedChestInv = new HashMap<>();
	}
	
	public void destroyBlock(Block b)
	{
		BlockState bs = b.getState();
		if(!(bs instanceof Container)) return;
		Container con = (Container)bs;
		Inventory chest = con.getInventory();
		OpenedChestMoneyInfo info = this.getMoneyInfo(chest);
		Location bLoc = b.getLocation();
		
		DoubleChestInventory doubleChest = this.getDoubleChestInfo(chest);
		int discountAmount = 0;
		if(doubleChest != null)
		{
			Inventory left = doubleChest.getLeftSide();
			Inventory right = doubleChest.getRightSide();
			if(left.getLocation().equals(bLoc))
			{
				discountAmount = this.getInventoryMoney(left);
				if(info != null) info.chestMoney -= discountAmount;
				this.destoryChest(b, discountAmount);
			}
			else if(right.getLocation().equals(bLoc))
			{
				discountAmount = this.getInventoryMoney(right);
				if(info != null) info.chestMoney -= discountAmount;
				this.destoryChest(b, discountAmount);
			}
		}
		else
		{
			discountAmount = this.getInventoryMoney(chest);
			if(info != null) info.chestMoney -= discountAmount;
			this.destoryChest(b, discountAmount);
		}
	}
	
	public void gainMoney(HumanEntity player, int amount)
	{
		Inventory openInv = player.getOpenInventory().getTopInventory();
		if(openInv == null) return;
		this.accessChestGainMoney(openInv, player, amount);
	}
	
	public void dropMoney(HumanEntity player, int amount)
	{
		Inventory openInv = player.getOpenInventory().getTopInventory();
		if(openInv == null) return;
		this.accessChestDropMoney(openInv, player, amount);
	}
	
	public void openChest(Inventory chest, HumanEntity player)
	{
		OpenedChestMoneyInfo info = this.getMoneyInfo(chest);
		if(info == null)
		{
			info = this.createMoneyInfo(chest);
		}
		int playerMoney = this.getInventoryMoney(player.getInventory());
		info.playerMoneyMap.put(player, playerMoney);
	}

	public void closeChest(Inventory chest, HumanEntity player)
	{
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
	
	private void chestToPlayer(EconomyMapChild<IntLocation, UUID> map, UUID playerUID, int amount)
	{
		int playerMoney = map.getMoney(playerUID);
		int otherMoney = amount - playerMoney;
		if(otherMoney <= 0)
		{// 창고에서 자신이 넣은만큼만 꺼내갔을 경우
			map.decreaseEconomy(playerUID, amount);
			Bukkit.getServer().broadcastMessage(String.format("PlayerAccess %s (%d)",Bukkit.getPlayer(playerUID).getName(), amount));
		}
		else
		{// 창고에 남이 넣은 돈까지 꺼내가는 경우
			map.decreaseEconomy(playerUID, playerMoney);
			if(map.getTotalMoney() - otherMoney < 0)
			{
				logger.log(Level.WARNING, String.format("EconomyDataStorage.chestToPlayer(): %s 존재하는 돈보다 많이 꺼냄(%d)", Bukkit.getPlayer(playerUID).getName(), map.getTotalMoney() - amount));
				return;
			}
			// 만약 플레이어가 넣은 돈보다 많이 꺼내갔을 경우 가장 적은 돈을 넣은 플레이어의 돈부터 가져가도록 함.
			int leftMoney = otherMoney;
			
			List<Map.Entry<UUID, Integer>> list = new LinkedList<>(map.eMap.entrySet());
			list.sort((o1, o2)->o1.getValue() - o2.getValue());
			
			for(Map.Entry<UUID, Integer> entry : list)
			{
				UUID key = entry.getKey();
				int value = entry.getValue();
				if(key.equals(playerUID)) continue;
				if(leftMoney - value <= 0)
				{
					map.decreaseEconomy(key, leftMoney);
					Bukkit.getServer().broadcastMessage(String.format("PlayerChestTrade %s to %s -%d(%d)",Bukkit.getPlayer(key).getName(), Bukkit.getPlayer(playerUID).getName(), leftMoney, map.getTotalMoney()));
					break;
				}
				else
				{
					map.decreaseEconomy(key, value);
					Bukkit.getServer().broadcastMessage(String.format("PlayerChestTrade %s to %s -%d(%d)",Bukkit.getPlayer(key).getName(), Bukkit.getPlayer(playerUID).getName(), value, map.getTotalMoney()));
					leftMoney -= value;
				}
			}
		}
	}
	
	private void chestToPlayer(DoubleChest chest, HumanEntity player, int amount)
	{
		EconomyMapChild<IntLocation, UUID> map = this.takeDoubleChest(chest);
		if(map == null)
		{//유저가 존재하지도 않는 돈을 꺼내가려고 할 때
			logger.log(Level.WARNING, String.format("EconomyDataStorage.chestToPlayer(): %s %s 존재하지 않는 돈 꺼냄(%d)", player.getName(), chest.getLocation(), amount));
			return;
		}
		else
		{
			this.chestToPlayer(map, player.getUniqueId(), amount);
		}
	}
	
	private void chestToPlayer(Chest chest, HumanEntity player, int amount)
	{
		IntLocation loc = new IntLocation(chest.getLocation());
		EconomyMapChild<IntLocation, UUID> map = this.chestDependEconomy.eMap.get(loc);
		if(map == null)
		{//유저가 존재하지도 않는 돈을 꺼내가려고 할 때
			logger.log(Level.WARNING, String.format("EconomyDataStorage.chestToPlayer(): %s %s 존재하지 않는 돈 꺼냄(%d)", player.getName(), amount));
			return;
		}
		this.chestToPlayer(map, player.getUniqueId(), amount);
	}
	
	private void playerToChest(HumanEntity player, Chest chest, int amount)
	{
		IntLocation loc = new IntLocation(chest.getLocation());
		EconomyMapChild<IntLocation, UUID> map = this.chestDependEconomy.increaseEconomy(loc, player.getUniqueId(), amount);
		Bukkit.getServer().broadcastMessage(String.format("PlayerToChest %s %s +%d(%d)", player.getName(), loc, amount, map.getTotalMoney()));
	}
	
	private void playerToChest(HumanEntity player, DoubleChest chest, int amount)
	{
		EconomyMapChild<IntLocation, UUID> takeMap = this.takeDoubleChest(chest);
		DoubleChestInventory inv = (DoubleChestInventory)chest.getInventory();
		IntLocation leftLoc = new IntLocation(inv.getLeftSide().getLocation());
		IntLocation rightLoc = new IntLocation(inv.getRightSide().getLocation());
		EconomyMapChild<IntLocation, UUID> increaseMap = this.chestDependEconomy.increaseEconomy(leftLoc, player.getUniqueId(), amount);
		if(takeMap == null)
		{
			this.chestDependEconomy.appendKey(rightLoc, increaseMap);
		}
		Bukkit.getServer().broadcastMessage(String.format("PlayerToChest %s %s %s +%d(%d)", player.getName(), leftLoc, rightLoc, amount, increaseMap.getTotalMoney()));
	}
	
	private void destoryChest(Block chest, int amount)
	{
		IntLocation chestLoc = new IntLocation(chest.getLocation());
		EconomyMapChild<IntLocation, UUID> map = this.chestDependEconomy.eMap.get(chestLoc);
		if(map == null)
		{
			if(amount > 0)
			{
				logger.log(Level.WARNING, String.format("EconomyDataStorage.breakChest(): %s 상자가 존재하지 않음(%d)", chestLoc, amount));
			}
			return;
		}
		if(map.getTotalMoney() < amount)
		{
			logger.log(Level.WARNING, String.format("EconomyDataStorage.breakChest(): %s 존재하는 돈보다 많이 꺼냄2(%d)", chestLoc, amount - map.getTotalMoney()));
			//돈 사라짐
			return;
		}
		Bukkit.getServer().broadcastMessage(String.format("ChestBreak %s", chestLoc, amount));
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
	
	private EconomyMapChild<IntLocation, UUID> takeDoubleChest(DoubleChest chest)
	{
		EconomyMapChild<IntLocation, UUID> map;
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
	
	private void accessChestGainMoney(Inventory chest, HumanEntity player, int amount)
	{
		OpenedChestMoneyInfo info = this.getMoneyInfo(chest);
		if(info == null) return;
		int playerMoney = info.playerMoneyMap.getOrDefault(player, -1);
		if(playerMoney == -1)
		{
			this.logger.log(Level.WARNING, "accessChestGainItem 추적 실패");
			return;
		}
		
		info.playerMoneyMap.put(player, playerMoney + amount);
	}
	
	private void accessChestDropMoney(Inventory chest, HumanEntity player, int amount)
	{
		OpenedChestMoneyInfo info = this.getMoneyInfo(chest);
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
					this.playerToChest(lookupPlayer, (DoubleChest)holder, playerBeforeMoney - playerNowMoney);
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
					this.chestToPlayer((DoubleChest)holder, lookupPlayer, playerNowMoney - playerBeforeMoney);
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

class OpenedChestMoneyInfo
{
	public Inventory masterInven;// 현재 접근중인 인벤토리의 주 인벤토리(더블 체스트 고려)
	public int chestMoney;
	public final Map<HumanEntity, Integer> playerMoneyMap;
	
	public OpenedChestMoneyInfo()
	{
		this.playerMoneyMap = new HashMap<>();
	}
}