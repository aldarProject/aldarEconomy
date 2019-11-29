package kr.dja.aldarEconomy.dataObject.player;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.Wallet;

public class PlayerWallet extends Wallet<UUID>
{
	PlayerWallet(UUID id)
	{
		super(id);
	}
	
	void setMoney(int money)
	{
		this.money = money;
	}
}
