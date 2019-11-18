package kr.dja.aldarEconomy.dataStorage.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kr.dja.aldarEconomy.api.SystemID;

public class MultipleEconomyMap<Depend1, Depend2>
{
	private final Map<Depend1, EconomyMapChild<Depend1, Depend2>> _eMap;
	public final Map<Depend1, EconomyMapChild<Depend1, Depend2>> eMap;
	public MultipleEconomyMap()
	{
		this._eMap = new HashMap<>();
		this.eMap = Collections.unmodifiableMap(this._eMap);
	}

	public EconomyMapChild<Depend1, Depend2> increaseEconomy(Depend1 key1, Depend2 key2, int amount)
	{
		EconomyMapChild<Depend1, Depend2> map = this._eMap.get(key1);
		if(map == null)
		{
			map = new EconomyMapChild<Depend1, Depend2>();
			map.parents.add(key1);
			this._eMap.put(key1, map);
		}
		map.increaseEconomy(key2, amount);
		return map;
	}
	
	public void decreaseEconomy(Depend1 obj1, Depend2 obj2, int amount)
	{
		EconomyMapChild<Depend1, Depend2> map = this._eMap.get(obj1);
		map.decreaseEconomy(obj2, amount);
		if(map.getTotalMoney() <= 0)
		{
			for(Depend1 key : map.parents)
			{
				this._eMap.remove(key);
			}
		}
	}
	
	public void appendKey(Depend1 key, EconomyMapChild<Depend1, Depend2> map)
	{// 더블체스트 고려, 여러 키로 단일 객체에 접근해야 함.
		if(!map.parents.contains(key))
		{
			map.parents.add(key);
			this._eMap.put(key, map);
		}
	}
	
	public void delKey(Depend1 key)
	{
		EconomyMapChild<Depend1, Depend2> map = this._eMap.getOrDefault(key, null);
		if(map != null)
		{
			this._eMap.remove(key);
			map.parents.remove(key);
		}
	}
	
	public int getMoney(Depend1 obj1, Depend2 obj2)
	{
		EconomyMap<Depend2> map = this._eMap.get(obj1);
		if(map != null)
		{
			return map.getMoney(obj2);
		}
		return 0;
	}
}


