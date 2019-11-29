package kr.dja.aldarEconomy.dataObject.system;

import kr.dja.aldarEconomy.api.SystemID;
import kr.dja.aldarEconomy.dataObject.EconomyMap;
import kr.dja.aldarEconomy.dataObject.IEconomyObserver;

public class SystemEconomyStorage extends EconomyMap<SystemID, SystemWallet>
{

	public SystemEconomyStorage(IEconomyObserver<SystemID, SystemWallet> callback)
	{
		super(callback);
	}

	protected void increaseEconomy(SystemID key, int amount)
	{
		SystemWallet wallet = this.eMap.get(key);
		if(wallet == null)
		{
			wallet = new SystemWallet(key);
		}
		this.increaseEconomy(wallet, amount);
	}
}
