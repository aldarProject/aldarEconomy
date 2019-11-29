package kr.dja.aldarEconomy.dataObject.container.enderChest;

import kr.dja.aldarEconomy.dataObject.Wallet;
import kr.dja.aldarEconomy.dataObject.container.IntLocation;

public class EnderChestWallet extends Wallet<IntLocation>
{
	EnderChestWallet(IntLocation key, int money)
	{
		super(key, money);
	}
	
	void setMoney(int money)
	{
		this.money = money;
	}
}
