package kr.dja.aldarEconomy.dataObject.enderChest;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.IntLocation;
import kr.dja.aldarEconomy.dataObject.Wallet;

public class EnderChestWallet extends Wallet<UUID>
{
	EnderChestWallet(UUID key)
	{
		super(key);
	}
	
	void setMoney(int money)
	{
		this.money = money;
	}
}
