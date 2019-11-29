package kr.dja.aldarEconomy.dataObject.itemEntity;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.EconomyMap;
import kr.dja.aldarEconomy.dataObject.IEconomyObserver;

public class ItemEconomyChild extends EconomyMap<UUID, ItemWallet>
{
	public final UUID parent;

	public ItemEconomyChild(IEconomyObserver<UUID, ItemWallet> callback, UUID parent)
	{
		super(callback);
		this.parent = parent;
	}

	public void increaseEconomy(UUID key, DependType type, int amount)
	{
		ItemWallet wallet = this.eMap.get(key);
		if(wallet == null)
		{
			wallet = new ItemWallet(this.parent, key, type);
		}
		this.increaseEconomy(wallet, amount);
	} 
}
