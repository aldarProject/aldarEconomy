package kr.dja.aldarEconomy.entity.player;

import java.util.UUID;

import kr.dja.aldarEconomy.entity.Wallet;

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
