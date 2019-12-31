package kr.dja.aldarEconomy.storage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

import kr.dja.aldarEconomy.IntLocation;

public class MoneyDetailResult
{
	public final UUID owner;
	public final long totalAsset;
	public final int chestMoneyTotal;
	public final Map<IntLocation, Integer> chestMoneyDetail;
	public final int inventoryMoney;
	public final int enderChestMoney;
	public final int itemMoneyTotal;
	public final Map<IntLocation, Integer> itemMoneyDetail;
	
	public MoneyDetailResult(UUID owner, long totalAsset, int chestMoney, Map<IntLocation, Integer> chestMoneyDetail, int inventoryMoney, int enderChestMoney, int itemMoneyTotal, Map<IntLocation, Integer> itemMoneyDetail)
	{
		this.owner = owner;
		this.totalAsset = totalAsset;
		this.chestMoneyTotal = chestMoney;
		this.chestMoneyDetail = Collections.unmodifiableMap(chestMoneyDetail);
		this.inventoryMoney = inventoryMoney;
		this.enderChestMoney = enderChestMoney;
		this.itemMoneyTotal = itemMoneyTotal;
		this.itemMoneyDetail = Collections.unmodifiableMap(itemMoneyDetail);
	}
}
