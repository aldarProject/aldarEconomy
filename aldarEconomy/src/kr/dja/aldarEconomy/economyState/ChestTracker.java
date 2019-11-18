package kr.dja.aldarEconomy.economyState;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import kr.dja.aldarEconomy.ConstraintChecker;
import kr.dja.aldarEconomy.setting.MoneyMetadata;

public class ChestTracker
{
	private final ConstraintChecker checker;
	private final EconomyDataStorage storage;
	private final Logger logger;
	
	private final Map<Inventory, OpenedChestMoneyInfo> openedChestInv;
	
	public ChestTracker(ConstraintChecker checker, EconomyDataStorage storage, Logger logger)
	{
		this.checker = checker;
		this.storage = storage;
		this.logger = logger;
		this.openedChestInv = new HashMap<>();
	}
	
	public void blockBreak(Block b)
	{
		BlockState bs = b.getState();
		if(!(bs instanceof Container)) return;
		Container con = (Container)bs;
		Inventory chest = con.getInventory();
		OpenedChestMoneyInfo info = this.getMoneyInfo(chest);
		if(info == null) return;
		this.openedChestBreak(b.getLocation(), chest, info);
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
	
	private void openedChestBreak(Location loc, Inventory chest, OpenedChestMoneyInfo info)
	{
		DoubleChestInventory doubleChest = this.getDoubleChestInfo(chest);
		int discountAmount = 0;
		if(doubleChest != null)
		{
			Inventory left = doubleChest.getLeftSide();
			Inventory right = doubleChest.getRightSide();
			if(left.getLocation().equals(loc))
			{
				discountAmount = this.getInventoryMoney(left);
				info.chestMoney -= discountAmount;
			}
			else if(right.getLocation().equals(loc))
			{
				discountAmount = this.getInventoryMoney(right);
				info.chestMoney -= discountAmount;
			}
		}
		else
		{
			discountAmount = this.getInventoryMoney(chest);
			info.chestMoney -= discountAmount;
		}
		
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
				
				Bukkit.getServer().broadcastMessage(String.format("PlayerToChest %s %d",lookupPlayer.getName(), playerBeforeMoney - playerNowMoney));
				if(holder instanceof DoubleChest)
				{
					this.storage.playerToChest(lookupPlayer, (DoubleChest)holder, playerBeforeMoney - playerNowMoney);
				}
				else if(holder instanceof Chest)
				{
					this.storage.playerToChest(lookupPlayer, (Chest)holder, playerBeforeMoney - playerNowMoney);
				}
				else
				{
					logger.log(Level.WARNING, String.format("ChestTracker.chestMoneyCounting구현되지 않은 인벤토리 %s", holder.getClass().getName()));
				}				
			}
			else if(playerBeforeMoney - playerNowMoney < 0)
			{
				Bukkit.getServer().broadcastMessage(String.format("ChestToPlayer %s %d",lookupPlayer.getName(), playerNowMoney - playerBeforeMoney));
				
				if(holder instanceof DoubleChest)
				{
					this.storage.chestToPlayer((DoubleChest)holder,lookupPlayer, playerNowMoney - playerBeforeMoney);
				}
				else if(holder instanceof Chest)
				{
					this.storage.chestToPlayer((Chest)holder, lookupPlayer, playerNowMoney - playerBeforeMoney);
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
	
	private Chest getChest(Inventory inv)
	{
		if(inv.getHolder() instanceof Chest)
		{
			Chest chest = (Chest)inv.getHolder();
			return chest;
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
		if(info != null)
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