package kr.dja.aldarEconomy.dataObject.enderChest;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.IntLocation;
import kr.dja.aldarEconomy.dataObject.Wallet;

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
