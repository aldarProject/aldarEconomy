package kr.dja.aldarEconomy.dataObject.multiKeyStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
public class MultipleEconomyMap<DependP, DependC>
{
	private final IncreaseEconomyMulti<DependC> increaseCallback;
	private final DecreaseEconomyMulti<DependC> decreaseCallback;
	private final AppendKey<DependP, DependC> appendKeyCallback;
	private final DeleteKey<DependP, DependC> deleteKeyCallback;
	
	private final Map<DependP, EconomyMapChild<DependP, DependC>> _eMap;
	public final Map<DependP, EconomyMapChild<DependP, DependC>> eMap;


	public MultipleEconomyMap(IncreaseEconomyMulti<DependC> increaseCallback
			, DecreaseEconomyMulti<DependC> decreaseCallback
			, AppendKey<DependP, DependC> appendKeyCallback
			, DeleteKey<DependP, DependC> deleteKeyCallback)
	{
		this.increaseCallback = increaseCallback;
		this.decreaseCallback = decreaseCallback;
		this.appendKeyCallback = appendKeyCallback;
		this.deleteKeyCallback = deleteKeyCallback;
		
		this._eMap = new HashMap<>();
		this.eMap = Collections.unmodifiableMap(this._eMap);
	}

	public EconomyMapChild<DependP, DependC> increaseEconomy(DependP key1, DependC key2, int amount)
	{
		EconomyMapChild<DependP, DependC> map = this._eMap.get(key1);
		if(map == null)
		{
			map = new EconomyMapChild<DependP, DependC>(this.increaseCallback, this.decreaseCallback);
			map.parents.add(key1);
			this._eMap.put(key1, map);
			this.appendKeyCallback.appendKey(key1, map);
		}
		map.increaseEconomy(key2, amount);
		return map;
	}
	
	public void decreaseEconomy(DependP key1, DependC key2, int amount)
	{
		EconomyMapChild<DependP, DependC> map = this._eMap.get(key1);
		map.decreaseEconomy(key2, amount);
		if(map.getTotalMoney() <= 0)
		{
			for(DependP key : map.parents)
			{
				this._eMap.remove(key);
				this.deleteKeyCallback.deleteKey(key, map);
			}
		}
	}
	
	public void appendKey(DependP key, EconomyMapChild<DependP, DependC> map)
	{// 더블체스트 고려, 여러 키로 단일 객체에 접근해야 함.
		if(!map.parents.contains(key))
		{
			map.parents.add(key);
			this._eMap.put(key, map);
			this.appendKeyCallback.appendKey(key, map);
		}
	}
	
	public void delKey(DependP key)
	{
		EconomyMapChild<DependP, DependC> map = this._eMap.getOrDefault(key, null);
		if(map != null)
		{
			this._eMap.remove(key);
			map.parents.remove(key);
			this.deleteKeyCallback.deleteKey(key, map);
		}
	}
	
	public int getMoney(DependP obj1, DependC obj2)
	{
		EconomyMapChild<DependP, DependC> map = this._eMap.get(obj1);
		if(map != null)
		{
			return map.getMoney(obj2);
		}
		return 0;
	}
}


