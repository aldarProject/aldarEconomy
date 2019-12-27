package kr.dja.aldarEconomy.bank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.bergerkiller.bukkit.common.utils.ItemUtil;

import kr.dja.aldarEconomy.EconomyUtil;
import kr.dja.aldarEconomy.IntLocation;
import kr.dja.aldarEconomy.api.token.APITokenManager;
import kr.dja.aldarEconomy.api.token.SystemID;
import kr.dja.aldarEconomy.coininfo.CoinInfo;
import kr.dja.aldarEconomy.coininfo.CoinMetadata;
import kr.dja.aldarEconomy.data.EconomyDataStorage;
import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.tracker.chest.ChestTracker;

public class Bank
{//돈을 발급해서 뿌려줌
	
	public static final int MAX_PLAYER_INVENTORY_SIZE = 36;
	
	private final double ITEM_DIST = 0.5;
	private final CoinInfo info;
	private final EconomyUtil util;
	private final EconomyDataStorage dataStorage;
	private final ChestTracker chestTracker;
	private final TradeTracker tradeTracker;
	private final int[] coinMaxStackSize;
	
	public Bank(CoinInfo info, EconomyUtil util, EconomyDataStorage dataStorage, ChestTracker chestTracker, TradeTracker tradeTracker)
	{
		this.info = info;
		this.util = util;
		this.dataStorage = dataStorage;
		this.chestTracker = chestTracker;
		this.tradeTracker = tradeTracker;
		this.coinMaxStackSize = new int[this.info.count];
		for(int i = 0; i < this.info.count; ++i)
		{
			coinMaxStackSize[i] = this.info.moneyList.get(i).maxStack;
		}
	}
	
	public EconomyActionResult issuanceToPlayer(SystemID id, HumanEntity player, int amount, String cause, String args)
	{
		int[] invMoneyTypeCountArr = new int[this.info.count];
		int[] needMoneyTypeArr = new int[this.info.count];
		
		Inventory playerInv = player.getInventory();
		ItemStack[] invContents = playerInv.getContents();
		ModifyInventoryInfo[] infoArr = new ModifyInventoryInfo[invContents.length];
		this.initInventoryEconomyMap(invMoneyTypeCountArr, invContents, infoArr);
		if(this.getIncreaseMoneyInfo(amount, needMoneyTypeArr) != 0)
		{// 지불할 소액권 없음.
			return EconomyActionResult.unitTooSmall;
		}
		
		if(this.issuanceReadyTask(needMoneyTypeArr, infoArr, MAX_PLAYER_INVENTORY_SIZE) != 0)
		{
			return EconomyActionResult.insufficientSpace;
		}
		
		UUID playerUID = player.getUniqueId();
		IntLocation intLoc = new IntLocation(player.getLocation());
		
		this.modifyInventory(player.getInventory(), infoArr);
		this.tradeTracker.normalIssuance(id, amount, intLoc, cause, args);
		this.dataStorage.playerDependEconomy.increaseEconomy(playerUID, amount);
		
		return EconomyActionResult.OK;
	}
	
	public EconomyActionResult consumeFromPlayer(SystemID id, HumanEntity player, int amount, String cause, String args)
	{
		int[] invMoneyTypeCountArr = new int[this.info.count];
		int[] needMoneyTypeArr = new int[this.info.count];
		
		Inventory playerInv = player.getInventory();
		ItemStack[] invContents = playerInv.getContents();
		ModifyInventoryInfo[] infoArr = new ModifyInventoryInfo[invContents.length];
		
		int invMoney = this.initInventoryEconomyMap(invMoneyTypeCountArr, invContents, infoArr);
		if(invMoney < amount)
		{//돈부족
			return EconomyActionResult.insufficientMoney;
		}

		if(this.getDecreaseMoneyInfo(amount, invMoneyTypeCountArr, needMoneyTypeArr) != 0)
		{// 지불할 소액권 없음.
			return EconomyActionResult.unitTooSmall;
		}
			
		this.consumeReadyTask(needMoneyTypeArr, infoArr);
		
		if(this.issuanceReadyTask(needMoneyTypeArr, infoArr, MAX_PLAYER_INVENTORY_SIZE) != 0)
		{
			return EconomyActionResult.insufficientSpace;
		}
		
		IntLocation intLoc = new IntLocation(player.getLocation());
		UUID playerUID = player.getUniqueId();
		int playerStorageMoney = this.dataStorage.playerDependEconomy.getMoney(playerUID);
		if(playerStorageMoney < amount)
		{
			this.dataStorage.playerDependEconomy.increaseEconomy(playerUID, invMoney - playerStorageMoney);
			this.tradeTracker.forceRebalancing(playerUID, invMoney - playerStorageMoney, "CONSUME_MONEY", intLoc);
		}
		this.modifyInventory(playerInv, infoArr);
		this.dataStorage.playerDependEconomy.decreaseEconomy(playerUID, amount);
		this.tradeTracker.normalConsume(id,playerUID,DependType.PLAYER, amount, intLoc, cause, args);
		return EconomyActionResult.OK;
	}
	
	public EconomyActionResult issuanceToItem(SystemID id, Location loc, int amount, String cause, String args)
	{
		World w = loc.getWorld();
		
		ItemStack[] moneyItemArr = this.getMoneyItemStack(amount);
		if(moneyItemArr == null) return EconomyActionResult.unitTooSmall;

		int sqSide = (int)Math.ceil(Math.sqrt(moneyItemArr.length));
		double offset = (sqSide * ITEM_DIST) / 2.0;
		for(int i = 0; i < sqSide; ++i)
		{
			for(int j = 0; j < sqSide; ++j)
			{
				int index = i * sqSide + j;
				if(index >= moneyItemArr.length) break;
				double xoffset = (i * ITEM_DIST) - offset;
				double zoffset = (j * ITEM_DIST) - offset;
				Location l = new Location(loc.getWorld(), loc.getX() + xoffset, loc.getY(), loc.getZ() + zoffset);
				Item item = w.dropItem(l, moneyItemArr[index]);
				this.dataStorage.itemEconomyStorage.increaseEconomy(
						item.getUniqueId()
						, id.uuid, DependType.SYSTEM
						, this.util.getValue(moneyItemArr[index]));
			}
		}
		this.tradeTracker.normalIssuance(id, amount, new IntLocation(loc), cause, args);
		return EconomyActionResult.OK;
	}
	
	public EconomyActionResult issuanceToChest(SystemID id, Inventory chestInv, int amount, String cause, String args)
	{
		int[] invMoneyTypeCountArr = new int[this.info.count];
		int[] needMoneyTypeArr = new int[this.info.count];
		
		ItemStack[] invContents = chestInv.getContents();
		ModifyInventoryInfo[] infoArr = new ModifyInventoryInfo[invContents.length];
		this.initInventoryEconomyMap(invMoneyTypeCountArr, invContents, infoArr);
		
		if(this.getIncreaseMoneyInfo(amount, needMoneyTypeArr) != 0)
		{// 지불할 소액권 없음.
			return EconomyActionResult.unitTooSmall;
		}
		
		if(this.issuanceReadyTask(needMoneyTypeArr, infoArr, -1) != 0)
		{
			return EconomyActionResult.insufficientSpace;
		}
		
		IntLocation intLoc = new IntLocation(chestInv.getLocation());
		this.modifyInventory(chestInv, infoArr);
		
		this.chestTracker.onIssuanceToChest(id, chestInv, amount);
		this.tradeTracker.normalIssuance(id, amount, intLoc, cause, args);
		
		return EconomyActionResult.OK;
	}
	
	public EconomyActionResult consumeFromChest(SystemID id, Inventory chestInv, int amount, String cause, String args)
	{
		int[] invMoneyTypeCountArr = new int[this.info.count];
		int[] needMoneyTypeArr = new int[this.info.count];
		
		ItemStack[] cInvContents = chestInv.getContents();
		ModifyInventoryInfo[] cInfoArr = new ModifyInventoryInfo[cInvContents.length];
		if(this.initInventoryEconomyMap(invMoneyTypeCountArr, cInvContents, cInfoArr) < amount)
		{//돈부족
			return EconomyActionResult.insufficientMoney;
		}

		if(this.getDecreaseMoneyInfo(amount, invMoneyTypeCountArr, needMoneyTypeArr) != 0)
		{// 지불할 소액권 없음.
			return EconomyActionResult.unitTooSmall;
		}
			
		this.consumeReadyTask(needMoneyTypeArr, cInfoArr);
		
		if(this.issuanceReadyTask(needMoneyTypeArr, cInfoArr, -1) != 0)
		{
			return EconomyActionResult.insufficientSpace;
		}
		this.modifyInventory(chestInv, cInfoArr);
		
		this.chestTracker.onConsumeFromChest(chestInv, amount, id, cause, args);
		
		return EconomyActionResult.OK;
	}
	
	public EconomyActionResult movePlayerMoneyToPlayer(HumanEntity source, HumanEntity target, int amount, String cause, String args)
	{
		int[] invMoneyTypeCountArr = new int[this.info.count];
		int[] needMoneyTypeArr = new int[this.info.count];
		
		Inventory sourceInv = source.getInventory();
		ItemStack[] sInvContents = sourceInv.getContents();
		ModifyInventoryInfo[] sInfoArr = new ModifyInventoryInfo[sInvContents.length];
		
		int sourceInvMoney = this.initInventoryEconomyMap(invMoneyTypeCountArr, sInvContents, sInfoArr);
		if(sourceInvMoney < amount)
		{//돈부족
			return EconomyActionResult.insufficientMoney;
		}

		if(this.getDecreaseMoneyInfo(amount, invMoneyTypeCountArr, needMoneyTypeArr) != 0)
		{// 지불할 소액권 없음.
			return EconomyActionResult.unitTooSmall;
		}
			
		this.consumeReadyTask(needMoneyTypeArr, sInfoArr);
		
		if(this.issuanceReadyTask(needMoneyTypeArr, sInfoArr, MAX_PLAYER_INVENTORY_SIZE) != 0)
		{
			return EconomyActionResult.insufficientSpace;
		}
		
		for(int i = 0; i < this.info.count; ++i)
		{
			invMoneyTypeCountArr[i] = 0;
			needMoneyTypeArr[i] = 0;
		}
		
		Inventory targetInv = target.getInventory();
		ItemStack[] tInvContents = targetInv.getContents();
		ModifyInventoryInfo[] tInfoArr = new ModifyInventoryInfo[tInvContents.length];
		
		this.initInventoryEconomyMap(invMoneyTypeCountArr, tInvContents, tInfoArr);
		if(this.getIncreaseMoneyInfo(amount, needMoneyTypeArr) != 0)
		{// 지불할 소액권 없음.
			return EconomyActionResult.unitTooSmall;
		}
		
		if(this.issuanceReadyTask(needMoneyTypeArr, tInfoArr, MAX_PLAYER_INVENTORY_SIZE) != 0)
		{
			return EconomyActionResult.insufficientSpace;
		}
		
		UUID sourceUID = source.getUniqueId();
		UUID targetUID = target.getUniqueId();
		IntLocation intLoc = new IntLocation(source.getLocation());
		
		int playerStorageMoney = this.dataStorage.playerDependEconomy.getMoney(sourceUID);
		if(playerStorageMoney < amount)
		{
			this.dataStorage.playerDependEconomy.increaseEconomy(sourceUID, sourceInvMoney - playerStorageMoney);
			this.tradeTracker.forceRebalancing(sourceUID, sourceInvMoney - playerStorageMoney, "CONSUME_MONEY", intLoc);
		}
		this.modifyInventory(sourceInv, sInfoArr);
		this.modifyInventory(targetInv, tInfoArr);
		this.dataStorage.playerDependEconomy.decreaseEconomy(sourceUID, amount);
		this.dataStorage.playerDependEconomy.increaseEconomy(targetUID, amount);
		this.tradeTracker.tradeLog(sourceUID, DependType.PLAYER, targetUID, DependType.PLAYER, amount, intLoc, cause, args);
		
		return EconomyActionResult.OK;
	}
	
	public EconomyActionResult moveChestMoneyToPlayer(Inventory chestInv, HumanEntity player, int amount)
	{
		int[] invMoneyTypeCountArr = new int[this.info.count];
		int[] needMoneyTypeArr = new int[this.info.count];
		
		ItemStack[] cInvContents = chestInv.getContents();
		ModifyInventoryInfo[] cInfoArr = new ModifyInventoryInfo[cInvContents.length];
		if(this.initInventoryEconomyMap(invMoneyTypeCountArr, cInvContents, cInfoArr) < amount)
		{//돈부족
			return EconomyActionResult.insufficientMoney;
		}

		if(this.getDecreaseMoneyInfo(amount, invMoneyTypeCountArr, needMoneyTypeArr) != 0)
		{// 지불할 소액권 없음.
			return EconomyActionResult.unitTooSmall;
		}
			
		this.consumeReadyTask(needMoneyTypeArr, cInfoArr);
		
		if(this.issuanceReadyTask(needMoneyTypeArr, cInfoArr, -1) != 0)
		{
			return EconomyActionResult.insufficientSpace;
		}
		
		for(int i = 0; i < this.info.count; ++i)
		{
			invMoneyTypeCountArr[i] = 0;
			needMoneyTypeArr[i] = 0;
		}
		Inventory playerInv = player.getInventory();
		ItemStack[] pInvContents = playerInv.getContents();
		ModifyInventoryInfo[] pInfoArr = new ModifyInventoryInfo[pInvContents.length];
		
		this.initInventoryEconomyMap(invMoneyTypeCountArr, pInvContents, pInfoArr);
		if(this.getIncreaseMoneyInfo(amount, needMoneyTypeArr) != 0)
		{// 지불할 소액권 없음.
			return EconomyActionResult.unitTooSmall;
		}
		
		if(this.issuanceReadyTask(needMoneyTypeArr, pInfoArr, MAX_PLAYER_INVENTORY_SIZE) != 0)
		{
			return EconomyActionResult.insufficientSpace;
		}
		
		this.modifyInventory(chestInv, cInfoArr);
		this.modifyInventory(playerInv, pInfoArr);
		
		this.chestTracker.onChestMoneyToPlayer(chestInv, player, amount);
		return EconomyActionResult.OK;
	}
	
	public EconomyActionResult movePlayerMoneyToChest(HumanEntity player, Inventory chestInv, int amount)
	{
		int[] invMoneyTypeCountArr = new int[this.info.count];
		int[] needMoneyTypeArr = new int[this.info.count];
		
		Inventory playerInv = player.getInventory();
		ItemStack[] pInvContents = playerInv.getContents();
		ModifyInventoryInfo[] pInfoArr = new ModifyInventoryInfo[pInvContents.length];
		if(this.initInventoryEconomyMap(invMoneyTypeCountArr, pInvContents, pInfoArr) < amount)
		{//돈부족
			return EconomyActionResult.insufficientMoney;
		}

		if(this.getDecreaseMoneyInfo(amount, invMoneyTypeCountArr, needMoneyTypeArr) != 0)
		{// 지불할 소액권 없음.
			return EconomyActionResult.unitTooSmall;
		}
			
		this.consumeReadyTask(needMoneyTypeArr, pInfoArr);
		
		if(this.issuanceReadyTask(needMoneyTypeArr, pInfoArr, MAX_PLAYER_INVENTORY_SIZE) != 0)
		{
			return EconomyActionResult.insufficientSpace;
		}
		
		for(int i = 0; i < this.info.count; ++i)
		{
			invMoneyTypeCountArr[i] = 0;
			needMoneyTypeArr[i] = 0;
		}
		
		ItemStack[] cInvContents = chestInv.getContents();
		ModifyInventoryInfo[] cInfoArr = new ModifyInventoryInfo[cInvContents.length];
		
		this.initInventoryEconomyMap(invMoneyTypeCountArr, cInvContents, cInfoArr);
		if(this.getIncreaseMoneyInfo(amount, needMoneyTypeArr) != 0)
		{// 지불할 소액권 없음.
			return EconomyActionResult.unitTooSmall;
		}
		
		if(this.issuanceReadyTask(needMoneyTypeArr, cInfoArr, -1) != 0)
		{
			return EconomyActionResult.insufficientSpace;
		}
		
		this.modifyInventory(playerInv, pInfoArr);
		this.modifyInventory(chestInv, cInfoArr);
		
		this.chestTracker.onPlayerMoneyToChest(player, chestInv, amount);
		return EconomyActionResult.OK;
	}
	
	private int initInventoryEconomyMap(int[] invMoneyTypeCountArr, ItemStack[] invContents, ModifyInventoryInfo[] infoArr)
	{
		int moneyTotal = 0;
		for(int i = 0; i < infoArr.length; ++i)
		{// 개인이 소유한 총액과 각 동전의 수량 계산, 인벤토리 돈에대한 정보지도 만들기.
			ItemStack stack = invContents[i];
			if(invContents[i] == null)
			{
				infoArr[i] = new ModifyInventoryInfo();
			}
			else
			{
				int invContentAmount = stack.getAmount();
				for(int j = 0; j < this.info.count; ++j)
				{
					CoinMetadata coinInfo = this.info.moneyList.get(j);
					if(coinInfo.itemStack.isSimilar(invContents[i]))
					{
						invMoneyTypeCountArr[j]+=invContentAmount;
						moneyTotal += (coinInfo.value * invContentAmount);
						infoArr[i] = new ModifyInventoryInfo();
						infoArr[i].actionMeta = j;
						infoArr[i].actionModify = invContentAmount;
						break;
					}
				}
			}
		}
		return moneyTotal;
	}
	
	private int getDecreaseMoneyInfo(int amount, int[] invMoneyTypeCountArr, int[] needMoneyTypeArr)
	{
		int leftMoney = amount;
		for(int i = 0; i < this.info.count; ++i)
		{// 각 동전이 얼마나 필요한지 계산.
			CoinMetadata coinInfo = this.info.moneyList.get(i);
			int m = invMoneyTypeCountArr[i] * coinInfo.value;
			
			if(m >= leftMoney)
			{
				needMoneyTypeArr[i] += (leftMoney / coinInfo.value);
				int change = leftMoney % coinInfo.value;
				
				if(change != 0)
				{//잔돈계산
					needMoneyTypeArr[i] += 1;
					change = coinInfo.value - change;
					//Bukkit.broadcastMessage("change:" + change);
					for(int j = i - 1; j >= 0; --j)
					{
						CoinMetadata changeCoinInfo = this.info.moneyList.get(j);
						
						needMoneyTypeArr[j] -= (change / changeCoinInfo.value);
						change = change % changeCoinInfo.value;
						//Bukkit.broadcastMessage("changeCal:" + (change / changeCoinInfo.value) + " remain:" +change);
						if(change == 0) break;
					}
				}
				leftMoney = 0;
				return change;
			}
			else
			{
				needMoneyTypeArr[i] += invMoneyTypeCountArr[i];
				leftMoney -= m;
			}
		}
		return 0;
	}
	
	public int getIncreaseMoneyInfo(int amount, int[] needMoneyTypeArr)
	{
		int remain = amount;
		for(int i = this.info.count - 1; i >= 0; --i)
		{
			int coinValue = this.info.moneyList.get(i).value;
			if(remain < coinValue)
			{
				continue;
			}
			needMoneyTypeArr[i] = -(remain / coinValue);
			remain = remain % coinValue;
		}
		return remain;
		
	}
	
	private void consumeReadyTask(int[] needMoneyTypeArr, ModifyInventoryInfo[] infoArr)
	{
		for(int i = 0; i < infoArr.length; ++i)
		{// 각 동전이 실제 인벤토리에 들어갈 자리 계산.(돈 소모 먼저 계산)
			if(infoArr[i] != null && infoArr[i].actionMeta != -1 && needMoneyTypeArr[infoArr[i].actionMeta] > 0)
			{
				int invContentAmount = infoArr[i].actionModify;
				int metaIndex = infoArr[i].actionMeta;
				if(needMoneyTypeArr[metaIndex] > 0)
				{// 돈 소모를 먼저 계산함.
				 // i자리의 돈 편집 정보를 저장함.
					if(needMoneyTypeArr[metaIndex] >= invContentAmount)
					{
						infoArr[i].actionModify = 0;
						needMoneyTypeArr[metaIndex] -= invContentAmount;
					}
					else
					{
						infoArr[i].actionModify -= needMoneyTypeArr[infoArr[i].actionMeta];
						needMoneyTypeArr[metaIndex] = 0;
					}
				}
			}
		}
	}
	
	
	private int issuanceReadyTask(int[] needMoneyTypeArr, ModifyInventoryInfo[] infoArr, int limit)
	{
		int editCount = 0;
		for(int i = 0; i < this.info.count; ++i)
		{
			if(needMoneyTypeArr[i] < 0)
			{
				editCount -= needMoneyTypeArr[i];
			}
		}
		if(limit == -1) limit = infoArr.length;
		for(int i = 0; i < limit; ++i)
		{// 각 동전이 실제 인벤토리에 들어갈 자리 계산.(돈 지급 계산)
			if(infoArr[i] != null)
			{
				if(infoArr[i].actionMeta == -1 || infoArr[i].actionModify == 0)
				{// 인벤토리의 i자리가 비었을 경우
					for(int j = 0; j < this.info.count; ++j)
					{
						if(needMoneyTypeArr[j] < 0)
						{// 돈 지급 계산.
							if(needMoneyTypeArr[j] + coinMaxStackSize[j] <= 0)
							{
								infoArr[i].actionMeta = j;
								infoArr[i].actionModify = coinMaxStackSize[j];
								needMoneyTypeArr[j] += coinMaxStackSize[j];
								editCount -= coinMaxStackSize[j];
							}
							else
							{
								infoArr[i].actionMeta = j;
								infoArr[i].actionModify = -needMoneyTypeArr[j];
								editCount += needMoneyTypeArr[j];
								needMoneyTypeArr[j] = 0;
							}
							break;
						}
					}
				}
				else
				{// 인벤토리의 i자리에 돈이 있을 경우.
					int coinIndex = infoArr[i].actionMeta;
					if(needMoneyTypeArr[coinIndex] < 0)
					{//해당 자리에 지급해야 할 돈이 있는 경우.
						int maxSlot = coinMaxStackSize[coinIndex] - infoArr[i].actionModify;
						if(needMoneyTypeArr[coinIndex] + maxSlot <= 0)
						{
							infoArr[i].actionModify = coinMaxStackSize[coinIndex];
							needMoneyTypeArr[coinIndex] += maxSlot;
							editCount -= maxSlot;
						}
						else
						{
							infoArr[i].actionModify -= needMoneyTypeArr[coinIndex];
							editCount += needMoneyTypeArr[coinIndex];
							needMoneyTypeArr[coinIndex] = 0;
						}
					}
				}
			}
		}
		return editCount;
	}
	
	private void modifyInventory(Inventory inv, ModifyInventoryInfo[] infoArr)
	{
		for(int i = 0; i < infoArr.length; ++i)
		{
			ModifyInventoryInfo info = infoArr[i];
			if(infoArr[i] != null && info.actionMeta != -1)
			{
				ItemStack invItem = inv.getItem(i);
				if(info.actionModify == 0)
				{
					if(invItem != null) inv.setItem(i, null);
				}
				else
				{
					CoinMetadata meta = this.info.moneyList.get(info.actionMeta);
					if(meta.itemStack.isSimilar(invItem))
					{
						if(invItem.getAmount() != info.actionModify)
						{
							invItem.setAmount(info.actionModify);
						}
					}
					else
					{
						ItemStack itemStack = meta.itemStack.clone();
						itemStack.setAmount(info.actionModify);
						inv.setItem(i, itemStack);
					}
				}
			}
		}
	}
	
	
	private ItemStack[] getMoneyItemStack(int amount)
	{
		if(amount % this.info.moneyList.get(0).value != 0) return null;
		int coinCntArr[] = new int[this.info.moneyList.size()];
		
		for(int i = this.info.moneyList.size() - 1; i >= 0; --i)
		{
			CoinMetadata meta = this.info.moneyList.get(i);
			coinCntArr[i] = amount / meta.value;
			amount = amount % meta.value;
		}
		
		List<ItemStack> moneyItemList = new ArrayList<>();
		for(int i = 0; i < this.info.moneyList.size(); ++i)
		{
			CoinMetadata meta = this.info.moneyList.get(i);
			int itemStackCnt = coinCntArr[i]/meta.maxStack;
			int itemStackRem = coinCntArr[i]%meta.maxStack;
			for(int j = 0; j < itemStackCnt; ++j)
			{
				ItemStack moneyItem = meta.itemStack.clone();
				moneyItem.setAmount(meta.maxStack);
				moneyItemList.add(moneyItem);
			}
			if(itemStackRem != 0)
			{
				ItemStack moneyItem = meta.itemStack.clone();
				moneyItem.setAmount(itemStackRem);
				moneyItemList.add(moneyItem);
			}
		}
		ItemStack itemStackArray[] = new ItemStack[moneyItemList.size()];
		moneyItemList.toArray(itemStackArray);
		return itemStackArray;
	}
}
