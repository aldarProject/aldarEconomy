package kr.dja.aldarEconomy.dataObject.itemEntity.player;

import java.util.UUID;

import kr.dja.aldarEconomy.api.SystemID;
import kr.dja.aldarEconomy.dataObject.EconomyMap;
import kr.dja.aldarEconomy.dataObject.IEconomyObserver;
import kr.dja.aldarEconomy.dataObject.itemEntity.system.ItemSystemWallet;

public class ItemPlayerEconomyStorage extends EconomyMap<UUID, ItemPlayerWallet>
{

	public ItemPlayerEconomyStorage(IEconomyObserver<UUID, ItemPlayerWallet> callback)
	{
		super(callback);
	}

	protected void increaseEconomy(UUID key, UUID id, int amount)
	{
		ItemPlayerWallet wallet = this.eMap.get(key);
		if(wallet == null)
		{
			wallet = new ItemPlayerWallet(key, id, amount);
			this.increaseEconomy(wallet, amount, true);
		}
		else
		{
			this.increaseEconomy(wallet, amount, false);
		}
	}
}
