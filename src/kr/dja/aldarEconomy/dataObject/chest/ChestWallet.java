package kr.dja.aldarEconomy.dataObject.chest;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.Wallet;

public class ChestWallet extends Wallet<UUID>
{
	public final DependType ownerType;
	
	ChestWallet(UUID owner, DependType ownerType)
	{
		super(owner);
		this.ownerType = ownerType;
		
	}
	
	void setMoney(int money)
	{
		this.money = money;
	}
}
