package kr.dja.aldarEconomy.dataObject.itemEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.IEconomyObserver;

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

	public ItemEconomyChild increaseEconomy(UUID item, UUID key, DependType type, int amount)
	{
		ItemEconomyChild map = this._eMap.get(item);
		if(map == null)
		{
			map = new ItemEconomyChild(this.callback, item);
			this._eMap.put(item, map);
		}
		map.increaseEconomy(key, type, amount);
		return map;
	}
	
	public void decreaseEconomy(UUID item, UUID key, int amount)
	{
		ItemEconomyChild map = this._eMap.get(item);
		map.decreaseEconomy(key, amount);
		if(map.getTotalMoney() == 0)
		{
			this._eMap.remove(item);
		}
	}

}
