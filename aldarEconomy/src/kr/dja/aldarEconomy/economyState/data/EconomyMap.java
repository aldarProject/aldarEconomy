package kr.dja.aldarEconomy.economyState.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kr.dja.aldarEconomy.api.SystemID;

public class EconomyMap<Depend>
{
	public final UUID uuid;
	private final Map<Depend, Integer> _eMap;
	public final Map<Depend, Integer> eMap;
	private int totalMoney;
	public EconomyMap()
	{
		this.uuid = UUID.randomUUID();
		this._eMap = new HashMap<>();
		this.eMap = Collections.unmodifiableMap(this._eMap);
		this.totalMoney = 0;
	}

	public boolean increaseEconomy(Depend obj, int amount)
	{
		if(amount <= 0) return false;
		int money = this._eMap.getOrDefault(obj, 0);
		money += amount;
		this._eMap.put(obj, money);
		this.totalMoney += amount;
		return true;
	}
	
	public boolean decreaseEconomy(Depend obj, int amount)
	{
		if(amount <= 0) return false;
		
		int money = this._eMap.getOrDefault(obj, 0);
		money -= amount;
		if(money < 0) return false;
		
		this._eMap.put(obj, money);
		this.totalMoney -= amount;
		if(money == 0) this._eMap.remove(obj);
		
		return true;
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


