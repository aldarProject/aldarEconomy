package kr.dja.aldarEconomy.dataObject.itemEntity;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.Wallet;

public class ItemWallet extends Wallet<UUID>
{
	public final UUID parent;
	public final DependType dependType;
	
	ItemWallet(UUID parent, UUID id, DependType dependType)
	{
		super(id);
		this.parent = parent;
		this.dependType = dependType;
		
	}
	
	void setMoney(int money)
	{
		this.money = money;
	}
}
