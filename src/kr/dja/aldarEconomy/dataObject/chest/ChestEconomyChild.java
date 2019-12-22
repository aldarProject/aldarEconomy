package kr.dja.aldarEconomy.dataObject.chest;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.IntLocation;


public class ChestEconomyChild
{
	private final IChestObserver callback;
	private final Map<UUID, ChestWallet> _eMap;
	public final Map<UUID, ChestWallet> eMap;
	private int totalMoney;
	
	public final UUID uuid;
	final Set<IntLocation> parents;

	ChestEconomyChild(IChestObserver callback)
	{
		this.callback = callback;
		this._eMap = new HashMap<>();
		this.eMap = Collections.unmodifiableMap(this._eMap);
		this.totalMoney = 0;
		this.uuid = UUID.randomUUID();
		this.parents = new HashSet<>();
	}

	void increaseEconomy(UUID key, int amount, DependType type)
	{
		this.totalMoney += amount;
		ChestWallet wallet = this._eMap.get(key);
		if(wallet == null)
		{
			wallet = new ChestWallet(key, type);
			wallet.setMoney(amount);
			this._eMap.put(key, wallet);
		}
		else
		{
			wallet.setMoney(wallet.getMoney() + amount);
		}
		
		this.callback.modifyMoney(this.uuid, key, wallet, amount);
	}
	
	int decreaseEconomy(UUID key, int amount)
	{// 지갑에 남은 돈 반환해줌, 음수면 오류인것
		ChestWallet wallet = this._eMap.get(key);
		if(wallet == null) return -amount;
		int beforeMoney = wallet.getMoney();
		wallet.setMoney(wallet.getMoney() - amount);
		
		if(wallet.getMoney() <= 0)
		{
			this._eMap.remove(key);
			this.callback.modifyMoney(this.uuid, key, wallet, -beforeMoney);
			this.totalMoney -= beforeMoney;
		}
		else
		{
			this.callback.modifyMoney(this.uuid, key, wallet, -amount);
			this.totalMoney -= amount;
		}
		return beforeMoney - amount;
	}
	
	public int getMoney(UUID key)
	{
		ChestWallet wallet = this._eMap.get(key);
		if(wallet == null) return 0;
		return wallet.getMoney();
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("key: ");
		for(IntLocation loc : this.parents)
		{
			buf.append(loc.toString() + ",");
		}
		buf.append(" totalMoney:"+this.totalMoney);
		buf.append(" UUID:"+this.uuid);
		return buf.toString();
	}

	public int getTotalMoney()
	{
		return this.totalMoney;
	}
}