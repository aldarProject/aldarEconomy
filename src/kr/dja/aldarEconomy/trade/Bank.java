package kr.dja.aldarEconomy.trade;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.bergerkiller.bukkit.common.utils.ItemUtil;

import kr.dja.aldarEconomy.EconomyUtil;
import kr.dja.aldarEconomy.api.SystemID;
import kr.dja.aldarEconomy.data.EconomyDataStorage;
import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.IntLocation;
import kr.dja.aldarEconomy.setting.MoneyInfo;
import kr.dja.aldarEconomy.setting.MoneyMetadata;
import kr.dja.aldarEconomy.tracker.chest.ChestTracker;

public class Bank
{//돈을 발급해서 뿌려줌
	private final double ITEM_DIST = 0.5;
	private final MoneyInfo info;
	private final EconomyUtil util;
	private final EconomyDataStorage dataStorage;
	private final ChestTracker chestTracker;
	private final TradeTracker tradeTracker;
	
	public Bank(MoneyInfo info, EconomyUtil util, EconomyDataStorage dataStorage, ChestTracker chestTracker, TradeTracker tradeTracker)
	{
		this.info = info;
		this.util = util;
		this.dataStorage = dataStorage;
		this.chestTracker = chestTracker;
		this.tradeTracker = tradeTracker;
	}
	
	public ConsumeMoneyCheckResult checkConsumeMoney(SystemID id, String args, HumanEntity player, int amount)
	{
		int moneyTypeCount = this.info.moneyList.size();
		int[] invMoneyTypeCountArr = new int[moneyTypeCount];
		int[] needMoneyTypeArr = new int[moneyTypeCount];
		int moneyTotal = 0;
		for(int i = 0; i < moneyTypeCount; ++i)
		{// 배열 초기화.
			invMoneyTypeCountArr[i] = 0;
			needMoneyTypeArr[i] = 0;
		}
		Inventory playerInv = player.getInventory();
		ItemStack[] invContents = playerInv.getContents();
		
		for(ItemStack stack : invContents)
		{// 개인이 소유한 총액과 각 동전의 수량 계산.
			for(int i = 0; i < moneyTypeCount; ++i)
			{
				MoneyMetadata meta = this.info.moneyList.get(i);
				if(meta.itemStack.isSimilar(stack))
				{
					invMoneyTypeCountArr[i]+=stack.getAmount();
					moneyTotal += (meta.value * stack.getAmount());
					break;
				}
			}
		}
		if(moneyTotal < amount)
		{//돈부족
			return new ConsumeMoneyCheckResult(ConsumeMoneyResultType.insufficientMoney);
		}
		if(amount % this.info.moneyList.get(0).value != 0)
		{// 지불할 소액권 없음.
			return new ConsumeMoneyCheckResult(ConsumeMoneyResultType.changeIsNotPayable);
		}
		
		int leftMoney = amount;
		for(int i = 0; i < moneyTypeCount; ++i)
		{// 각 동전이 얼마나 필요한지 계산.
			MoneyMetadata coinInfo = this.info.moneyList.get(i);
			int m = invMoneyTypeCountArr[i] * coinInfo.value;
			
			if(m >= leftMoney)
			{
				needMoneyTypeArr[i] += (leftMoney / coinInfo.value);
				int change = leftMoney % coinInfo.value;
				if(change != 0)
				{
					needMoneyTypeArr[i] += 1;
					for(int j = i - 1; j >= 0; --j)
					{
						MoneyMetadata changeCoinInfo = this.info.moneyList.get(j);
						needMoneyTypeArr[j] -= (change / changeCoinInfo.value);
						change = change % changeCoinInfo.value;
						if(change == 0) break;
					}
				}
				leftMoney = 0;
				break;
			}
			else
			{
				needMoneyTypeArr[i] += invMoneyTypeCountArr[i];
				leftMoney -= m;
			}
		}
		
		ModifyInventoryInfo[] infoArr = new ModifyInventoryInfo[playerInv.getSize()];
		
		for(int i = 0; i < invContents.length; ++i)
		{// 각 동전이 실제 인벤토리에 들어갈 자리 계산.(돈 소모 먼저 계산)
			if(invContents[i] == null)
			{
				infoArr[i] = new ModifyInventoryInfo();
			}
			else
			{
				for(int j = 0; j < moneyTypeCount; ++j)
				{
					MoneyMetadata coinInfo = this.info.moneyList.get(j);
					if(coinInfo.itemStack.isSimilar(invContents[i]))
					{
						int invContentAmount = invContents[i].getAmount();
						if(needMoneyTypeArr[j] > 0)
						{// 돈 소모를 먼저 계산함.
						 // i자리의 돈 편집 정보를 저장함.
							if(needMoneyTypeArr[j] >= invContentAmount)
							{
								infoArr[i] = new ModifyInventoryInfo();
								infoArr[i].actionMeta = j;
								infoArr[i].actionModify = 0;
								needMoneyTypeArr[j] -= invContentAmount;
							}
							else
							{
								infoArr[i] = new ModifyInventoryInfo();
								infoArr[i].actionMeta = j;
								infoArr[i].actionModify = invContentAmount - needMoneyTypeArr[j];
								needMoneyTypeArr[j] = 0;
							}
						}
						else
						{// 아무 편집 안해도 일단 i 자리에 뭐가 있는지 저장.
							infoArr[i] = new ModifyInventoryInfo();
							infoArr[i].actionMeta = j;
							infoArr[i].actionModify = invContentAmount;
						}
						break;
					}
				}
			}
		}
		
		int[] coinMaxStackSize = new int[moneyTypeCount];
		for(int i = 0; i < moneyTypeCount; ++i)
		{
			coinMaxStackSize[i] = this.info.moneyList.get(i).maxStack;
		}

		for(int i = 0; i < infoArr.length; ++i)
		{// 각 동전이 실제 인벤토리에 들어갈 자리 계산.(돈 지급 계산)
			if(infoArr[i] != null)
			{
				if(infoArr[i].actionMeta == -1 || infoArr[i].actionModify == 0)
				{// 인벤토리의 i자리가 비었을 경우
					for(int j = 0; j < moneyTypeCount; ++j)
					{
						if(needMoneyTypeArr[j] < 0)
						{// 돈 지급 계산.
							if(needMoneyTypeArr[j] + coinMaxStackSize[j] <= 0)
							{
								infoArr[i].actionMeta = j;
								infoArr[i].actionModify = coinMaxStackSize[j];
								needMoneyTypeArr[j] += coinMaxStackSize[j];
							}
							else
							{
								infoArr[i].actionMeta = j;
								infoArr[i].actionModify = -needMoneyTypeArr[j];
								needMoneyTypeArr[j] = 0;
							}
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
						}
						else
						{
							infoArr[i].actionModify += -needMoneyTypeArr[coinIndex];
							needMoneyTypeArr[coinIndex] = 0;
						}
					}
				}
			}
		}
		
		for(int i = 0; i < moneyTypeCount; ++i)
		{
			if(needMoneyTypeArr[i] != 0)
			{
				return new ConsumeMoneyCheckResult(ConsumeMoneyResultType.insufficientChangeSpace);
			}
		}
		return new ConsumeMoneyCheckResult(ConsumeMoneyResultType.OK, invContents, infoArr, id, args, player, amount);
		
	}
	
	public void consumeMoney(ConsumeMoneyCheckResult result)
	{
		if(result.result != ConsumeMoneyResultType.OK) return;
		Inventory inv = result.player.getInventory();
		for(int i = 0; i < result.modifyInfo.length; ++i)
		{
			ModifyInventoryInfo info = result.modifyInfo[i];
			if(result.modifyInfo[i] != null && info.actionMeta != -1)
			{
				if(info.actionModify == 0)
				{
					inv.setItem(i, null);
				}
				else
				{
					ItemStack itemStack = this.getMonyItemStack(this.info.moneyList.get(info.actionMeta), info.actionModify);
					inv.setItem(i, itemStack);
				}
			}
		}
		this.tradeTracker.normalIssuance(result.player.getUniqueId(), result.amount, result.args, new IntLocation(result.player.getLocation()));
	}
	
	private ItemStack getMonyItemStack(MoneyMetadata moneyMeta, int count)
	{
		ItemStack itemStack = moneyMeta.itemStack.clone();
		itemStack.setAmount(count);
		return itemStack;
	}
	
	/*public boolean issuanceToItem(SystemID id, Location loc, int amount, String cause, String data)
	{
		World w = loc.getWorld();
		
		ItemStack[] moneyItemArr = this.getMoneyItemStack(amount);
		if(moneyItemArr == null) return false;
		
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
		
		return true;
	}
	
	public boolean issuanceToPlayer(SystemID id, HumanEntity player, int amount, String cause, String data)
	{
		ItemStack[] moneyItemArr = this.getMoneyItemStack(amount);
		if(moneyItemArr == null) return false;
		Inventory inv = player.getInventory();
		if(!ItemUtil.canTransferAll(moneyItemArr, inv)) return false;
		for(ItemStack item : moneyItemArr)
		{
			ItemUtil.transfer(item, inv, -1);
		}
		this.dataStorage.playerDependEconomy.increaseEconomy(player.getUniqueId(), amount);
		return true;
	}
	
	public boolean issuanceToChest(SystemID id, Chest chest, int amount, String cause, String data)
	{
		ItemStack[] moneyItemArr = this.getMoneyItemStack(amount);
		if(moneyItemArr == null) return false;
		Inventory inv = chest.getInventory();
		if(this.chestTracker.isOpenedEconomyChest(inv)) return false;
		if(!ItemUtil.canTransferAll(moneyItemArr, inv)) return false;
		for(ItemStack item : moneyItemArr)
		{
			ItemUtil.transfer(item, inv, -1);
		}
		IntLocation intLoc = new IntLocation(chest.getLocation());
		//this.dataStorage.chestDependEconomy.createAndIncreaseEconomy(intLoc, id.uuid, amount, DependType.SYSTEM);
		return true;
	}
	
	

	
	private ItemStack[] getMoneyItemStack(int amount)
	{
		if(amount % this.info.moneyList.get(0).value != 0) return null;
		int coinCntArr[] = new int[this.info.moneyList.size()];
		
		for(int i = this.info.moneyList.size() - 1; i >= 0; --i)
		{
			MoneyMetadata meta = this.info.moneyList.get(i);
			coinCntArr[i] = amount / meta.value;
			amount = amount % meta.value;
		}
		
		List<ItemStack> moneyItemList = new ArrayList<>();
		for(int i = 0; i < this.info.moneyList.size(); ++i)
		{
			MoneyMetadata meta = this.info.moneyList.get(i);
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
	}*/
}
