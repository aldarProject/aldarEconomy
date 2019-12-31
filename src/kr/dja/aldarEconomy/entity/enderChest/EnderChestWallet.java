package kr.dja.aldarEconomy.entity.enderChest;

import java.util.UUID;

import kr.dja.aldarEconomy.IntLocation;
import kr.dja.aldarEconomy.entity.Wallet;

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
