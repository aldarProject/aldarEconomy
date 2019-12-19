package kr.dja.aldarEconomy.tracker.item;

import java.util.UUID;

import kr.dja.aldarEconomy.tracker.chest.DestroyChestResult;

public class MoneyItemSpawnCacheData
{
	public static final int DESTORY_CHEST = 1;
	public static final int ENTITY_DEATH = 2;
	
	public final int type;
	public final int dropMoney;
	public final DestroyChestResult chestResult;
	public final UUID entityDeathResultUID;
	
	
	MoneyItemSpawnCacheData(int type, UUID entityDeathResultUID,int entityDeathResult)
	{
		this.type = type;
		this.dropMoney = entityDeathResult;
		this.chestResult = null;
		this.entityDeathResultUID = entityDeathResultUID;
	}
	
	MoneyItemSpawnCacheData(int type, DestroyChestResult destroyChestResult)
	{
		this.type = type;
		this.dropMoney = destroyChestResult.totalAmount;
		this.chestResult = destroyChestResult;
		this.entityDeathResultUID = null;
	}

}
