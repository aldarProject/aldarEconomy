package kr.dja.aldarEconomy.dataObject.multiKeyStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class EconomyMapChild<DependP, Depend>
{
	private final Map<Depend, Integer> _eMap;
	public final Map<Depend, Integer> eMap;
	private int totalMoney;
	private final IncreaseEconomyMulti<Depend> increaseCallback;
	private final DecreaseEconomyMulti<Depend> decreaseCallback;
	
	public final UUID uuid;
	final Set<DependP> parents;

	EconomyMapChild(IncreaseEconomyMulti<Depend> increaseCallback, DecreaseEconomyMulti<Depend> decreaseCallback)
	{
		this.increaseCallback = increaseCallback;
		this.decreaseCallback = decreaseCallback;
		this._eMap = new HashMap<>();
		this.eMap = Collections.unmodifiableMap(this._eMap);
		this.totalMoney = 0;
		this.uuid = UUID.randomUUID();
		this.parents = new HashSet<>();
	}
	
	public void increaseEconomy(Depend key, int amount)
	{
		this.totalMoney += amount;
		if(this._eMap.containsKey(key))
		{
			int money = this._eMap.get(key);
			money += amount;
			this._eMap.put(key, money);
			this.increaseCallback.increaseEconomy(this.uuid, key, amount, false);
		}
		else
		{
			this._eMap.put(key, amount);
			this.increaseCallback.increaseEconomy(this.uuid, key, amount, true);
		}
	}
	
	public void decreaseEconomy(Depend obj, int amount)
	{
		int money = this._eMap.getOrDefault(obj, 0);
		money -= amount;
		this.totalMoney -= amount;
		if(money == 0)
		{
			
			this._eMap.remove(obj);
			this.decreaseCallback.decreaseEconomy(this.uuid, obj, amount, true);
		}
		else
		{
			this._eMap.put(obj, money);
			this.decreaseCallback.decreaseEconomy(this.uuid, obj, amount, false);
		}
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