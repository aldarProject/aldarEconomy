package kr.dja.aldarEconomy.dataObject.itemEntity.player;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.Wallet;

public class ItemPlayerWallet extends Wallet<UUID>
{
	public final UUID player;
	
	ItemPlayerWallet(UUID item, UUID player)
	{
		super(item);
		this.player = player;
	}
	
	void setMoney(int money)
	{
		this.money = money;
	}
}
