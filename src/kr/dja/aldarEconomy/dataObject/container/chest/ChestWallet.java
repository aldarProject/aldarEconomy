package kr.dja.aldarEconomy.dataObject.container.chest;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.Wallet;

public class ChestWallet extends Wallet<UUID>
{
	public static final int WALLET_SYSTEM = 1;
	public static final int WALLET_PLAYER = 2;
	//public final int walletType;
	
	ChestWallet(UUID id)
	{
		super(id);
	}
	
	void setMoney(int money)
	{
		this.money = money;
	}
}
