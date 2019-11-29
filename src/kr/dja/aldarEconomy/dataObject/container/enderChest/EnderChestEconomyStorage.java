package kr.dja.aldarEconomy.dataObject.container.enderChest;

import java.util.UUID;

import kr.dja.aldarEconomy.api.SystemID;
import kr.dja.aldarEconomy.dataObject.EconomyMap;
import kr.dja.aldarEconomy.dataObject.IEconomyObserver;
import kr.dja.aldarEconomy.dataObject.Wallet;
import kr.dja.aldarEconomy.dataObject.container.IntLocation;
import kr.dja.aldarEconomy.dataObject.player.PlayerWallet;

public class EnderChestEconomyStorage extends EconomyMap<IntLocation, EnderChestWallet>
{

	public EnderChestEconomyStorage(IEconomyObserver<IntLocation, EnderChestWallet> callback) {
		super(callback);
	}

	public void increaseEconomy(IntLocation key, UUID player, int amount)
	{
		EnderChestWallet wallet = this.eMap.get(key);
		if(wallet == null)
		{
			wallet = new EnderChestWallet(key, player);
		}
		this.increaseEconomy(wallet, amount);
		
	}
}
