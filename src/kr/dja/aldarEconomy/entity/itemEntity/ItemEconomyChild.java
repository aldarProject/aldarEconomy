package kr.dja.aldarEconomy.entity.itemEntity;

import java.util.UUID;

import kr.dja.aldarEconomy.entity.DependType;
import kr.dja.aldarEconomy.entity.EconomyMap;
import kr.dja.aldarEconomy.entity.IEconomyObserver;

public class ItemEconomyChild extends EconomyMap<UUID, ItemWallet>
{
	public final UUID parent;

	public ItemEconomyChild(IEconomyObserver<UUID, ItemWallet> callback, UUID parent)
	{
		super(callback);
		this.parent = parent;
	}

	public void increaseEconomy(UUID owner, DependType ownerType, int amount)
	{
		ItemWallet wallet = this.eMap.get(owner);
		if(wallet == null)
		{
			wallet = new ItemWallet(this.parent, owner, ownerType);
		}
		this.increaseEconomy(wallet, amount);
	} 
}
