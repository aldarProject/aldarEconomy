package kr.dja.aldarEconomy.dataObject.itemEntity.system;

import java.util.UUID;

import kr.dja.aldarEconomy.api.SystemID;
import kr.dja.aldarEconomy.dataObject.Wallet;

public class ItemSystemWallet extends Wallet<UUID>
{
	public final SystemID systemID;
	
	ItemSystemWallet(UUID item, SystemID systemID, int money)
	{
		super(item);
		this.systemID = systemID;
	}
	
	void setMoney(int money)
	{
		this.money = money;
	}
}
