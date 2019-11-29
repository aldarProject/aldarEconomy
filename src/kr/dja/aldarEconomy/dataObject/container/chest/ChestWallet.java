package kr.dja.aldarEconomy.dataObject.container.chest;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.Wallet;

public class ChestWallet extends Wallet<UUID>
{
	ChestWallet(UUID id, int money)
	{
		super(id, money);
	}
	
	void setMoney(int money)
	{
		this.money = money;
	}
}
