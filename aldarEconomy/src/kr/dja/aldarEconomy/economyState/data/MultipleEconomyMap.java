package kr.dja.aldarEconomy.economyState.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kr.dja.aldarEconomy.api.SystemID;

public class MultipleEconomyMap<Depend1, Depend2>
{
	private final Map<Depend1, EconomyMap<Depend2>> _eMap;
	public final Map<Depend1, EconomyMap<Depend2>> eMap;
	public MultipleEconomyMap()
	{
		this._eMap = new HashMap<>();
		this.eMap = Collections.unmodifiableMap(this._eMap);
	}

	public boolean increaseEconomy(Depend1 obj1, Depend2 obj2, int amount)
	{
		if(amount <= 0) return false;
		EconomyMap<Depend2> map = this._eMap.get(obj1);
		if(map != null)
		{
			map = new EconomyMap<Depend2>();
			this._eMap.put(obj1, map);
		}
		if(!map.increaseEconomy(obj2, amount)) return false;
		
		return true;
	}
	
	public boolean increaseEconomy(Depend1 obj1_1, Depend1 obj1_2, Depend2 obj2, int amount)
	{// 더블 체스트를 고려하여 key 두개까지 가능.
		if(amount <= 0) return false;
		EconomyMap<Depend2> map1 = this._eMap.get(obj1_1);
		EconomyMap<Depend2> map2 = this._eMap.get(obj1_2);
		if(map != null)
		{
			map = new EconomyMap<Depend2>();
			this._eMap.put(obj1, map);
		}
		if(!map.increaseEconomy(obj2, amount)) return false;

		return true;
	}
	
	public boolean decreaseEconomy(Depend1 obj1, Depend2 obj2, int amount)
	{
		if(amount <= 0) return false;
		EconomyMap<Depend2> map = this._eMap.get(obj1);
		if(map == null) return false;
		
		if(!map.decreaseEconomy(obj2, amount)) return false;
		
		return true;
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


