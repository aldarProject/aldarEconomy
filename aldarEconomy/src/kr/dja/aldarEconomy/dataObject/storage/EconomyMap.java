package kr.dja.aldarEconomy.dataObject.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EconomyMap<Depend, Info>
{
	private final Map<Depend, Integer> _eMap;
	public final Map<Depend, Integer> eMap;
	private int totalMoney;
	private final IncreaseEconomy<Depend> increaseCallback;
	private final DecreaseEconomy<Depend> decreaseCallback;
	
	public EconomyMap(IncreaseEconomy<Depend> increaseCallback, DecreaseEconomy<Depend> decreaseCallback)
	{
		this.increaseCallback = increaseCallback;
		this.decreaseCallback = decreaseCallback;
		this._eMap = new HashMap<>();
		this.eMap = Collections.unmodifiableMap(this._eMap);
		this.totalMoney = 0;
	}

	public void increaseEconomy(Depend obj, int amount)
	{
		this.totalMoney += amount;
		if(this._eMap.containsKey(obj))
		{
			int money = this._eMap.get(obj);
			money += amount;
			this._eMap.put(obj, money);
			this.increaseCallback.increaseEconomy(obj, amount, false);
		}
		else
		{
			this._eMap.put(obj, amount);
			this.increaseCallback.increaseEconomy(obj, amount, true);
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
			this.decreaseCallback.decreaseEconomy(obj, amount, true);
		}
		else
		{
			this._eMap.put(obj, money);
			this.decreaseCallback.decreaseEconomy(obj, amount, false);
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