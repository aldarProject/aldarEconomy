package kr.dja.aldarEconomy.dataObject.itemEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kr.dja.aldarEconomy.IntLocation;
import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.IEconomyObserver;
import kr.dja.aldarEconomy.dataObject.chest.ChestEconomyChild;

public class ItemEconomyStorage
{
	private final Map<UUID, ItemEconomyChild> _eMap;
	public final Map<UUID, ItemEconomyChild> eMap;
	private final IEconomyObserver<UUID, ItemWallet> callback;

	public ItemEconomyStorage(IEconomyObserver<UUID, ItemWallet> callback)
	{
		this.callback = callback;
		this._eMap = new HashMap<>();
		this.eMap = Collections.unmodifiableMap(this._eMap);
	}

	public ItemEconomyChild increaseEconomy(UUID itemUID, UUID owner, DependType ownerType, int amount)
	{
		ItemEconomyChild map = this._eMap.get(itemUID);
		if(map == null)
		{
			map = new ItemEconomyChild(this.callback, itemUID);
			this._eMap.put(itemUID, map);
		}
		map.increaseEconomy(owner, ownerType, amount);
		return map;
	}
	
	public void decreaseEconomy(UUID itemUID, UUID key, int amount)
	{
		ItemEconomyChild map = this._eMap.get(itemUID);
		map.decreaseEconomy(key, amount);
		if(map.getTotalMoney() <= 0)
		{
			this._eMap.remove(itemUID);
		}
	}
	
	public int getMoney(UUID key1, UUID key2)
	{
		ItemEconomyChild map = this._eMap.get(key1);
		if(map != null)
		{
			return map.getMoney(key2);
		}
		return 0;
	}

}
