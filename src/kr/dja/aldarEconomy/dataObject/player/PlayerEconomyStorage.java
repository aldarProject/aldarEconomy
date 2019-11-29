package kr.dja.aldarEconomy.dataObject.player;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.EconomyMap;
import kr.dja.aldarEconomy.dataObject.IEconomyObserver;

public class PlayerEconomyStorage extends EconomyMap<UUID, PlayerWallet>
{

	public PlayerEconomyStorage(IEconomyObserver<UUID, PlayerWallet> callback)
	{
		super(callback);
	}

	protected void increaseEconomy(UUID key, int amount)
	{
		PlayerWallet wallet = this.eMap.get(key);
		if(wallet == null)
		{
			wallet = new PlayerWallet(key, amount);
			this.increaseEconomy(wallet, amount, true);
		}
		else
		{
			this.increaseEconomy(wallet, amount, false);
		}
	}
}
