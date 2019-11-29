package kr.dja.aldarEconomy.dataObject.container.enderChest;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.Wallet;
import kr.dja.aldarEconomy.dataObject.container.IntLocation;

public class EnderChestWallet extends Wallet<IntLocation>
{
	public final UUID player;
	
	EnderChestWallet(IntLocation key, UUID player)
	{
		super(key);
		this.player = player;
	}
	
	void setMoney(int money)
	{
		this.money = money;
	}
}
