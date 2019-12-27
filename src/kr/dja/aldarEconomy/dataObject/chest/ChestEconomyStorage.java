package kr.dja.aldarEconomy.dataObject.chest;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

import kr.dja.aldarEconomy.IntLocation;
import kr.dja.aldarEconomy.dataObject.DependType;

public class ChestEconomyStorage
{
	private final IChestObserver callback;
	private final Map<IntLocation, ChestEconomyChild> _eMap;
	public final Map<IntLocation, ChestEconomyChild> eMap;
	private final Set<ChestEconomyChild> _childSet;
	public final Set<ChestEconomyChild> childSet;

	public ChestEconomyStorage(IChestObserver callback)
	{
		this.callback = callback;
		this._eMap = new HashMap<>();
		this.eMap = Collections.unmodifiableMap(this._eMap);
		this._childSet = new HashSet<>();
		this.childSet = Collections.unmodifiableSet(this._childSet);
	}
	
	public ChestEconomyChild createEconomyChild(IntLocation key)
	{
		ChestEconomyChild map = new ChestEconomyChild(this.callback);
		this._childSet.add(map);
		this.appendKey(key, map);
		return map;
	}
	
	public ChestEconomyChild increaseEconomy(ChestEconomyChild map, UUID key2, int amount, DependType type)
	{
		map.increaseEconomy(key2, amount, type);
		return map;
	}

	public void decreaseEconomy(ChestEconomyChild map, UUID key2, int amount)
	{
		map.decreaseEconomy(key2, amount);
		if(map.getTotalMoney() <= 0)
		{
			for(IntLocation key : map.parents)
			{
				this._eMap.remove(key);
				this.callback.deleteKey(key, map);
			}
			this._childSet.remove(map);
			map.parents.clear();
		}
		
	}
	
	public void appendKey(IntLocation key, ChestEconomyChild map)
	{// 더블체스트 고려, 여러 키로 단일 객체에 접근해야 함.
		map.parents.add(key);
		this._eMap.put(key, map);
		this.callback.appendKey(key, map);
	}
	
	public void delKey(IntLocation key)
	{
		ChestEconomyChild map = this._eMap.get(key);
		this._eMap.remove(key);
		map.parents.remove(key);
		this.callback.deleteKey(key, map);
	}
	
	public int getMoney(IntLocation key1, UUID key2)
	{
		ChestEconomyChild map = this._eMap.get(key1);
		if(map != null)
		{
			return map.getMoney(key2);
		}
		return 0;
	}
}