package kr.dja.aldarEconomy.entity.enderChest;

import java.util.UUID;

import kr.dja.aldarEconomy.IntLocation;
import kr.dja.aldarEconomy.entity.EconomyMap;
import kr.dja.aldarEconomy.entity.IEconomyObserver;

public class EnderChestEconomyStorage extends EconomyMap<UUID, EnderChestWallet>
{

	public EnderChestEconomyStorage(IEconomyObserver<UUID, EnderChestWallet> callback) {
		super(callback);
	}

	public void increaseEconomy(UUID key, int amount)
	{
		EnderChestWallet wallet = this.eMap.get(key);
		if(wallet == null)
		{
			wallet = new EnderChestWallet(key);
		}
		this.increaseEconomy(wallet, amount);
		
	}
}
