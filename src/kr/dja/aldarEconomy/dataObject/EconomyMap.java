package kr.dja.aldarEconomy.dataObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class EconomyMap<Depend, WalletType extends Wallet<Depend>>
{
	private final IEconomyObserver<Depend, WalletType> callback;
	private final Map<Depend, WalletType> _eMap;
	public final Map<Depend, WalletType> eMap;
	private int totalMoney;
	
	public EconomyMap(IEconomyObserver<Depend, WalletType> callback)
	{
		this.callback = callback;
		this._eMap = new HashMap<>();
		this.eMap = Collections.unmodifiableMap(this._eMap);
		this.totalMoney = 0;
	}

	protected void increaseEconomy(WalletType wallet, int amount)
	{
		if(!this._eMap.containsKey(wallet.depend))
		{
			this._eMap.put(wallet.depend, wallet);
		}
		wallet.money += amount;
		this.totalMoney += amount;
		this.callback.modifyMoney(wallet, amount);
	}
	
	public int decreaseEconomy(Depend key, int amount)
	{
		WalletType wallet = this._eMap.get(key);
		wallet.money -= amount;
		this.totalMoney -= amount;
		if(wallet.money == 0)
		{
			this._eMap.remove(key);
		}
		this.callback.modifyMoney(wallet, -amount);
		return wallet.money;
	}
	
	public int getMoney(Depend obj)
	{
		WalletType wallet = this._eMap.get(obj);
		if(wallet == null) return 0;
		return wallet.money;
	}

	public int getTotalMoney()
	{
		return this.totalMoney;
	}
}