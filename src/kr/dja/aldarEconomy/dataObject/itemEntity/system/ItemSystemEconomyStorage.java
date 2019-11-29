package kr.dja.aldarEconomy.dataObject.itemEntity.system;

import java.util.UUID;

import kr.dja.aldarEconomy.api.SystemID;
import kr.dja.aldarEconomy.dataObject.EconomyMap;
import kr.dja.aldarEconomy.dataObject.IEconomyObserver;
import kr.dja.aldarEconomy.dataObject.player.PlayerWallet;

public class ItemSystemEconomyStorage extends EconomyMap<UUID, ItemSystemWallet>
{

	public ItemSystemEconomyStorage(IEconomyObserver<UUID, ItemSystemWallet> callback)
	{
		super(callback);
	}

	protected void increaseEconomy(UUID key, SystemID id, int amount)
	{
		ItemSystemWallet wallet = this.eMap.get(key);
		if(wallet == null)
		{
			wallet = new ItemSystemWallet(key, id, amount);
			this.increaseEconomy(wallet, amount, true);
		}
		else
		{
			this.increaseEconomy(wallet, amount, false);
		}
	}
}
