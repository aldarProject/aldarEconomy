package kr.dja.aldarEconomy.economyState.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kr.dja.aldarEconomy.api.SystemID;

public class EconomyMap<Depend>
{
	private final Map<Depend, Integer> _eMap;
	public final Map<Depend, Integer> eMap;
	private int totalMoney;
	public EconomyMap()
	{
		this._eMap = new HashMap<>();
		this.eMap = Collections.unmodifiableMap(this._eMap);
		this.totalMoney = 0;
	}

	public void increaseEconomy(Depend obj, int amount)
	{
		int money = this._eMap.getOrDefault(obj, 0);
		money += amount;
		this._eMap.put(obj, money);
		this.totalMoney += amount;
	}
	
	public void decreaseEconomy(Depend obj, int amount)
	{
		int money = this._eMap.getOrDefault(obj, 0);
		money -= amount;
		
		this._eMap.put(obj, money);
		this.totalMoney -= amount;
		if(money <= 0) this._eMap.remove(obj);
	}
	
	public int getMoney(Depend obj)
	{
		return this._eMap.getOrDefault(obj, 0);
	}

	public int getTotalMoney()
	{
		return this.totalMoney;
	}
}


