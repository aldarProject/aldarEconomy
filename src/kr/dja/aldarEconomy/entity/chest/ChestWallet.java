package kr.dja.aldarEconomy.entity.chest;

import java.util.UUID;

import kr.dja.aldarEconomy.entity.DependType;
import kr.dja.aldarEconomy.entity.Wallet;

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
