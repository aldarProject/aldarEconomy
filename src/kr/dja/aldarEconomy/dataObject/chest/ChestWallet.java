package kr.dja.aldarEconomy.dataObject.chest;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.Wallet;

public class ChestWallet extends Wallet<UUID>
{
	public final DependType dependType;
	
	ChestWallet(UUID id, DependType dependType)
	{
		super(id);
		this.dependType = dependType;
		
	}
	
	void setMoney(int money)
	{
		this.money = money;
	}
}
